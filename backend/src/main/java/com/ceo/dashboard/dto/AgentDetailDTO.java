package com.ceo.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 详细信息 DTO
 */
public class AgentDetailDTO {
    
    private String id;
    private String name;
    private String status;
    private String ip;
    private String version;
    private LocalDateTime lastHeartbeat;
    private String location;
    private List<String> capabilities;
    private String currentTask;
    private String logs;

    // 构造函数
    public AgentDetailDTO() {}

    public AgentDetailDTO(String id, String name, String status, String ip, String version,
                         LocalDateTime lastHeartbeat, String location, List<String> capabilities, 
                         String currentTask, String logs) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.ip = ip;
        this.version = version;
        this.lastHeartbeat = lastHeartbeat;
        this.location = location;
        this.capabilities = capabilities;
        this.currentTask = currentTask;
        this.logs = logs;
    }

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

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}