package com.ceo.dashboard.dto;

import java.time.LocalDateTime;

/**
 * 告警项 DTO
 */
public class AlertItemDTO {
    
    private String id;
    private String title;
    private String level; // CRITICAL, WARNING, INFO
    private String message;
    private LocalDateTime timestamp;
    private String source;
    private Boolean acknowledged;

    // 构造函数
    public AlertItemDTO() {}

    public AlertItemDTO(String id, String title, String level, String message, 
                       LocalDateTime timestamp, String source, Boolean acknowledged) {
        this.id = id;
        this.title = title;
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
        this.source = source;
        this.acknowledged = acknowledged;
    }

    // Getter 和 Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}