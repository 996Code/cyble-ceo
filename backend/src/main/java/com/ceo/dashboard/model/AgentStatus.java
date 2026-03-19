package com.ceo.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Agent 状态实体
 */
@Entity
@Table(name = "agent_status")
public class AgentStatus {

    @Id
    private String agentId;

    private String workspace;

    @Enumerated(EnumType.STRING)
    private Status status = Status.IDLE;

    private String currentTaskName;

    private Integer currentTaskProgress;

    private LocalDateTime lastActive;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum Status {
        IDLE,    // 空闲
        BUSY,    // 忙碌
        ERROR    // 错误/阻塞
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCurrentTaskName() {
        return currentTaskName;
    }

    public void setCurrentTaskName(String currentTaskName) {
        this.currentTaskName = currentTaskName;
    }

    public Integer getCurrentTaskProgress() {
        return currentTaskProgress;
    }

    public void setCurrentTaskProgress(Integer currentTaskProgress) {
        this.currentTaskProgress = currentTaskProgress;
    }

    public LocalDateTime getLastActive() {
        return lastActive;
    }

    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
