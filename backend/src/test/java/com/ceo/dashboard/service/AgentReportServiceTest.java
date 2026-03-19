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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AgentReportServiceTest {

    @Mock
    private AgentStatusRepository agentStatusRepository;

    @Mock
    private AgentReportRepository agentReportRepository;

    @Mock
    private AgentLogRepository agentLogRepository;

    @Mock
    private AgentInteractionRepository agentInteractionRepository;

    @InjectMocks
    private AgentReportService agentReportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReportLog() {
        // 准备测试数据
        LogReportDTO request = new LogReportDTO();
        request.setAgentId("test-agent");
        request.setTaskId("task-001");
        request.setLevel("INFO");
        request.setMessage("Test log message");

        AgentStatus agentStatus = new AgentStatus();
        agentStatus.setAgentId("test-agent");
        when(agentStatusRepository.findById("test-agent")).thenReturn(java.util.Optional.of(agentStatus));
        when(agentStatusRepository.save(any(AgentStatus.class))).thenReturn(agentStatus);

        AgentLog savedLog = new AgentLog();
        savedLog.setId(1L);
        when(agentLogRepository.save(any(AgentLog.class))).thenReturn(savedLog);

        // 执行
        LogReportResponseDTO response = agentReportService.reportLog(request);

        // 验证
        assertTrue(response.isSuccess());
        assertEquals("1", response.getLogId());
        verify(agentStatusRepository, times(1)).save(any(AgentStatus.class));
        verify(agentLogRepository, times(1)).save(any(AgentLog.class));
    }

    @Test
    void testReportInteraction() {
        // 准备测试数据
        InteractionReportDTO request = new InteractionReportDTO();
        request.setAgentId("test-agent");
        request.setTaskId("task-001");
        request.setRole("agent");
        request.setContent("Test interaction message");

        AgentStatus agentStatus = new AgentStatus();
        agentStatus.setAgentId("test-agent");
        when(agentStatusRepository.findById("test-agent")).thenReturn(java.util.Optional.of(agentStatus));
        when(agentStatusRepository.save(any(AgentStatus.class))).thenReturn(agentStatus);

        AgentInteraction savedInteraction = new AgentInteraction();
        savedInteraction.setId(1L);
        when(agentInteractionRepository.save(any(AgentInteraction.class))).thenReturn(savedInteraction);

        // 执行
        InteractionReportResponseDTO response = agentReportService.reportInteraction(request);

        // 验证
        assertTrue(response.isSuccess());
        assertEquals("1", response.getInteractionId());
        verify(agentStatusRepository, times(1)).save(any(AgentStatus.class));
        verify(agentInteractionRepository, times(1)).save(any(AgentInteraction.class));
    }

    @Test
    void testReportHeartbeat() {
        // 准备测试数据
        HeartbeatReportDTO request = new HeartbeatReportDTO();
        request.setAgentId("test-agent");
        request.setStatus("BUSY");

        AgentStatus agentStatus = new AgentStatus();
        agentStatus.setAgentId("test-agent");
        when(agentStatusRepository.findById("test-agent")).thenReturn(java.util.Optional.of(agentStatus));
        when(agentStatusRepository.save(any(AgentStatus.class))).thenReturn(agentStatus);

        // 执行
        HeartbeatReportResponseDTO response = agentReportService.reportHeartbeat(request);

        // 验证
        assertTrue(response.isSuccess());
        assertEquals("test-agent", response.getHeartbeatId());
        verify(agentStatusRepository, times(1)).save(any(AgentStatus.class));
    }

    @Test
    void testReportError() {
        // 准备测试数据
        ErrorReportDTO request = new ErrorReportDTO();
        request.setAgentId("test-agent");
        request.setTaskId("task-001");
        request.setErrorType("NullPointerException");
        request.setErrorMessage("Test error message");
        request.setStackTrace("Stack trace here");

        AgentStatus agentStatus = new AgentStatus();
        agentStatus.setAgentId("test-agent");
        when(agentStatusRepository.findById("test-agent")).thenReturn(java.util.Optional.of(agentStatus));
        when(agentStatusRepository.save(any(AgentStatus.class))).thenReturn(agentStatus);

        AgentReport savedReport = new AgentReport();
        savedReport.setId(1L);
        when(agentReportRepository.save(any(AgentReport.class))).thenReturn(savedReport);

        // 执行
        ErrorReportResponseDTO response = agentReportService.reportError(request);

        // 验证
        assertTrue(response.isSuccess());
        assertEquals("1", response.getErrorId());
        verify(agentStatusRepository, times(1)).save(any(AgentStatus.class));
        verify(agentReportRepository, times(1)).save(any(AgentReport.class));
    }
}