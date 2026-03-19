package com.ceo.dashboard.repository;

import com.ceo.dashboard.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByStatus(@Param("status") String status);

    @Query("SELECT t FROM Task t WHERE (:status IS NULL OR t.status = :status) ORDER BY t.updatedAt DESC")
    List<Task> findByStatusOrAll(@Param("status") String status);
    
    @Query("SELECT t FROM Task t ORDER BY t.updatedAt DESC")
    List<Task> findAllOrderByUpdatedAtDesc();
    
    List<Task> findByAssigneeOrderByUpdatedAtDesc(@Param("assignee") String assignee);
    
    @Query("SELECT t FROM Task t WHERE t.sessionKey = :sessionKey ORDER BY t.createdAt ASC")
    List<Task> findBySessionKey(@Param("sessionKey") String sessionKey);
    
    // 添加归档相关的查询方法
    @Query("SELECT t FROM Task t WHERE t.archived = :archived ORDER BY t.updatedAt DESC")
    List<Task> findByArchived(@Param("archived") Boolean archived);
    
    @Query("SELECT t FROM Task t WHERE t.archived = :archived AND (:status IS NULL OR t.status = :status) ORDER BY t.updatedAt DESC")
    List<Task> findByArchivedAndStatus(@Param("archived") Boolean archived, @Param("status") String status);
    
    @Query("SELECT t FROM Task t WHERE t.archived = false AND (:status IS NULL OR t.status = :status) AND t.createdAt >= :cutoffDate ORDER BY t.updatedAt DESC")
    List<Task> findByArchivedFalseAndStatusAndCreatedAtAfter(@Param("status") String status, @Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}