package com.ceo.dashboard.dto;

/**
 * 进度报告 DTO
 */
public class ProgressDTO {
    private Integer percentage;
    private String description;
    private String operator;

    // Getters and Setters
    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}