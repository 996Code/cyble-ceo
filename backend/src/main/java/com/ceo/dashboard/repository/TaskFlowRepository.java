package com.ceo.dashboard.repository;

import com.ceo.dashboard.entity.TaskFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskFlowRepository extends JpaRepository<TaskFlow, Long> {
    List<TaskFlow> findByTaskId(String taskId);
}