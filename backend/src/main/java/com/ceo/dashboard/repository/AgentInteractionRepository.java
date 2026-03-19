package com.ceo.dashboard.repository;

import com.ceo.dashboard.model.AgentInteraction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent 交互记录 Repository
 */
@Repository
public interface AgentInteractionRepository extends JpaRepository<AgentInteraction, Long> {

    /**
     * 按 Agent ID 查询交互记录（按时间降序）
     */
    List<AgentInteraction> findByAgentIdOrderByInteractionTimeDesc(String agentId);

    /**
     * 按 Agent ID 查询交互记录（按时间降序，限制数量）
     */
    List<AgentInteraction> findByAgentIdOrderByInteractionTimeDesc(String agentId, Pageable pageable);

    /**
     * 按 Agent ID 查询交互记录
     */
    List<AgentInteraction> findByAgentId(String agentId);

    /**
     * 按 Agent ID 和任务 ID 查询交互记录（按时间降序）
     */
    List<AgentInteraction> findByAgentIdAndTaskIdOrderByInteractionTimeDesc(String agentId, String taskId);

    /**
     * 按 Agent ID 和任务 ID 查询交互记录（按时间降序，限制数量）
     */
    List<AgentInteraction> findByAgentIdAndTaskIdOrderByInteractionTimeDesc(String agentId, String taskId, Pageable pageable);

    /**
     * 按 Agent ID 和任务 ID 查询交互记录
     */
    List<AgentInteraction> findByAgentIdAndTaskId(String agentId, String taskId);

    /**
     * 按 Agent ID 和角色查询交互记录（按时间降序）
     */
    List<AgentInteraction> findByAgentIdAndRoleOrderByInteractionTimeDesc(String agentId, String role);

    /**
     * 按 Agent ID 和角色查询交互记录（按时间降序，限制数量）
     */
    List<AgentInteraction> findByAgentIdAndRoleOrderByInteractionTimeDesc(String agentId, String role, Pageable pageable);

    /**
     * 按 Agent ID 和角色查询交互记录
     */
    List<AgentInteraction> findByAgentIdAndRole(String agentId, String role);
}