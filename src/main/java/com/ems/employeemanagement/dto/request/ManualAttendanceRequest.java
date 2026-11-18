package com.ems.employeemanagement.dto.request;

import com.ems.employeemanagement.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for manual attendance recording or adjustments by HR/Admin.
 *
 * @param employeeId     target employee ID
 * @param attendanceDate date for the attendance entry (cannot be in future)
 * @param checkInTime    recorded check-in time
 * @param checkOutTime   recorded check-out time
 * @param status         explicit status override (optional; automatically derived if null)
 * @param notes          audit notes explaining the manual entry
 */
public record ManualAttendanceRequest(

        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Attendance date is required")
        @PastOrPresent(message = "Attendance date cannot be in the future")
        LocalDate attendanceDate,

        LocalTime checkInTime,

        LocalTime checkOutTime,

        AttendanceStatus status,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {}

