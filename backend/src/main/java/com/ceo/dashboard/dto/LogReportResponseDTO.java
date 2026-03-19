package com.ceo.dashboard.dto;

/**
 * 日志上报响应 DTO
 */
public class LogReportResponseDTO {
    private boolean success;
    private String logId;

    public LogReportResponseDTO(boolean success, String logId) {
        this.success = success;
        this.logId = logId;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}