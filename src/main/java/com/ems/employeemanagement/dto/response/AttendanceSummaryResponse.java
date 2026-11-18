package com.ems.employeemanagement.dto.response;

/**
 * Response DTO aggregating monthly attendance metrics for an employee.
 *
 * @param employeeId         employee ID
 * @param employeeCode       employee code (e.g. EMP-000001)
 * @param employeeName       employee full name
 * @param year               queried year
 * @param month              queried month (1-12)
 * @param totalDaysRecorded  count of recorded attendance days in month
 * @param presentDays        number of PRESENT days
 * @param lateDays           number of days arrived LATE
 * @param halfDays           number of HALF_DAY days
 * @param absentDays         number of recorded ABSENT days
 * @param totalWorkingHours  accumulated work hours in the month
 * @param averageDailyHours  average working hours per present day
 */
public record AttendanceSummaryResponse(
        Long employeeId,
        String employeeCode,
        String employeeName,
        int year,
        int month,
        long totalDaysRecorded,
        long presentDays,
        long lateDays,
        long halfDays,
        long absentDays,
        double totalWorkingHours,
        double averageDailyHours
) {}

