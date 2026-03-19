package com.ceo.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Agent 交互记录实体
 */
@Entity
@Table(name = "agent_interactions")
public class AgentInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String agentId;

    private String taskId;

    private String role;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime interactionTime;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        interactionTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getInteractionTime() {
        return interactionTime;
    }

    public void setInteractionTime(LocalDateTime interactionTime) {
        this.interactionTime = interactionTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}