package com.ems.employeemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Uniform API response envelope wrapping all success responses.
 *
 * <p>Every API endpoint returns this wrapper so the frontend always
 * knows the structure of a successful response:
 * <pre>
 * {
 *   "success": true,
 *   "message": "Employee created successfully",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * </pre>
 *
 * <p>Why a Java Record?
 * Records are immutable data carriers — perfect for response DTOs.
 * The compiler generates canonical constructor, getters, equals/hashCode/toString.
 *
 * <p>@JsonInclude(NON_NULL): Jackson omits fields that are null.
 * When there is no data (e.g., DELETE response), "data" won't appear in JSON.
 *
 * @param <T> the type of the data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {
    /**
     * Factory method for a successful response with data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Factory method for a successful response without data payload
     * (e.g., DELETE, logout).
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    /**
     * Factory method for an error response.
     * Errors are typically handled by GlobalExceptionHandler, but
     * this can be used for programmatic error responses.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}

