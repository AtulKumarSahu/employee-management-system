package com.ems.employeemanagement.dto.response;

import com.ems.employeemanagement.enums.LeaveType;

import java.time.LocalDateTime;

/**
 * Response DTO representing an employee's leave balance quota.
 */
public record LeaveBalanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        Integer totalDays,
        Integer usedDays,
        Integer remainingDays,
        Integer leaveYear,
        LocalDateTime updatedAt
) {}

