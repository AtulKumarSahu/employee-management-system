package com.ems.employeemanagement.dto.response;

import com.ems.employeemanagement.enums.LeaveStatus;
import com.ems.employeemanagement.enums.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing a leave request.
 *
 * <p>Implemented as a Java 21 Record with nested lightweight summaries
 * to decouple response representations from entity objects.
 */
public record LeaveRequestResponse(
        Long id,
        EmployeeSummary employee,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        Integer totalDays,
        String reason,
        LeaveStatus status,
        ReviewerSummary reviewedBy,
        LocalDateTime reviewedAt,
        String reviewerNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record EmployeeSummary(
            Long id,
            String employeeCode,
            String fullName,
            String email
    ) {}

    public record ReviewerSummary(
            Long id,
            String username,
            String email
    ) {}
}

