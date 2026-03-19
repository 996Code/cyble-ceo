package com.ceo.dashboard.dto;

/**
 * 流程记录 DTO
 */
public class FlowRecordDTO {
    private String content;
    private String operator;
    private Long timestamp;

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}