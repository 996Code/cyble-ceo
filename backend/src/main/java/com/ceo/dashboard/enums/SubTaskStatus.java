package com.ceo.dashboard.enums;

public enum SubTaskStatus {
    NOT_STARTED("not-started"),
    IN_PROGRESS("in-progress"),
    COMPLETED("completed");

    private final String value;

    SubTaskStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SubTaskStatus fromValue(String value) {
        for (SubTaskStatus status : SubTaskStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown subtask status: " + value);
    }
}