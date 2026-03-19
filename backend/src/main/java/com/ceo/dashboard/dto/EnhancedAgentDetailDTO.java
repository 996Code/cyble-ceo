package com.ceo.dashboard.dto;

import com.ceo.dashboard.model.AgentLog;
import com.ceo.dashboard.model.AgentReport;
import com.ceo.dashboard.model.AgentInteraction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 增强版 Agent 详细信息 DTO
 */
public class EnhancedAgentDetailDTO {
    
    private String id;
    private String name;
    private String status;
    private String ip;
    private String version;
    private LocalDateTime lastHeartbeat;
    private String location;
    private List<String> capabilities;
    private CurrentTaskInfo currentTask;
    private List<AgentLog> logs;
    private List<AgentReport> taskHistory;
    private List<AgentInteraction> conversations;

    // 构造函数
    public EnhancedAgentDetailDTO() {}

    // Getter 和 Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public CurrentTaskInfo getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(CurrentTaskInfo currentTask) {
        this.currentTask = currentTask;
    }

    public List<AgentLog> getLogs() {
        return logs;
    }

    public void setLogs(List<AgentLog> logs) {
        this.logs = logs;
    }

    public List<AgentReport> getTaskHistory() {
        return taskHistory;
    }

    public void setTaskHistory(List<AgentReport> taskHistory) {
        this.taskHistory = taskHistory;
    }

    public List<AgentInteraction> getConversations() {
        return conversations;
    }

    public void setConversations(List<AgentInteraction> conversations) {
        this.conversations = conversations;
    }

    /**
     * 当前任务信息内部类
     */
    public static class CurrentTaskInfo {
        private String name;
        private Integer progress;
        private LocalDateTime startTime;

        public CurrentTaskInfo() {}

        public CurrentTaskInfo(String name, Integer progress, LocalDateTime startTime) {
            this.name = name;
            this.progress = progress;
            this.startTime = startTime;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }
    }
}