package com.ceo.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警信息响应 DTO
 */
public class AlertsResponseDTO {
    
    private Integer totalAlerts;
    private Integer criticalAlerts;
    private Integer warningAlerts;
    private Integer infoAlerts;
    private List<AlertItemDTO> alerts;
    private LocalDateTime lastUpdated;

    // 构造函数
    public AlertsResponseDTO() {}

    public AlertsResponseDTO(Integer totalAlerts, Integer criticalAlerts, Integer warningAlerts, 
                            Integer infoAlerts, List<AlertItemDTO> alerts, LocalDateTime lastUpdated) {
        this.totalAlerts = totalAlerts;
        this.criticalAlerts = criticalAlerts;
        this.warningAlerts = warningAlerts;
        this.infoAlerts = infoAlerts;
        this.alerts = alerts;
        this.lastUpdated = lastUpdated;
    }

    // Getter 和 Setter
    public Integer getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(Integer totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public Integer getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(Integer criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public Integer getWarningAlerts() {
        return warningAlerts;
    }

    public void setWarningAlerts(Integer warningAlerts) {
        this.warningAlerts = warningAlerts;
    }

    public Integer getInfoAlerts() {
        return infoAlerts;
    }

    public void setInfoAlerts(Integer infoAlerts) {
        this.infoAlerts = infoAlerts;
    }

    public List<AlertItemDTO> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertItemDTO> alerts) {
        this.alerts = alerts;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}