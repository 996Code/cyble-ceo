package com.ceo.dashboard.dto;

import java.time.LocalDateTime;

/**
 * 上报响应 DTO
 */
public class ReportResponseDTO {
    private Boolean reported;
    private LocalDateTime timestamp;

    public ReportResponseDTO() {
        this.reported = true;
        this.timestamp = LocalDateTime.now();
    }

    public ReportResponseDTO(Boolean reported, LocalDateTime timestamp) {
        this.reported = reported;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Boolean getReported() {
        return reported;
    }

    public void setReported(Boolean reported) {
        this.reported = reported;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}