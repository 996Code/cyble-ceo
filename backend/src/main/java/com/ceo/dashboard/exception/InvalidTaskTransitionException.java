package com.ceo.dashboard.exception;

public class InvalidTaskTransitionException extends IllegalArgumentException {
    public InvalidTaskTransitionException(String fromStatus, String toStatus) {
        super("Invalid state transition: " + fromStatus + " -> " + toStatus);
    }
}