package com.ems.employeemanagement.dto.response;

import com.ems.employeemanagement.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO representing a daily attendance record.
 */
public record AttendanceResponse(
        Long id,
        EmployeeSummary employee,
        LocalDate attendanceDate,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        Double workingHours,
        AttendanceStatus status,
        boolean late,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record EmployeeSummary(
            Long id,
            String employeeCode,
            String fullName,
            String email
    ) {}
}

