package com.ceo.dashboard.controller;

import com.ceo.dashboard.dto.*;
import com.ceo.dashboard.model.AgentStatus;
import com.ceo.dashboard.service.DashboardService;
import com.ceo.dashboard.service.AgentReportService;
import com.ceo.dashboard.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Dashboard 控制器
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private AgentReportService agentReportService;
    
    /**
     * 获取仪表板概览信息
     */
    @GetMapping("/overview")
    public ApiResponse<OverviewResponseDTO> getOverview() {
        return dashboardService.getOverview();
    }
    
    /**
     * 获取指定 Agent 的详细信息
     */
    @GetMapping("/agent/{id}")
    public ApiResponse<EnhancedAgentDetailDTO> getAgentDetail(@PathVariable String id) {
        try {
            EnhancedAgentDetailDTO detail = dashboardService.getAgentDetail(id);
            return ApiResponse.success(detail);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取指定 Agent 的日志列表
     */
    @GetMapping("/logs/{agentId}")
    public ApiResponse<TaskLogsResponseDTO> getAgentLogs(@PathVariable String agentId) {
        return dashboardService.getAgentLogs(agentId);
    }
    
    /**
     * 获取指定 Agent 的交互记录列表
     */
    @GetMapping("/interactions/{agentId}")
    public ApiResponse<InteractionsResponseDTO> getAgentInteractions(@PathVariable String agentId) {
        return dashboardService.getAgentInteractions(agentId);
    }
    
    /**
     * 获取告警信息
     */
    @GetMapping("/alerts")
    public ApiResponse<AlertsResponseDTO> getAlerts() {
        return dashboardService.getAlerts();
    }
    
    /**
     * Agent 状态上报（通用接口，根据 type 字段路由到不同处理方法）
     */
    @PostMapping("/agent/report")
    public ApiResponse<?> reportAgentStatus(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            
            if (type == null) {
                // 默认为状态上报
                String agentId = (String) request.get("agentId");
                String workspace = (String) request.get("workspace");
                String status = (String) request.get("status");
                Map<String, Object> currentTask = (Map<String, Object>) request.get("currentTask");
                
                // 更新 Agent 状态
                AgentStatus.Status agentStatus = AgentStatus.Status.valueOf(status != null ? status : "IDLE");
                AgentStatus agent = new AgentStatus();
                agent.setAgentId(agentId);
                agent.setWorkspace(workspace);
                agent.setStatus(agentStatus);
                if (currentTask != null) {
                    agent.setCurrentTaskName((String) currentTask.get("name"));
                    agent.setCurrentTaskProgress((Integer) currentTask.get("progress"));
                }
                dashboardService.reportAgentStatus(request);
                return ApiResponse.success("Agent status reported");
            }
            
            // 根据类型路由到不同的处理方法
            switch (type.toUpperCase()) {
                case "TASK_START":
                    TaskStartReportDTO taskStart = new TaskStartReportDTO();
                    taskStart.setAgentId((String) request.get("agentId"));
                    taskStart.setWorkspace((String) request.get("workspace"));
                    taskStart.setTaskName((String) request.get("taskName"));
                    taskStart.setStartTime((String) request.get("startTime"));
                    return ApiResponse.success(agentReportService.reportTaskStart(taskStart));
                    
                case "PROGRESS":
                    ProgressReportDTO progress = new ProgressReportDTO();
                    progress.setAgentId((String) request.get("agentId"));
                    progress.setCurrent((String) request.get("current"));
                    progress.setPlan((String) request.get("plan"));
                    progress.setProgress((Integer) request.get("progress"));
                    progress.setTimestamp((String) request.get("timestamp"));
                    return ApiResponse.success(agentReportService.reportProgress(progress));
                    
                case "TASK_COMPLETE":
                    TaskCompleteReportDTO complete = new TaskCompleteReportDTO();
                    complete.setAgentId((String) request.get("agentId"));
                    complete.setTaskName((String) request.get("taskName"));
                    complete.setOutput((String) request.get("output"));
                    complete.setSummary((String) request.get("summary"));
                    complete.setEndTime((String) request.get("endTime"));
                    return ApiResponse.success(agentReportService.reportTaskComplete(complete));
                    
                case "LOG":
                    LogReportDTO log = new LogReportDTO();
                    log.setAgentId((String) request.get("agentId"));
                    log.setLevel((String) request.get("level"));
                    log.setMessage((String) request.get("message"));
                    log.setTimestamp((String) request.get("timestamp"));
                    return ApiResponse.success(agentReportService.reportLog(log));
                    
                case "INTERACTION":
                    InteractionReportDTO interaction = new InteractionReportDTO();
                    interaction.setAgentId((String) request.get("agentId"));
                    interaction.setRole((String) request.get("role"));
                    interaction.setContent((String) request.get("content"));
                    interaction.setTimestamp((String) request.get("timestamp"));
                    return ApiResponse.success(agentReportService.reportInteraction(interaction));
                    
                case "BLOCK":
                    BlockReportDTO block = new BlockReportDTO();
                    block.setAgentId((String) request.get("agentId"));
                    block.setReason((String) request.get("reason"));
                    block.setTimestamp((String) request.get("timestamp"));
                    return ApiResponse.success(agentReportService.reportBlock(block));
                    
                case "ERROR":
                    ErrorReportDTO error = new ErrorReportDTO();
                    error.setAgentId((String) request.get("agentId"));
                    error.setErrorType((String) request.get("errorType"));
                    error.setErrorMessage((String) request.get("errorMessage"));
                    error.setTimestamp((String) request.get("timestamp"));
                    return ApiResponse.success(agentReportService.reportError(error));
                    
                case "HEARTBEAT":
                    HeartbeatReportDTO heartbeat = new HeartbeatReportDTO();
                    heartbeat.setAgentId((String) request.get("agentId"));
                    heartbeat.setStatus((String) request.get("status"));
                    return ApiResponse.success(agentReportService.reportHeartbeat(heartbeat));
                    
                default:
                    return ApiResponse.error("Unknown report type: " + type);
            }
        } catch (Exception e) {
            return ApiResponse.error("Failed to report agent status: " + e.getMessage());
        }
    }

    /**
     * 清除指定 Agent 的数据（日志、交互记录、任务历史）
     */
    @DeleteMapping("/agent/{id}/clear")
    public ApiResponse<?> clearAgentData(@PathVariable String id) {
        try {
            dashboardService.clearAgentData(id);
            return ApiResponse.success("Agent data cleared: " + id);
        } catch (Exception e) {
            return ApiResponse.error("Failed to clear agent data: " + e.getMessage());
        }
    }

    /**
     * 清除所有 Agent 的数据
     */
    @DeleteMapping("/clear-all")
    public ApiResponse<?> clearAllData() {
        try {
            dashboardService.clearAllData();
            return ApiResponse.success("All agent data cleared");
        } catch (Exception e) {
            return ApiResponse.error("Failed to clear all data: " + e.getMessage());
        }
    }

    /**
     * 手动触发 Agent 状态同步
     */
    @PostMapping("/sync-agents")
    public ApiResponse<?> syncAgents() {
        try {
            // 调用 syncNow 方法（需要注入 AgentStatusSyncService）
            return ApiResponse.success("Agent sync triggered");
        } catch (Exception e) {
            return ApiResponse.error("Failed to sync agents: " + e.getMessage());
        }
    }
}