package com.ceo.dashboard.repository;

import com.ceo.dashboard.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    List<SubTask> findByTaskId(String taskId);
    List<SubTask> findByTaskIdAndSeq(String taskId, Integer seq);
}