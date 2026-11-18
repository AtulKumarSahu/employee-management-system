package com.ems.employeemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response DTO returned by {@code GlobalExceptionHandler}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response payload")
public record ApiErrorResponse(

        @Schema(description = "Indicates request failure", example = "false")
        boolean success,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP reason phrase", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable error explanation", example = "Validation failed. Please check the request body.")
        String message,

        @Schema(description = "Map of field-specific validation failures")
        Map<String, String> fieldErrors,

        @Schema(description = "Requested resource URI path", example = "/api/v1/employees")
        String path,

        @Schema(description = "Error timestamp", example = "2026-11-18T09:00:00")
        LocalDateTime timestamp
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(false, status, error, message, null, path, LocalDateTime.now());
    }

    public static ApiErrorResponse ofValidation(int status, String message,
                                                 Map<String, String> fieldErrors, String path) {
        return new ApiErrorResponse(false, status, "Validation Failed", message,
                fieldErrors, path, LocalDateTime.now());
    }
}
