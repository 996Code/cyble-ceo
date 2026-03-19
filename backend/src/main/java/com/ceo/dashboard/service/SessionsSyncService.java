package com.ceo.dashboard.service;

import com.ceo.dashboard.entity.Task;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 OpenClaw runtime sessions.json 读取活跃会话
 */
@Service
public class SessionsSyncService {
    
    private static final Logger log = LoggerFactory.getLogger(SessionsSyncService.class);
    private static final String OPENCLAW_AGENTS_PATH = "/openclaw/agents";
    private static final long FIVE_MINUTES_MS = 5 * 60 * 1000;
    private static final long ONE_HOUR_MS = 60 * 60 * 1000;
    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 从 sessions.json 读取活跃的 OC-任务（实时计算，不持久化）
     */
    public List<Task> getActiveSessions() {
        List<Task> tasks = new ArrayList<>();
        Path agentsRoot = Paths.get(OPENCLAW_AGENTS_PATH);
        
        if (!Files.exists(agentsRoot)) {
            log.debug("OpenClaw agents 目录不存在：{}", OPENCLAW_AGENTS_PATH);
            return tasks;
        }
        
        long nowMs = System.currentTimeMillis();
        long oneDayAgo = nowMs - ONE_DAY_MS;
        
        try {
            for (Path agentDir : Files.newDirectoryStream(agentsRoot)) {
                if (!Files.isDirectory(agentDir)) continue;
                
                String agentId = agentDir.getFileName().toString();
                Path sessionsFile = agentDir.resolve("sessions/sessions.json");
                
                if (!Files.exists(sessionsFile)) continue;
                
                try {
                    String json = Files.readString(sessionsFile);
                    JsonNode root = objectMapper.readTree(json);
                    
                    if (!root.isObject()) continue;
                    
                    root.fields().forEachRemaining(entry -> {
                        String sessionKey = entry.getKey();
                        JsonNode row = entry.getValue();
                        
                        if (!row.isObject()) return;
                        
                        Task task = buildTask(agentId, sessionKey, row, nowMs, oneDayAgo);
                        if (task != null) {
                            tasks.add(task);
                        }
                    });
                    
                } catch (Exception e) {
                    log.debug("读取 {} 失败：{}", sessionsFile, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("扫描 agents 目录失败：{}", e.getMessage());
        }
        
        tasks.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
        return tasks;
    }
    
    private Task buildTask(String agentId, String sessionKey, JsonNode row, long nowMs, long oneDayAgo) {
        long updatedAt = row.path("updatedAt").asLong(0);
        long ageMs = nowMs - updatedAt;
        boolean aborted = row.path("abortedLastRun").asBoolean(false);
        long finishedAt = row.path("finishedAt").asLong(0);
        
        // 过滤：超过 24 小时
        if (updatedAt < oneDayAgo) return null;
        
        // 过滤：已结束超过 5 分钟
        if (finishedAt > 0 && (nowMs - finishedAt) > FIVE_MINUTES_MS) return null;
        
        // 计算状态
        String state = computeState(ageMs, aborted);
        
        // 过滤：只显示 Doing/Review/Blocked
        if (!state.equals("Doing") && !state.equals("Review") && !state.equals("Blocked")) return null;
        
        // 过滤：cron/subagent 除非 Blocked 否则不显示
        String title = extractTitle(row, sessionKey, agentId);
        if ((title.contains("定时任务") || title.contains("子任务")) && !state.equals("Blocked")) return null;
        
        // 构建 Task 对象
        String sessionId = row.path("sessionId").asText(sessionKey);
        String taskId = "OC-" + agentId + "-" + sessionId.substring(0, Math.min(8, sessionId.length()));
        
        Task task = new Task();
        task.setId(taskId);
        task.setTitle(title);
        task.setAssignee(agentId);
        task.setStatus(state);
        task.setCurrentProgress(extractCurrentActivity(row));
        task.setProgressPercent(null);
        task.setCreatedAt(msToLocalDateTime(updatedAt));
        task.setUpdatedAt(msToLocalDateTime(updatedAt));
        task.setStartedAt(null);
        task.setCompletedAt(null);
        task.setArchived(false);
        task.setAutoCreated(true);
        task.setSessionKey(sessionKey);
        
        return task;
    }
    
    private String computeState(long ageMs, boolean aborted) {
        if (aborted) return "Blocked";
        if (ageMs <= FIVE_MINUTES_MS) return "Doing";
        if (ageMs <= ONE_HOUR_MS) return "Review";
        return "Next";
    }
    
    private String extractTitle(JsonNode row, String sessionKey, String agentId) {
        String label = row.path("label").asText("");
        if (sessionKey.contains(":cron:") || label.contains("cron")) return agentId + "定时任务";
        if (sessionKey.contains(":subagent:") || label.contains("subagent")) return agentId + "子任务";
        if (!label.isEmpty() && !label.equals(sessionKey) && label.length() <= 60) return label;
        return agentId + "会话";
    }
    
    private String extractCurrentActivity(JsonNode row) {
        String sessionFile = row.path("sessionFile").asText("");
        if (sessionFile.isEmpty()) return "等待指令";
        
        Path path = Paths.get(sessionFile);
        if (!Files.exists(path)) return "等待指令";
        
        try {
            java.util.List<String> lines = Files.readAllLines(path);
            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    JsonNode event = objectMapper.readTree(line);
                    String role = event.path("message").path("role").asText("");
                    if ("assistant".equals(role)) {
                        String text = event.path("message").path("content").path(0).path("text").asText("");
                        if (!text.isEmpty()) {
                            text = text.replace("[[reply_to_current]]", "").trim();
                            String firstLine = text.split("\n")[0];
                            return firstLine.length() > 80 ? firstLine.substring(0, 80) + "..." : firstLine;
                        }
                    }
                } catch (Exception e) { /* skip */ }
            }
        } catch (IOException e) { /* skip */ }
        
        return "等待指令";
    }
    
    private LocalDateTime msToLocalDateTime(long ms) {
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
