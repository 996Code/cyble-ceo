package com.ceo.dashboard.repository;

import com.ceo.dashboard.model.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent 状态 Repository
 */
@Repository
public interface AgentStatusRepository extends JpaRepository<AgentStatus, String> {

    /**
     * 按状态查询
     */
    List<AgentStatus> findByStatus(AgentStatus.Status status);

    /**
     * 查询所有活跃的 Agent
     */
    List<AgentStatus> findByStatusIn(List<AgentStatus.Status> statuses);
}
