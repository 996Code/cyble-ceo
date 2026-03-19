package com.ceo.dashboard.dto;

/**
 * 错误上报响应 DTO
 */
public class ErrorReportResponseDTO {
    private boolean success;
    private String errorId;

    public ErrorReportResponseDTO(boolean success, String errorId) {
        this.success = success;
        this.errorId = errorId;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }
}