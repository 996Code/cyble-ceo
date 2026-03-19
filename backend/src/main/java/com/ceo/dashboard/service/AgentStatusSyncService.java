package com.ceo.dashboard.service;

import com.ceo.dashboard.entity.Task;
import com.ceo.dashboard.model.AgentInteraction;
import com.ceo.dashboard.model.AgentLog;
import com.ceo.dashboard.model.AgentStatus;
import com.ceo.dashboard.repository.AgentInteractionRepository;
import com.ceo.dashboard.repository.AgentLogRepository;
import com.ceo.dashboard.repository.AgentStatusRepository;
import com.ceo.dashboard.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Agent 状态同步服务
 * 定时从 OpenClaw agents 目录读取 sessions.json，同步 Agent 状态到数据库
 */
@Service
public class AgentStatusSyncService {

    private static final Logger log = LoggerFactory.getLogger(AgentStatusSyncService.class);

    // 常量定义
    private static final String OPENCLAW_AGENTS_PATH = "/openclaw/agents";
    private static final String MAIN_AGENT_ID = "main";
    private static final String QWEN3_CODER_PLUS_AGENT_ID = "qwen3-coder-plus";
    private static final String SESSIONS_DIR = "sessions";
    private static final String SESSIONS_JSON_FILE = "sessions.json";
    private static final String JSONL_EXTENSION = ".jsonl";
    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";
    private static final String TOOL_RESULT_ROLE = "toolResult";
    private static final String TEXT_TYPE = "text";
    private static final String TOOL_USE_TYPE = "tool_use";
    private static final String IMAGE_URL_TYPE = "image_url";
    private static final String SUBAGENT_TASK_PREFIX = "[Subagent Task]:";
    private static final String ASSISTANT_KIND = "assistant";
    private static final String TASK_LABEL_ORIGIN = "label";
    private static final String TASK_NAME_ORIGIN = "task";
    private static final String UPDATED_AT_FIELD = "updatedAt";
    private static final String ABORTED_LAST_RUN_FIELD = "abortedLastRun";
    private static final String SESSION_ID_FIELD = "sessionId";
    private static final String MESSAGE_FIELD = "message";
    private static final String ROLE_FIELD = "role";
    private static final String CONTENT_FIELD = "content";
    private static final String ORIGIN_FIELD = "origin";
    private static final String TYPE_FIELD = "type";
    private static final String NAME_FIELD = "name";
    private static final String KIND_FIELD = "kind";
    private static final String ASSISTANT_LOG_LEVEL = "INFO";
    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MAX_TASK_NAME_LENGTH = 60;
    private static final int MAX_ACTIVITY_LENGTH = 80;
    private static final int SUBAGENT_TASK_MAX_LENGTH = 60;
    private static final long FIVE_MINUTES_MS = 5 * 60 * 1000; // 5分钟
    private static final String SUBAGENT_TOKEN = ":subagent:";
    private static final String SUBAGENT_TASK_TITLE_PREFIX = "Subagent Task: ";
    private static final String THINKING_ACTIVITY_PREFIX = "思考中: ";
    private static final String TASK_DOING_STATUS = "DOING";
    private static final String TASK_DONE_STATUS = "DONE";
    private static final String AGENT_CREATOR = "ceo";

    @Autowired
    private AgentStatusRepository agentStatusRepository;

    @Autowired
    private AgentInteractionRepository agentInteractionRepository;

    @Autowired
    private AgentLogRepository agentLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${openclaw.workspace.base:/openclaw}")
    private String workspaceBasePath;

    // 用于跟踪每个 .jsonl 文件上次同步的行数（增量同步）
    private final Map<String, Integer> lastSyncedLines = new HashMap<>();

    /**
     * 每 30 秒同步一次 Agent 状态和对话内容
     */
    @Scheduled(fixedRate = 30000)
    public void syncAgentStatus() {
        // log.info("开始同步 Agent 状态...");

        Path agentsRoot = Paths.get(OPENCLAW_AGENTS_PATH);

        if (!Files.exists(agentsRoot)) {
            log.warn("OpenClaw agents 目录不存在：{}", OPENCLAW_AGENTS_PATH);
            return;
        }

        try {
            // 遍历所有 Agent 目录
            int syncedCount = 0;
            for (Path agentDir : Files.newDirectoryStream(agentsRoot)) {
                if (!Files.isDirectory(agentDir)) continue;

                String agentId = agentDir.getFileName().toString();
                if (agentId.startsWith(".")) {  // 保持原样，因为"."不是有意义的常量
                    continue; // 跳过隐藏目录
                }
                
                // 跳过不需要监控的特殊目录
                if (MAIN_AGENT_ID.equals(agentId) || QWEN3_CODER_PLUS_AGENT_ID.equals(agentId)) {
                    continue; // 跳过 main 和 qwen3-coder-plus 目录
                }

                Path sessionsFile = agentDir.resolve(SESSIONS_DIR).resolve(SESSIONS_JSON_FILE);

                if (Files.exists(sessionsFile)) {
                    try {
                        AgentStatus status = parseAgentStatus(agentId, sessionsFile);
                        agentStatusRepository.save(status);
                        
                        // 同步对话内容
                        syncAgentConversations(agentId, agentDir);
                        
                        // 检查是否存在subagent会话，如果存在则创建对应的Task记录
                        syncSubagentTasks(agentId, sessionsFile);
                        
                        syncedCount++;
                    } catch (Exception e) {
                        log.error("同步 Agent {} 状态失败", agentId, e);
                    }
                }
            }

            // log.info("同步完成，共同步 {} 个 Agent", syncedCount);

        } catch (IOException e) {
            log.error("读取 OpenClaw agents 目录失败", e);
        }
    }

    /**
     * 解析 Agent 状态
     */
    private AgentStatus parseAgentStatus(String agentId, Path sessionsFile) throws IOException {
        String json = Files.readString(sessionsFile);
        JsonNode root = objectMapper.readTree(json);

        // 先从数据库中获取现有的 AgentStatus，以便保留其 updatedAt 时间
        AgentStatus status = agentStatusRepository.findById(agentId).orElse(new AgentStatus());
        status.setAgentId(agentId);
        status.setWorkspace(workspaceBasePath + "-" + agentId);

        // 查找最近更新的会话
        JsonNode latestSession = null;
        long latestUpdateTime = 0;
        String latestTaskName = null;
        String latestSessionKey = null;

        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode session = entry.getValue();

            // 获取会话更新时间
            long updatedAt = session.path(UPDATED_AT_FIELD).asLong(0);
            if (updatedAt > latestUpdateTime) {
                latestUpdateTime = updatedAt;
                latestSession = session;
                latestSessionKey = entry.getKey();
                
                // 从 origin 中提取任务名称
                JsonNode origin = session.path(ORIGIN_FIELD);
                if (origin != null && !origin.isNull()) {
                    // 只从 subagent 会话提取任务名，main/cron 等会话跳过
                    // main session 的 origin.label 是对话者名称，不是任务名
                    if (entry.getKey().contains(":subagent:")) {
                        String label = origin.path(TASK_LABEL_ORIGIN).asText("");
                        String task = origin.path(TASK_NAME_ORIGIN).asText("");
                        
                        if (!label.isEmpty()) {
                            latestTaskName = label;
                        } else if (!task.isEmpty()) {
                            latestTaskName = task;
                        }
                    }
                }
            }
        }

        // 设置当前任务名称
        if (latestTaskName != null && !latestTaskName.isEmpty()) {
            status.setCurrentTaskName(latestTaskName);
            status.setCurrentTaskProgress(50); // 默认 50%
        }

        // 检查最近的会话是否在活跃状态（最近5分钟内更新）
        boolean isActive = false;
        if (latestSession != null) {
            long currentTime = System.currentTimeMillis();
            long fiveMinutesAgo = currentTime - (5 * 60 * 1000); // 5分钟前的时间戳
            
            if (latestUpdateTime > fiveMinutesAgo) {
                isActive = true;
            }
        }

        // 确定 Agent 状态 - 基于 updatedAt 时间判断（最近5分钟内更新认为是忙碌状态）
        AgentStatus.Status previousStatus = status.getStatus(); // 保存之前的状态
        if (latestSession != null) {
            boolean aborted = latestSession.path(ABORTED_LAST_RUN_FIELD).asBoolean(false);
            
            if (aborted) {
                status.setStatus(AgentStatus.Status.ERROR);
            } else if (isActive) {
                status.setStatus(AgentStatus.Status.BUSY);
            } else {
                status.setStatus(AgentStatus.Status.IDLE);
                status.setCurrentTaskName(null);
                status.setCurrentTaskProgress(null);
            }
        } else {
            status.setStatus(AgentStatus.Status.IDLE);
            status.setCurrentTaskName(null);
            status.setCurrentTaskProgress(null);
        }

        // 只有当状态发生变化时才更新 lastActive 和 updatedAt
        if (previousStatus != status.getStatus() || status.getCreatedAt() == null) {
            // 状态发生了变化或首次创建，更新时间
            status.setLastActive(LocalDateTime.now());
        } else {
            // 状态未变化，使用之前的 lastActive 时间
            // 我们需要将时间戳转换为 LocalDateTime
            if (latestUpdateTime > 0) {
                status.setLastActive(LocalDateTime.ofEpochSecond(latestUpdateTime / 1000, 0, java.time.ZoneOffset.UTC));
            }
        }

        // 尝试从 .jsonl 文件中获取更精确的当前活动（仅 subagent 会话）
        if (latestSession != null && latestTaskName == null 
            && latestSessionKey != null && latestSessionKey.contains(":subagent:")) {
            Path sessionFile = Paths.get(sessionsFile.getParent().toString(), 
                    latestSession.path(SESSION_ID_FIELD).asText() + JSONL_EXTENSION);
            if (Files.exists(sessionFile)) {
                try {
                    String currentActivity = getCurrentActivityFromSessionFile(sessionFile);
                    if (currentActivity != null && !currentActivity.isEmpty()) {
                        status.setCurrentTaskName(currentActivity);
                    }
                } catch (Exception e) {
                    // log.debug("读取会话文件失败：{}", sessionFile, e);
                }
            }
        }

        // 当状态为BUSY但currentTaskName仍为空时，从.jsonl文件前10行中提取第一个user消息的任务名（仅 subagent 会话）
        if (status.getStatus() == AgentStatus.Status.BUSY && 
            (status.getCurrentTaskName() == null || status.getCurrentTaskName().isEmpty())
            && latestSessionKey != null && latestSessionKey.contains(":subagent:")) {
            Path sessionsDir = sessionsFile.getParent();
            String latestSessionId = latestSession != null ? latestSession.path(SESSION_ID_FIELD).asText("") : "";
            
            if (!latestSessionId.isEmpty()) {
                Path jsonlFile = sessionsDir.resolve(latestSessionId + ".jsonl");
                if (Files.exists(jsonlFile)) {
                    try (BufferedReader reader = Files.newBufferedReader(jsonlFile)) {
                        // 读取前 10 行找第一条 user 消息
                        for (int i = 0; i < 10; i++) {
                            String line = reader.readLine();
                            if (line == null) break;
                            try {
                                JsonNode msg = objectMapper.readTree(line);
                                JsonNode message = msg.path(MESSAGE_FIELD);
                                if (USER_ROLE.equals(message.path(ROLE_FIELD).asText())) {
                                    for (JsonNode c : message.path(CONTENT_FIELD)) {
                                        if (TEXT_TYPE.equals(c.path(TYPE_FIELD).asText())) {
                                            String text = c.path("text").asText("");
                                            // 提取 [Subagent Task]: 后面的内容
                                            int idx = text.indexOf(SUBAGENT_TASK_PREFIX);
                                            if (idx >= 0) {
                                                String taskText = text.substring(idx + 16).trim();
                                                // 取第一行作为任务名
                                                String[] lines = taskText.split("\n");
                                                String taskName = lines[0].trim();
                                                if (taskName.length() > 60) taskName = taskName.substring(0, 60) + "...";
                                                status.setCurrentTaskName(taskName);
                                            } else {
                                                // 没有 [Subagent Task] 前缀，直接用前 60 字符
                                                String taskName = text.length() > 60 ? text.substring(0, 60) + "..." : text;
                                                status.setCurrentTaskName(taskName);
                                            }
                                            break;
                                        }
                                    }
                                    break;  // 找到第一条 user 消息就停
                                }
                            } catch (Exception e) {
                                // 跳过非 JSON 行
                            }
                        }
                    } catch (Exception e) {
                        // log.debug("从会话文件前10行读取任务名失败：{}", jsonlFile, e);
                    }
                }
            }
        }

        // 对于 BUSY 的非 subagent 会话（如 main/cron），不显示任务名
        if (status.getStatus() == AgentStatus.Status.BUSY 
            && (latestSessionKey == null || !latestSessionKey.contains(":subagent:"))
            && latestTaskName == null) {
            status.setCurrentTaskName(null);
            status.setCurrentTaskProgress(null);
        }

        return status;
    }

    /**
     * 同步 Agent 的对话内容
     */
    private void syncAgentConversations(String agentId, Path agentDir) {
        try {
            Path sessionsDir = agentDir.resolve(SESSIONS_DIR);
            if (!Files.exists(sessionsDir)) {
                // log.debug("Agent {} 没有 sessions 目录", agentId);
                return;
            }

            // 遍历所有 .jsonl 会话文件
            for (Path sessionFile : Files.newDirectoryStream(sessionsDir, "*" + JSONL_EXTENSION)) {
                String fileName = sessionFile.getFileName().toString();
                String sessionId = fileName.substring(0, fileName.lastIndexOf(JSONL_EXTENSION));
                
                syncSessionFile(agentId, sessionId, sessionFile);
            }
        } catch (IOException e) {
            log.error("同步 Agent {} 对话内容失败", agentId, e);
        }
    }

    /**
     * 同步单个会话文件
     */
    private void syncSessionFile(String agentId, String sessionId, Path sessionFile) {
        try {
            // 获取已缓存的最后同步行数
            String filePath = sessionFile.toString();
            int lastSyncedLine = lastSyncedLines.getOrDefault(filePath, 0);
            
            // 计算文件总行数
            long totalLines = Files.lines(sessionFile).count();
            
            // 如果文件行数没有变化，跳过同步
            if (totalLines <= lastSyncedLine) {
                return;
            }
            
            // 读取新增的行
            try (BufferedReader reader = Files.newBufferedReader(sessionFile)) {
                String line;
                int currentLine = 0;
                
                while ((line = reader.readLine()) != null) {
                    currentLine++;
                    
                    // 只处理新增的行
                    if (currentLine <= lastSyncedLine) {
                        continue;
                    }
                    
                    try {
                        JsonNode messageNode = objectMapper.readTree(line);
                        processMessage(agentId, sessionId, messageNode);
                    } catch (Exception e) {
                        log.warn("解析会话文件 {} 第 {} 行时出错: {}", sessionFile, currentLine, e.getMessage());
                    }
                }
            }
            
            // 更新最后同步的行数
            lastSyncedLines.put(filePath, (int) totalLines);
            // log.debug("同步了 {} 条新消息，来自文件 {}", totalLines - lastSyncedLine, sessionFile);
            
        } catch (IOException e) {
            log.error("处理会话文件 {} 失败", sessionFile, e);
        }
    }

    /**
     * 处理单条消息
     */
    private void processMessage(String agentId, String sessionId, JsonNode messageNode) {
        try {
            JsonNode message = messageNode.path("message");
            String role = message.path(ROLE_FIELD).asText("");
            JsonNode contentNode = message.path(CONTENT_FIELD);
            
            // 根据角色处理不同类型的消息
            switch (role) {
                case USER_ROLE:
                    // 保存为交互记录（用户消息）
                    saveUserInteraction(agentId, sessionId, contentNode);
                    break;
                case ASSISTANT_ROLE:
                    // 保存为交互记录（助手回复）
                    saveAssistantInteraction(agentId, sessionId, contentNode);
                    break;
                case TOOL_RESULT_ROLE:
                    // 为了防止日志表中存入大量无关内容，我们不再保存toolResult的完整输出
                    // 仅在DEBUG级别记录
                    // log.debug("跳过工具执行结果: {}", contentNode.toString().substring(0, Math.min(100, contentNode.toString().length())));
                    break;
                default:
                    // log.debug("忽略未知角色的消息: {}", role);
                    break;
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    /**
     * 保存用户交互记录
     */
    private void saveUserInteraction(String agentId, String sessionId, JsonNode contentNode) {
        AgentInteraction interaction = new AgentInteraction();
        interaction.setAgentId(agentId);
        interaction.setTaskId(sessionId);
        interaction.setRole("USER");
        
        // 将内容转换为字符串
        String contentStr = formatContent(contentNode);
        interaction.setContent(contentStr);
        
        interaction.setInteractionTime(LocalDateTime.now());
        agentInteractionRepository.save(interaction);
    }

    /**
     * 保存助手交互记录
     */
    private void saveAssistantInteraction(String agentId, String sessionId, JsonNode contentNode) {
        AgentInteraction interaction = new AgentInteraction();
        interaction.setAgentId(agentId);
        interaction.setTaskId(sessionId);
        interaction.setRole("AGENT");
        
        // 将内容转换为字符串
        String contentStr = formatContent(contentNode);
        interaction.setContent(contentStr);
        
        interaction.setInteractionTime(LocalDateTime.now());
        agentInteractionRepository.save(interaction);
        
        // 同时保存到日志表
        saveAssistantLog(agentId, sessionId, contentStr);
    }

    /**
     * 保存助手日志
     */
    private void saveAssistantLog(String agentId, String sessionId, String contentStr) {
        AgentLog agentLog = new AgentLog();
        agentLog.setAgentId(agentId);
        agentLog.setTaskId(sessionId);
        agentLog.setLevel(ASSISTANT_LOG_LEVEL);
        
        agentLog.setMessage(contentStr);
        
        agentLog.setLogTime(LocalDateTime.now());
        agentLogRepository.save(agentLog);
    }

    /**
     * 保存工具执行结果日志
     */
    private void saveToolResultLog(String agentId, String sessionId, JsonNode contentNode) {
        AgentLog agentLog = new AgentLog();
        agentLog.setAgentId(agentId);
        agentLog.setTaskId(sessionId);
        agentLog.setLevel(ASSISTANT_LOG_LEVEL);
        
        // 将内容转换为字符串
        String contentStr = formatContent(contentNode);
        agentLog.setMessage(contentStr);
        
        agentLog.setLogTime(LocalDateTime.now());
        agentLogRepository.save(agentLog);
    }

    /**
     * 格式化内容节点为字符串
     */
    private String formatContent(JsonNode contentNode) {
        StringBuilder sb = new StringBuilder();
        
        if (contentNode.isArray()) {
            for (JsonNode item : contentNode) {
                String type = item.path("type").asText();
                if (TEXT_TYPE.equals(type)) {
                    String text = item.path("text").asText("");
                    // 清除ANSI颜色码
                    text = removeAnsiColors(text);
                    // 只保留合理长度的文本内容（避免存储过长的日志）
                    if (text.length() > MAX_CONTENT_LENGTH) {
                        text = text.substring(0, MAX_CONTENT_LENGTH) + "...";
                    }
                    sb.append(text);
                } else if (TOOL_USE_TYPE.equals(type)) {
                    sb.append("[工具调用: ").append(item.path(NAME_FIELD).asText()).append("]");
                } else if (IMAGE_URL_TYPE.equals(type)) {
                    sb.append("[图片]");
                } else {
                    // 对于其他类型，只保留文本内容并过滤
                    String itemText = item.toString();
                    itemText = removeAnsiColors(itemText);
                    if (itemText.length() > MAX_CONTENT_LENGTH) {
                        itemText = itemText.substring(0, MAX_CONTENT_LENGTH) + "...";
                    }
                    sb.append(itemText);
                }
                sb.append(" ");
            }
        } else if (contentNode.isTextual()) {
            String text = contentNode.asText();
            // 清除ANSI颜色码
            text = removeAnsiColors(text);
            // 只保留合理长度的文本内容
            if (text.length() > MAX_CONTENT_LENGTH) {
                text = text.substring(0, MAX_CONTENT_LENGTH) + "...";
            }
            sb.append(text);
        } else {
            String text = contentNode.toString();
            // 清除ANSI颜色码
            text = removeAnsiColors(text);
            // 只保留合理长度的文本内容
            if (text.length() > MAX_CONTENT_LENGTH) {
                text = text.substring(0, MAX_CONTENT_LENGTH) + "...";
            }
            sb.append(text);
        }
        
        return sb.toString().trim();
    }

    /**
     * 清除ANSI颜色码
     */
    private String removeAnsiColors(String text) {
        if (text == null) {
            return null;
        }
        // 正则表达式匹配ANSI转义序列
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    /**
     * 从 .jsonl 文件中获取当前活动
     */
    private String getCurrentActivityFromSessionFile(Path sessionFile) throws IOException {
        // 读取最后几行来获取当前活动
        try (var lines = Files.lines(sessionFile)) {
            var recentLines = lines.skip(Math.max(0, Files.lines(sessionFile).count() - 5)) // 读取最后5行
                    .toArray(String[]::new);
            
            // 从后往前查找最新的 assistant 消息
            for (int i = recentLines.length - 1; i >= 0; i--) {
                String line = recentLines[i];
                if (line.trim().isEmpty()) continue;
                
                try {
                    JsonNode message = objectMapper.readTree(line);
                    String kind = message.path("kind").asText("");
                    
                    if ("assistant".equals(kind)) {
                        String text = extractTextFromMessage(message);
                        if (text != null && !text.isEmpty()) {
                            return "思考中: " + text.substring(0, Math.min(80, text.length()));
                        }
                    }
                } catch (Exception e) {
                    // log.debug("解析会话文件行失败: {}", line, e);
                }
            }
        }
        
        return null;
    }

    /**
     * 读取文件最后一行
     */
    private String getLastLine(Path file) throws IOException {
        try (var lines = Files.lines(file)) {
            return lines.reduce((first, second) -> second).orElse(null);
        }
    }

    /**
     * 从消息中提取任务名称
     */
    private String extractTaskName(JsonNode message) {
        // 尝试从消息内容中提取任务信息
        JsonNode content = message.path("message");
        if (content.isArray()) {
            for (JsonNode item : content) {
                if (TEXT_TYPE.equals(item.path(TYPE_FIELD).asText())) {
                    String text = item.path("text").asText("");
                    // 简单提取任务名称（可以根据实际需要优化）
                    if (text.contains("任务") || text.contains("开发") || text.contains("测试")) {
                        return text.substring(0, Math.min(50, text.length()));
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从消息中提取文本内容
     */
    private String extractTextFromMessage(JsonNode message) {
        JsonNode content = message.path("message");
        if (content.isArray()) {
            for (JsonNode item : content) {
                if (TEXT_TYPE.equals(item.path(TYPE_FIELD).asText())) {
                    String text = item.path("text").asText("");
                    return text;
                }
            }
        } else if (content.isTextual()) {
            return content.asText();
        }
        return null;
    }

    /**
     * 同步subagent会话并创建Task记录
     */
    private void syncSubagentTasks(String agentId, Path sessionsFile) {
        try {
            String json = Files.readString(sessionsFile);
            JsonNode root = objectMapper.readTree(json);

            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String sessionKey = entry.getKey();
                JsonNode session = entry.getValue();

                // 检测是否为subagent会话
                if (sessionKey.contains(":subagent:")) {
                    String sessionKeyTemp = entry.getKey();
                    JsonNode sessionValue = entry.getValue();
                    
                    // 用实际的 sessionId（UUID）
                    String sessionId = sessionValue.path("sessionId").asText(sessionKeyTemp);
                    long updatedAt = sessionValue.path("updatedAt").asLong(0);
                    long currentTime = System.currentTimeMillis();
                    long ageMs = currentTime - updatedAt;
                    
                    // 从origin中提取任务名称
                    String taskName = null;
                    JsonNode origin = sessionValue.path("origin");
                    if (origin != null && !origin.isNull()) {
                        String label = origin.path("label").asText("");
                        String task = origin.path("task").asText("");
                        
                        if (!label.isEmpty()) {
                            taskName = label;
                        } else if (!task.isEmpty()) {
                            taskName = task;
                        }
                    }
                    
                    // 如果任务名称仍然为空，尝试从.jsonl文件的前10行中提取第一个user消息的任务名
                    if ((taskName == null || taskName.isEmpty()) && sessionValue.has("sessionId")) {
                        String sessionFileId = sessionValue.path("sessionId").asText("");
                        Path sessionDir = sessionsFile.getParent();
                        Path jsonlFile = sessionDir.resolve(sessionFileId + ".jsonl");
                        
                        if (Files.exists(jsonlFile)) {
                            try (BufferedReader reader = Files.newBufferedReader(jsonlFile)) {
                                // 读取前 10 行找第一条 user 消息
                                for (int i = 0; i < 10; i++) {
                                    String line = reader.readLine();
                                    if (line == null) break;
                                    try {
                                        JsonNode msg = objectMapper.readTree(line);
                                        JsonNode message = msg.path(MESSAGE_FIELD);
                                        if (USER_ROLE.equals(message.path(ROLE_FIELD).asText())) {
                                            for (JsonNode c : message.path(CONTENT_FIELD)) {
                                                if (TEXT_TYPE.equals(c.path(TYPE_FIELD).asText())) {
                                                    String text = c.path("text").asText("");
                                                    // 提取 [Subagent Task]: 后面的内容
                                                    int idx = text.indexOf(SUBAGENT_TASK_PREFIX);
                                                    if (idx >= 0) {
                                                        String taskText = text.substring(idx + 16).trim();
                                                        // 取第一行作为任务名
                                                        String[] lines = taskText.split("\n");
                                                        taskName = lines[0].trim();
                                                        if (taskName.length() > 60) taskName = taskName.substring(0, 60) + "...";
                                                    } else {
                                                        // 没有 [Subagent Task] 前缀，直接用前 60 字符
                                                        taskName = text.length() > 60 ? text.substring(0, 60) + "..." : text;
                                                    }
                                                    break;
                                                }
                                            }
                                            break;  // 找到第一条 user 消息就停
                                        }
                                    } catch (Exception e) {
                                        // 跳过非 JSON 行
                                    }
                                }
                            } catch (Exception e) {
                                // log.debug("从会话文件前10行读取任务名失败：{}", jsonlFile, e);
                            }
                        }
                    }

                    // 首先按 sessionKey 查找已有任务（不限于 OC- 前缀）
                    List<Task> matchedTasks = taskRepository.findBySessionKey(sessionKey);
                    Task existing = matchedTasks.isEmpty() ? null : matchedTasks.get(0);
                    // 如果有重复记录，删除多余的（保留最早创建的）
                    if (matchedTasks.size() > 1) {
                        for (int i = 1; i < matchedTasks.size(); i++) {
                            taskRepository.delete(matchedTasks.get(i));
                            log.info("删除重复任务: {}", matchedTasks.get(i).getId());
                        }
                    }

                    // 如果没有找到，按旧逻辑用 taskId 查找
                    if (existing == null) {
                        String taskId = "OC-" + agentId + "-" + sessionId.substring(0, Math.min(8, sessionId.length()));
                        existing = taskRepository.findById(taskId).orElse(null);
                    }

                    // 更新状态
                    if (existing != null) {
                        boolean changed = false;
                        
                        // 更新标题（如果还是默认的 Subagent Task: 前缀 或者标题为空）
                        if ((existing.getTitle() == null || existing.getTitle().startsWith(SUBAGENT_TASK_TITLE_PREFIX)) && taskName != null && !taskName.isEmpty()) {
                            existing.setTitle(taskName);
                            changed = true;
                        }
                        
                        // 检查会话是否已经结束（如果会话的finishedAt字段存在且不为0，说明会话已结束）
                        long finishedAt = sessionValue.path("finishedAt").asLong(0);
                        String newStatus;
                        
                        if (finishedAt > 0) {
                            // 会话已明确结束，状态应为DONE
                            newStatus = TASK_DONE_STATUS;
                        } else {
                            // 会话未结束，根据ageMs判断状态
                            newStatus = ageMs < FIVE_MINUTES_MS ? TASK_DOING_STATUS : TASK_DONE_STATUS;
                        }
                        
                        if (!newStatus.equals(existing.getStatus())) {
                            existing.setStatus(newStatus);
                            changed = true;
                            existing.setUpdatedAt(LocalDateTime.now());
                            if (TASK_DONE_STATUS.equals(newStatus) && existing.getCompletedAt() == null) {
                                existing.setCompletedAt(LocalDateTime.now());
                            }
                        }
                        
                        if (changed) {
                            taskRepository.save(existing);
                            log.info("更新现有任务: {} -> {} (状态: {})", existing.getId(), existing.getTitle(), existing.getStatus());
                        }
                    } else {
                        // 创建新 OC- 任务
                        String taskId = "OC-" + agentId + "-" + sessionId.substring(0, Math.min(8, sessionId.length()));
                        Task task = new Task();
                        task.setId(taskId);
                        task.setSessionKey(sessionKey); // 设置sessionKey
                        task.setTitle(taskName != null && !taskName.isEmpty() ? taskName : SUBAGENT_TASK_TITLE_PREFIX + sessionId);
                        task.setAssignee(agentId);
                        task.setCreator(AGENT_CREATOR);
                        
                        // 检查会话是否已经结束（如果会话的finishedAt字段存在且不为0，说明会话已结束）
                        long finishedAt = sessionValue.path("finishedAt").asLong(0);
                        if (finishedAt > 0) {
                            task.setStatus(TASK_DONE_STATUS);
                        } else {
                            task.setStatus(ageMs < FIVE_MINUTES_MS ? TASK_DOING_STATUS : TASK_DONE_STATUS); // 5分钟内更新的标记为DOING，否则为DONE
                        }
                        
                        task.setCreatedAt(LocalDateTime.now());
                        task.setUpdatedAt(LocalDateTime.now());
                        taskRepository.save(task);
                        
                        log.info("创建新任务: {} -> {} (分配给: {})", taskId, task.getTitle(), agentId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("同步subagent任务失败，agentId: {}", agentId, e);
        }
    }

    /**
     * 手动触发同步（用于调试）
     */
    public void syncNow() {
        log.info("手动触发 Agent 状态同步...");
        syncAgentStatus();
    }
}
