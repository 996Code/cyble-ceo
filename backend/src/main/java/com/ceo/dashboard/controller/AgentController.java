package com.ceo.dashboard.controller;

import com.ceo.dashboard.dto.*;
import com.ceo.dashboard.model.AgentStatus;
import com.ceo.dashboard.repository.AgentStatusRepository;
import com.ceo.dashboard.service.AgentReportService;
import com.ceo.dashboard.service.AgentStatusSyncService;
import com.ceo.dashboard.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Agent 状态控制器
 */
@RestController
@RequestMapping("/api/v1")
public class AgentController {

    @Autowired
    private AgentStatusRepository agentStatusRepository;

    @Autowired
    private AgentStatusSyncService agentStatusSyncService;

    @Autowired
    private AgentReportService agentReportService;

    /**
     * 获取所有 Agent 状态
     */
    @GetMapping("/agents")
    public ResponseEntity<List<AgentStatus>> getAllAgents() {
        List<AgentStatus> agents = agentStatusRepository.findAll();
        return ResponseEntity.ok(agents);
    }

    /**
     * 获取特定 Agent 状态
     */
    @GetMapping("/agents/{agentId}")
    public ResponseEntity<AgentStatus> getAgent(@PathVariable String agentId) {
        Optional<AgentStatus> agent = agentStatusRepository.findById(agentId);
        if (agent.isPresent()) {
            return ResponseEntity.ok(agent.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 手动触发 Agent 状态同步
     */
    @PostMapping("/agents/sync")
    public ResponseEntity<String> syncAgents() {
        agentStatusSyncService.syncNow();
        return ResponseEntity.ok("Agent 状态同步已触发");
    }

    /**
     * 任务开始上报
     */
    @PostMapping("/agents/report/task-start")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> reportTaskStart(@RequestBody TaskStartReportDTO request) {
        try {
            ReportResponseDTO response = agentReportService.reportTaskStart(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }

    /**
     * 进展上报
     */
    @PostMapping("/agents/report/progress")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> reportProgress(@RequestBody ProgressReportDTO request) {
        try {
            ReportResponseDTO response = agentReportService.reportProgress(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }

    /**
     * 任务完成上报
     */
    @PostMapping("/agents/report/task-complete")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> reportTaskComplete(@RequestBody TaskCompleteReportDTO request) {
        try {
            ReportResponseDTO response = agentReportService.reportTaskComplete(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }

    /**
     * 阻塞上报
     */
    @PostMapping("/agents/report/block")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> reportBlock(@RequestBody BlockReportDTO request) {
        try {
            ReportResponseDTO response = agentReportService.reportBlock(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }

    /**
     * 日志上报
     */
    @PostMapping("/agents/report/log")
    public ResponseEntity<ApiResponse<LogReportResponseDTO>> reportLog(@RequestBody LogReportDTO request) {
        try {
            LogReportResponseDTO response = agentReportService.reportLog(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }

    /**
     * 交互记录上报
     */
    @PostMapping("/agents/report/interaction")
    public ResponseEntity<ApiResponse<InteractionReportResponseDTO>> reportInteraction(@RequestBody InteractionReportDTO request) {
        try {
            InteractionReportResponseDTO response = agentReportService.reportInteraction(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }

    /**
     * 心跳上报
     */
    @PostMapping("/agents/report/heartbeat")
    public ResponseEntity<ApiResponse<HeartbeatReportResponseDTO>> reportHeartbeat(@RequestBody HeartbeatReportDTO request) {
        try {
            HeartbeatReportResponseDTO response = agentReportService.reportHeartbeat(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }

    /**
     * 错误上报
     */
    @PostMapping("/agents/report/error")
    public ResponseEntity<ApiResponse<ErrorReportResponseDTO>> reportError(@RequestBody ErrorReportDTO request) {
        try {
            ErrorReportResponseDTO response = agentReportService.reportError(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        }
    }
}