package com.ceo.dashboard.dto;

import java.util.List;

/**
 * 交互记录响应 DTO
 */
public class InteractionsResponseDTO {
    
    private List<InteractionEntryDTO> interactions;
    private Integer total;

    public InteractionsResponseDTO(List<InteractionEntryDTO> interactions, Integer total) {
        this.interactions = interactions;
        this.total = total;
    }

    // Getters and Setters
    public List<InteractionEntryDTO> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<InteractionEntryDTO> interactions) {
        this.interactions = interactions;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * 交互条目 DTO
     */
    public static class InteractionEntryDTO {
        private String role;
        private String content;
        private String timestamp;

        public InteractionEntryDTO(String role, String content, String timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
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