package com.ceo.dashboard.service;

import com.ceo.dashboard.dto.*;
import com.ceo.dashboard.entity.Task;
import com.ceo.dashboard.exception.AgentNotFoundException;
import com.ceo.dashboard.model.AgentStatus;
import com.ceo.dashboard.model.AgentLog;
import com.ceo.dashboard.model.AgentReport;
import com.ceo.dashboard.model.AgentInteraction;
import com.ceo.dashboard.repository.AgentStatusRepository;
import com.ceo.dashboard.repository.AgentLogRepository;
import com.ceo.dashboard.repository.AgentReportRepository;
import com.ceo.dashboard.repository.AgentInteractionRepository;
import com.ceo.dashboard.repository.TaskRepository;
import com.ceo.dashboard.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard 服务类
 */
@Service
@Transactional
public class DashboardService {
    
    @Autowired
    private AgentStatusRepository agentStatusRepository;

    @Autowired
    private AgentLogRepository agentLogRepository;

    @Autowired
    private AgentReportRepository agentReportRepository;

    @Autowired
    private AgentInteractionRepository agentInteractionRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * 获取仪表板概览信息
     */
    public ApiResponse<OverviewResponseDTO> getOverview() {
        try {
            List<AgentStatus> allAgents = agentStatusRepository.findAll();
            Long totalAgents = (long) allAgents.size();
            
            // 统计活跃和非活跃代理
            long activeCount = allAgents.stream()
                    .filter(agent -> AgentStatus.Status.BUSY == agent.getStatus())
                    .count();
            long inactiveCount = totalAgents - activeCount;
            
            // 创建汇总统计
            Map<String, Object> summaryStats = new HashMap<>();
            summaryStats.put("totalTasks", 0); // 实际应用中应从任务表获取
            summaryStats.put("completedTasks", 0);
            summaryStats.put("pendingTasks", 0);
            summaryStats.put("errorRate", 0.0);
            
            OverviewResponseDTO response = new OverviewResponseDTO(
                totalAgents,
                activeCount,
                inactiveCount,
                summaryStats,
                LocalDateTime.now().toString()
            );
            
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取概览信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定 Agent 的详细信息
     */
    public EnhancedAgentDetailDTO getAgentDetail(String id) {
        Optional<AgentStatus> agentOpt = agentStatusRepository.findById(id);
        
        if (!agentOpt.isPresent()) {
            throw new AgentNotFoundException("Agent 不存在: " + id);
        }
        
        AgentStatus agent = agentOpt.get();
        
        // 设置分页大小，避免返回过多数据
        Pageable top100 = PageRequest.of(0, 100);
        
        // 从各个仓库获取详细数据（限制返回数量）
        List<AgentLog> logs = agentLogRepository.findByAgentIdOrderByLogTimeDesc(id, top100);
        List<AgentInteraction> conversations = agentInteractionRepository.findByAgentIdOrderByInteractionTimeDesc(id, top100);
        
        // 修复 Bug 3: 从 TaskRepository 获取该 Agent 的任务历史
        List<Task> agentTasks = taskRepository.findByAssigneeOrderByUpdatedAtDesc(id);
        
        // 将 Task 实体转换为 AgentReport 形式用于任务历史展示
        List<AgentReport> taskHistory = agentTasks.stream()
            .map(task -> {
                AgentReport report = new AgentReport();
                report.setAgentId(id);
                report.setReportType(AgentReport.ReportType.TASK_START); // 使用 TASK_START 作为任务类型标识
                report.setTaskName(task.getTitle());
                report.setContent(task.getStatus()); // 将状态作为内容
                report.setReportTime(task.getCreatedAt());
                return report;
            })
            .sorted((r1, r2) -> r2.getReportTime().compareTo(r1.getReportTime())) // 按时间倒序排列
            .limit(100) // 限制数量
            .collect(Collectors.toList());
        
        // 创建增强版详细信息
        EnhancedAgentDetailDTO detail = new EnhancedAgentDetailDTO();
        detail.setId(agent.getAgentId());
        detail.setName(agent.getWorkspace() != null ? agent.getWorkspace() : agent.getAgentId());
        detail.setStatus(agent.getStatus().toString());
        detail.setIp("");
        detail.setVersion("");
        detail.setLastHeartbeat(agent.getLastActive());
        detail.setLocation(agent.getWorkspace() != null ? agent.getWorkspace() : "");
        detail.setCapabilities(Arrays.asList("task-execution", "data-processing"));
        
        // 设置当前任务信息
        EnhancedAgentDetailDTO.CurrentTaskInfo currentTaskInfo = new EnhancedAgentDetailDTO.CurrentTaskInfo();
        currentTaskInfo.setName(agent.getCurrentTaskName());
        currentTaskInfo.setProgress(agent.getCurrentTaskProgress());
        
        // 查找最近的任务开始时间作为当前任务的开始时间
        Optional<AgentReport> latestTaskStart = taskHistory.stream()
            .filter(r -> r.getReportType() == AgentReport.ReportType.TASK_START)
            .findFirst();
            
        if (latestTaskStart.isPresent()) {
            currentTaskInfo.setStartTime(latestTaskStart.get().getReportTime());
        }
        
        detail.setCurrentTask(currentTaskInfo);
        detail.setLogs(logs);
        detail.setTaskHistory(taskHistory);
        detail.setConversations(conversations);
        
        return detail;
    }

    /**
     * 获取指定 Agent 的日志列表
     */
    public ApiResponse<TaskLogsResponseDTO> getAgentLogs(String agentId) {
        try {
            List<AgentLog> logs = agentLogRepository.findByAgentIdOrderByLogTimeDesc(agentId);
            
            List<TaskLogsResponseDTO.LogEntryDTO> logEntries = logs.stream()
                .map(log -> new TaskLogsResponseDTO.LogEntryDTO(
                    log.getLevel(),
                    log.getMessage(),
                    log.getLogTime().toString()
                ))
                .collect(Collectors.toList());
            
            TaskLogsResponseDTO response = new TaskLogsResponseDTO(logEntries, logEntries.size());
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取 Agent 日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定 Agent 的交互记录列表
     */
    public ApiResponse<InteractionsResponseDTO> getAgentInteractions(String agentId) {
        try {
            List<AgentInteraction> interactions = agentInteractionRepository.findByAgentIdOrderByInteractionTimeDesc(agentId);
            
            List<InteractionsResponseDTO.InteractionEntryDTO> interactionEntries = interactions.stream()
                .map(interaction -> new InteractionsResponseDTO.InteractionEntryDTO(
                    interaction.getRole(),
                    interaction.getContent(),
                    interaction.getInteractionTime().toString()
                ))
                .collect(Collectors.toList());
            
            InteractionsResponseDTO response = new InteractionsResponseDTO(interactionEntries, interactionEntries.size());
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取 Agent 交互记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取告警信息
     */
    public ApiResponse<AlertsResponseDTO> getAlerts() {
        try {
            // 创建模拟告警数据
            List<AlertItemDTO> alerts = new ArrayList<>();
            
            // 添加一些示例告警
            alerts.add(new AlertItemDTO(
                "alert_001",
                "CPU 使用率过高",
                "WARNING",
                "服务器 CPU 使用率达到 85%",
                LocalDateTime.now().minusMinutes(5),
                "server_001",
                false
            ));
            
            alerts.add(new AlertItemDTO(
                "alert_002",
                "内存不足警告",
                "CRITICAL",
                "可用内存低于 10%",
                LocalDateTime.now().minusMinutes(10),
                "server_002",
                true
            ));
            
            int criticalCount = (int) alerts.stream().filter(a -> "CRITICAL".equals(a.getLevel())).count();
            int warningCount = (int) alerts.stream().filter(a -> "WARNING".equals(a.getLevel())).count();
            int infoCount = (int) alerts.stream().filter(a -> "INFO".equals(a.getLevel())).count();
            
            AlertsResponseDTO response = new AlertsResponseDTO(
                alerts.size(),
                criticalCount,
                warningCount,
                infoCount,
                alerts,
                LocalDateTime.now()
            );
            
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取告警信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 报告 Agent 状态
     */
    @Transactional
    public void reportAgentStatus(Object request) {
        // 根据实际需求实现状态报告逻辑
        // 此处为占位符实现
    }

    /**
     * 清除指定 Agent 的所有数据（日志、交互记录、任务历史）
     */
    @Transactional
    public void clearAgentData(String agentId) {
        // 删除日志
        List<AgentLog> logs = agentLogRepository.findByAgentId(agentId);
        agentLogRepository.deleteAll(logs);
        
        // 删除交互记录
        List<AgentInteraction> interactions = agentInteractionRepository.findByAgentId(agentId);
        agentInteractionRepository.deleteAll(interactions);
        
        // 删除任务历史（上报记录）
        List<AgentReport> reports = agentReportRepository.findByAgentId(agentId);
        agentReportRepository.deleteAll(reports);
    }

    /**
     * 清除所有 Agent 的数据
     */
    @Transactional
    public void clearAllData() {
        agentLogRepository.deleteAll();
        agentInteractionRepository.deleteAll();
        agentReportRepository.deleteAll();
    }
}