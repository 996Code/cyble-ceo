package com.ceo.dashboard.dto;

/**
 * 交互记录上报响应 DTO
 */
public class InteractionReportResponseDTO {
    private boolean success;
    private String interactionId;

    public InteractionReportResponseDTO(boolean success, String interactionId) {
        this.success = success;
        this.interactionId = interactionId;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(String interactionId) {
        this.interactionId = interactionId;
    }
}