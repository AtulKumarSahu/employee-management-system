package com.ems.employeemanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a department.
 *
 * @param name        department name — required, max 100 chars
 * @param description optional description of department responsibilities
 * @param code        short code like "ENG", "HR", "FIN" — optional, max 20 chars
 */
@Schema(description = "Department creation and update payload")
public record DepartmentRequest(

        @Schema(description = "Unique department name", example = "Software Engineering")
        @NotBlank(message = "Department name is required")
        @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
        String name,

        @Schema(description = "Detailed description of department operations", example = "Core software development, cloud infrastructure, and DevOps")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Short department code", example = "ENG")
        @Size(max = 20, message = "Department code must not exceed 20 characters")
        String code
) {}
