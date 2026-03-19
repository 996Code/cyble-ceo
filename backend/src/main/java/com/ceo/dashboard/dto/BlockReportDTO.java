package com.ceo.dashboard.dto;

/**
 * 阻塞上报 DTO
 */
public class BlockReportDTO {
    private String agentId;
    private String reason;
    private String expectedResolve;
    private String timestamp;

    // Getters and Setters
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExpectedResolve() {
        return expectedResolve;
    }

    public void setExpectedResolve(String expectedResolve) {
        this.expectedResolve = expectedResolve;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}