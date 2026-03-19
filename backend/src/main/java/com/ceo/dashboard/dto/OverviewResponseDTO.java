package com.ceo.dashboard.dto;

import java.util.Map;

/**
 * 仪表板概览响应 DTO
 */
public class OverviewResponseDTO {
    
    private Long totalAgents;
    private Long activeAgents;
    private Long inactiveAgents;
    private Map<String, Object> summaryStats;
    private String lastUpdateTime;

    // 构造函数
    public OverviewResponseDTO() {}

    public OverviewResponseDTO(Long totalAgents, Long activeAgents, Long inactiveAgents, 
                              Map<String, Object> summaryStats, String lastUpdateTime) {
        this.totalAgents = totalAgents;
        this.activeAgents = activeAgents;
        this.inactiveAgents = inactiveAgents;
        this.summaryStats = summaryStats;
        this.lastUpdateTime = lastUpdateTime;
    }

    // Getter 和 Setter
    public Long getTotalAgents() {
        return totalAgents;
    }

    public void setTotalAgents(Long totalAgents) {
        this.totalAgents = totalAgents;
    }

    public Long getActiveAgents() {
        return activeAgents;
    }

    public void setActiveAgents(Long activeAgents) {
        this.activeAgents = activeAgents;
    }

    public Long getInactiveAgents() {
        return inactiveAgents;
    }

    public void setInactiveAgents(Long inactiveAgents) {
        this.inactiveAgents = inactiveAgents;
    }

    public Map<String, Object> getSummaryStats() {
        return summaryStats;
    }

    public void setSummaryStats(Map<String, Object> summaryStats) {
        this.summaryStats = summaryStats;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}