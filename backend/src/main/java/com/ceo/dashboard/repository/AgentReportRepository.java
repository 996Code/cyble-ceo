package com.ceo.dashboard.repository;

import com.ceo.dashboard.model.AgentReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent 上报记录 Repository
 */
@Repository
public interface AgentReportRepository extends JpaRepository<AgentReport, Long> {

    /**
     * 按 Agent ID 查询上报记录（按时间降序）
     */
    List<AgentReport> findByAgentIdOrderByReportTimeDesc(String agentId);

    /**
     * 按 Agent ID 查询上报记录（按时间降序，限制数量）
     */
    List<AgentReport> findByAgentIdOrderByReportTimeDesc(String agentId, Pageable pageable);

    /**
     * 按 Agent ID 查询上报记录
     */
    List<AgentReport> findByAgentId(String agentId);

    /**
     * 按 Agent ID 和上报类型查询（按时间降序）
     */
    List<AgentReport> findByAgentIdAndReportTypeOrderByReportTimeDesc(String agentId, AgentReport.ReportType reportType);

    /**
     * 按 Agent ID 和上报类型查询（按时间降序，限制数量）
     */
    List<AgentReport> findByAgentIdAndReportTypeOrderByReportTimeDesc(String agentId, AgentReport.ReportType reportType, Pageable pageable);

    /**
     * 按 Agent ID 和上报类型查询
     */
    List<AgentReport> findByAgentIdAndReportType(String agentId, AgentReport.ReportType reportType);

    /**
     * 按任务名查询上报记录
     */
    List<AgentReport> findByTaskName(String taskName);
}