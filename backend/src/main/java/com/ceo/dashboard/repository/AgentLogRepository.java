package com.ceo.dashboard.repository;

import com.ceo.dashboard.model.AgentLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 日志 Repository
 */
@Repository
public interface AgentLogRepository extends JpaRepository<AgentLog, Long> {

    /**
     * 按 Agent ID 查询日志（按时间降序）
     */
    List<AgentLog> findByAgentIdOrderByLogTimeDesc(String agentId);

    /**
     * 按 Agent ID 查询日志（按时间降序，限制数量）
     */
    List<AgentLog> findByAgentIdOrderByLogTimeDesc(String agentId, Pageable pageable);

    /**
     * 按 Agent ID 查询日志
     */
    List<AgentLog> findByAgentId(String agentId);

    /**
     * 按 Agent ID 和任务 ID 查询日志（按时间降序）
     */
    List<AgentLog> findByAgentIdAndTaskIdOrderByLogTimeDesc(String agentId, String taskId);

    /**
     * 按 Agent ID 和任务 ID 查询日志（按时间降序，限制数量）
     */
    List<AgentLog> findByAgentIdAndTaskIdOrderByLogTimeDesc(String agentId, String taskId, Pageable pageable);

    /**
     * 按 Agent ID 和任务 ID 查询日志
     */
    List<AgentLog> findByAgentIdAndTaskId(String agentId, String taskId);

    /**
     * 按 Agent ID 和日志级别查询（按时间降序）
     */
    List<AgentLog> findByAgentIdAndLevelOrderByLogTimeDesc(String agentId, String level);

    /**
     * 按 Agent ID 和日志级别查询
     */
    List<AgentLog> findByAgentIdAndLevel(String agentId, String level);
}