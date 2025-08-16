package com.quantumleap.exception;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Standard error response structure for the API
 */
@Data
@Builder
public class ErrorResponse {
    
    /**
     * Timestamp when the error occurred
     */
    private OffsetDateTime timestamp;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error type/category
     */
    private String error;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Additional error details (optional)
     */
    private Map<String, Object> details;
    
    /**
     * Request path that caused the error
     */
    private String path;
}
