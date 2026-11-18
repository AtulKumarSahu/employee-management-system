package com.ems.employeemanagement.dto.request;

import com.ems.employeemanagement.enums.LeaveType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for submitting a new leave application.
 */
@Schema(description = "Leave application submission payload")
public record LeaveApplicationRequest(

        @Schema(description = "Target employee ID (optional; inferred from token if omitted)", example = "1")
        Long employeeId,

        @Schema(description = "Leave type", example = "CASUAL")
        @NotNull(message = "Leave type is required")
        LeaveType leaveType,

        @Schema(description = "Leave start date", example = "2026-12-01")
        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,

        @Schema(description = "Leave end date", example = "2026-12-03")
        @NotNull(message = "End date is required")
        LocalDate endDate,

        @Schema(description = "Reason or remarks for requested leave", example = "Attending family function")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason
) {}
