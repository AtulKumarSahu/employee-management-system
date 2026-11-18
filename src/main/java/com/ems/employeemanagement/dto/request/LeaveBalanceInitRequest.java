package com.ems.employeemanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for initializing annual leave quotas for an employee.
 *
 * @param employeeId target employee ID
 * @param leaveYear  calendar or financial year (e.g. 2026)
 * @param casualDays annual entitlement for casual leave (default 12)
 * @param sickDays   annual entitlement for sick leave (default 12)
 * @param earnedDays annual entitlement for earned leave (default 15)
 */
public record LeaveBalanceInitRequest(

        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Leave year is required")
        @Min(value = 2000, message = "Leave year must be valid")
        Integer leaveYear,

        @NotNull(message = "Casual leave days required")
        @Min(value = 0, message = "Days cannot be negative")
        Integer casualDays,

        @NotNull(message = "Sick leave days required")
        @Min(value = 0, message = "Days cannot be negative")
        Integer sickDays,

        @NotNull(message = "Earned leave days required")
        @Min(value = 0, message = "Days cannot be negative")
        Integer earnedDays
) {}

