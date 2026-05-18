package com.example.superMalle.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Response Wrapper
 * 
 * Standard response wrapper for all API endpoints
 * Provides consistent response structure across the API
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Response timestamp")
    private String timestamp;

    @Schema(description = "HTTP status code")
    private Integer status;

    @Schema(description = "Response message")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error details (if any)")
    private ErrorDetails error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error details")
    public static class ErrorDetails {
        @Schema(description = "Error code")
        private String code;

        @Schema(description = "Error field (for validation errors)")
        private String field;

        @Schema(description = "Error message")
        private String message;

        @Schema(description = "Additional error details")
        private Object details;
    }

    /**
     * Create success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(java.time.Instant.now().toString())
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    /**
     * Create success response with custom message
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(java.time.Instant.now().toString())
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(Integer status, String message) {
        return ApiResponse.<T>builder()
                .timestamp(java.time.Instant.now().toString())
                .status(status)
                .message(message)
                .error(ErrorDetails.builder()
                        .code(String.valueOf(status))
                        .message(message)
                        .build())
                .build();
    }

    /**
     * Create error response with details
     */
    public static <T> ApiResponse<T> error(Integer status, String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .timestamp(java.time.Instant.now().toString())
                .status(status)
                .message(message)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .build();
    }
}
