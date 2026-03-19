package com.ceo.dashboard.dto;

/**
 * 心跳上报响应 DTO
 */
public class HeartbeatReportResponseDTO {
    private boolean success;
    private String heartbeatId;

    public HeartbeatReportResponseDTO(boolean success, String heartbeatId) {
        this.success = success;
        this.heartbeatId = heartbeatId;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getHeartbeatId() {
        return heartbeatId;
    }

    public void setHeartbeatId(String heartbeatId) {
        this.heartbeatId = heartbeatId;
    }
}