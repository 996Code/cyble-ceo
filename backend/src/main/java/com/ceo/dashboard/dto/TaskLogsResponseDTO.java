package com.ceo.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务日志响应 DTO
 */
public class TaskLogsResponseDTO {
    
    private List<LogEntryDTO> logs;
    private Integer total;

    public TaskLogsResponseDTO(List<LogEntryDTO> logs, Integer total) {
        this.logs = logs;
        this.total = total;
    }

    // Getters and Setters
    public List<LogEntryDTO> getLogs() {
        return logs;
    }

    public void setLogs(List<LogEntryDTO> logs) {
        this.logs = logs;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * 日志条目 DTO
     */
    public static class LogEntryDTO {
        private String level;
        private String content;
        private String timestamp;

        public LogEntryDTO(String level, String content, String timestamp) {
            this.level = level;
            this.content = content;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}