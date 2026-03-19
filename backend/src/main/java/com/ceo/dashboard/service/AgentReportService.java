package com.ceo.dashboard.service;

import com.ceo.dashboard.dto.*;
import com.ceo.dashboard.model.AgentLog;
import com.ceo.dashboard.model.AgentInteraction;
import com.ceo.dashboard.model.AgentReport;
import com.ceo.dashboard.model.AgentStatus;
import com.ceo.dashboard.repository.AgentLogRepository;
import com.ceo.dashboard.repository.AgentInteractionRepository;
import com.ceo.dashboard.repository.AgentReportRepository;
import com.ceo.dashboard.repository.AgentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Agent 上报服务
 */
@Service
@Transactional
public class AgentReportService {

    @Autowired
    private AgentStatusRepository agentStatusRepository;

    @Autowired
    private AgentReportRepository agentReportRepository;

    @Autowired
    private AgentLogRepository agentLogRepository;

    @Autowired
    private AgentInteractionRepository agentInteractionRepository;

    @Value("${openclaw.workspace.base:/openclaw}")
    private String workspaceBasePath;

    // 常量定义
    private static final String AGENT_CREATOR = "ceo";
    private static final String DEFAULT_WORKSPACE_BASE = "/openclaw";

    /**
     * 任务开始上报
     */
    public ReportResponseDTO reportTaskStart(TaskStartReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setWorkspace(request.getWorkspace());
        agentStatus.setCurrentTaskName(request.getTaskName());
        agentStatus.setCurrentTaskProgress(0);
        agentStatus.setStatus(AgentStatus.Status.BUSY);
        // 设置最后活跃时间为当前时间
        agentStatus.setLastActive(LocalDateTime.now());
        
        agentStatusRepository.save(agentStatus);

        // 保存上报记录
        AgentReport report = new AgentReport();
        report.setAgentId(request.getAgentId());
        report.setReportType(AgentReport.ReportType.TASK_START);
        report.setTaskName(request.getTaskName());
        report.setContent("Task started: " + request.getTaskName());
        // 确保任务开始时间被正确设置
        report.setReportTime(parseTimeString(request.getStartTime()));
        
        agentReportRepository.save(report);

        return new ReportResponseDTO(true, report.getReportTime());
    }

    /**
     * 进展上报
     */
    public ReportResponseDTO reportProgress(ProgressReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setCurrentTaskName(request.getCurrent());
        agentStatus.setCurrentTaskProgress(request.getProgress());
        agentStatus.setLastActive(LocalDateTime.now());
        
        agentStatusRepository.save(agentStatus);

        // 保存上报记录
        AgentReport report = new AgentReport();
        report.setAgentId(request.getAgentId());
        report.setReportType(AgentReport.ReportType.PROGRESS);
        report.setTaskName(agentStatus.getCurrentTaskName());
        report.setContent("Current: " + request.getCurrent() + ", Plan: " + request.getPlan());
        report.setProgress(request.getProgress());
        report.setReportTime(parseTimeString(request.getTimestamp()));
        
        agentReportRepository.save(report);

        return new ReportResponseDTO(true, report.getReportTime());
    }

    /**
     * 任务完成上报
     */
    public ReportResponseDTO reportTaskComplete(TaskCompleteReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setCurrentTaskName(null);
        agentStatus.setCurrentTaskProgress(null);
        agentStatus.setStatus(AgentStatus.Status.IDLE);
        agentStatus.setLastActive(LocalDateTime.now());
        
        agentStatusRepository.save(agentStatus);

        // 保存上报记录
        AgentReport report = new AgentReport();
        report.setAgentId(request.getAgentId());
        report.setReportType(AgentReport.ReportType.TASK_COMPLETE);
        report.setTaskName(request.getTaskName());
        report.setContent("Task completed: " + request.getTaskName() + ", Summary: " + request.getSummary() + 
                         ", Output: " + request.getOutput());
        report.setReportTime(parseTimeString(request.getEndTime()));
        
        agentReportRepository.save(report);

        return new ReportResponseDTO(true, report.getReportTime());
    }

    /**
     * 阻塞上报
     */
    public ReportResponseDTO reportBlock(BlockReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setStatus(AgentStatus.Status.ERROR);
        agentStatus.setLastActive(LocalDateTime.now());
        
        agentStatusRepository.save(agentStatus);

        // 保存上报记录
        AgentReport report = new AgentReport();
        report.setAgentId(request.getAgentId());
        report.setReportType(AgentReport.ReportType.BLOCK);
        report.setContent("Blocked: " + request.getReason() + ", Expected resolve: " + request.getExpectedResolve());
        report.setReportTime(parseTimeString(request.getTimestamp()));
        
        agentReportRepository.save(report);

        return new ReportResponseDTO(true, report.getReportTime());
    }

    /**
     * 日志上报
     */
    public LogReportResponseDTO reportLog(LogReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setLastActive(LocalDateTime.now());
        agentStatusRepository.save(agentStatus);

        // 保存日志记录
        AgentLog log = new AgentLog();
        log.setAgentId(request.getAgentId());
        log.setTaskId(request.getTaskId());
        log.setLevel(request.getLevel());
        log.setMessage(request.getMessage());
        // 确保即使时间戳为空也设置当前时间
        log.setLogTime(parseTimeString(request.getTimestamp()));

        AgentLog savedLog = agentLogRepository.save(log);

        return new LogReportResponseDTO(true, String.valueOf(savedLog.getId()));
    }

    /**
     * 交互记录上报
     */
    public InteractionReportResponseDTO reportInteraction(InteractionReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setLastActive(LocalDateTime.now());
        agentStatusRepository.save(agentStatus);

        // 保存交互记录
        AgentInteraction interaction = new AgentInteraction();
        interaction.setAgentId(request.getAgentId());
        interaction.setTaskId(request.getTaskId());
        interaction.setRole(request.getRole());
        interaction.setContent(request.getContent());
        interaction.setInteractionTime(parseTimeString(request.getTimestamp()));

        AgentInteraction savedInteraction = agentInteractionRepository.save(interaction);

        return new InteractionReportResponseDTO(true, String.valueOf(savedInteraction.getId()));
    }

    /**
     * 心跳上报
     */
    public HeartbeatReportResponseDTO reportHeartbeat(HeartbeatReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setLastActive(LocalDateTime.now());
        
        // 如果提供了状态信息，也更新状态
        if (request.getStatus() != null) {
            try {
                AgentStatus.Status status = AgentStatus.Status.valueOf(request.getStatus());
                agentStatus.setStatus(status);
            } catch (IllegalArgumentException e) {
                // 如果状态无效，则不更新状态，只更新最后活跃时间
            }
        }
        
        agentStatusRepository.save(agentStatus);

        // 返回心跳响应
        return new HeartbeatReportResponseDTO(true, request.getAgentId());
    }

    /**
     * 错误上报
     */
    public ErrorReportResponseDTO reportError(ErrorReportDTO request) {
        validateRequest(request.getAgentId());

        // 更新 Agent 状态
        AgentStatus agentStatus = getOrCreateAgentStatus(request.getAgentId());
        agentStatus.setStatus(AgentStatus.Status.ERROR);
        agentStatus.setLastActive(LocalDateTime.now());
        agentStatusRepository.save(agentStatus);

        // 保存上报记录
        AgentReport report = new AgentReport();
        report.setAgentId(request.getAgentId());
        report.setReportType(AgentReport.ReportType.BLOCK);
        report.setTaskName(request.getTaskId());
        report.setContent("Error: " + request.getErrorType() + " - " + request.getErrorMessage() + 
                         ", Stack trace: " + request.getStackTrace());
        report.setReportTime(parseTimeString(request.getTimestamp()));

        AgentReport savedReport = agentReportRepository.save(report);

        return new ErrorReportResponseDTO(true, String.valueOf(savedReport.getId()));
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("agentId is required");
        }
    }

    /**
     * 获取或创建 Agent 状态
     */
    private AgentStatus getOrCreateAgentStatus(String agentId) {
        return agentStatusRepository.findById(agentId)
                .orElse(createNewAgentStatus(agentId));
    }

    /**
     * 创建新的 Agent 状态
     */
    private AgentStatus createNewAgentStatus(String agentId) {
        AgentStatus agentStatus = new AgentStatus();
        agentStatus.setAgentId(agentId);
        agentStatus.setWorkspace(workspaceBasePath + "-" + agentId);
        agentStatus.setStatus(AgentStatus.Status.IDLE);
        agentStatus.setLastActive(LocalDateTime.now());
        return agentStatus;
    }

    /**
     * 解析时间字符串
     */
    private LocalDateTime parseTimeString(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // 尝试解析不同格式的时间字符串
            if (timeString.contains("T")) {
                return LocalDateTime.parse(timeString);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(timeString, formatter);
            }
        } catch (Exception e) {
            // 如果解析失败，返回当前时间
            return LocalDateTime.now();
        }
    }
}