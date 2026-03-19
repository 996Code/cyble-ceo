package com.ceo.dashboard.dto;

/**
 * 任务完成 DTO
 */
public class TaskCompleteDTO {
    private String result;
    private String feedback;
    private String operator;

    // Getters and Setters
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}