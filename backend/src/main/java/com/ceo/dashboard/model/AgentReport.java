package com.ceo.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Agent 上报记录实体
 */
@Entity
@Table(name = "agent_reports")
public class AgentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String agentId;

    @Enumerated(EnumType.STRING)
    private ReportType reportType; // TASK_START, PROGRESS, TASK_COMPLETE, BLOCK

    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String content; // 存储具体的上报内容

    private Integer progress;

    private LocalDateTime reportTime;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 枚举类型定义
    public enum ReportType {
        TASK_START,    // 任务开始
        PROGRESS,      // 进展上报
        TASK_COMPLETE, // 任务完成
        BLOCK          // 阻塞上报
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        reportTime = LocalDateTime.now();
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

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}