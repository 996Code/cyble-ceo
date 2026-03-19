package com.ceo.dashboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Agent 未找到异常
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AgentNotFoundException extends RuntimeException {
    
    public AgentNotFoundException(String message) {
        super(message);
    }
    
    public AgentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}