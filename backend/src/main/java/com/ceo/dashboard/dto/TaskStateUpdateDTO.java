package com.ceo.dashboard.dto;

/**
 * 任务状态更新 DTO
 */
public class TaskStateUpdateDTO {
    private String state;
    private String remark;

    // Getters and Setters
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}