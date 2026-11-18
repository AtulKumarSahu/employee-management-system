package com.ems.employeemanagement.enums;

/**
 * Daily attendance status for an employee.
 *
 * <p>Recorded once per employee per working day.
 * Determined from check-in/check-out times or manual entry by HR.
 */
public enum AttendanceStatus {

    /** Employee was present for the full working day. */
    PRESENT,

    /** Employee was absent (no check-in recorded). */
    ABSENT,

    /** Employee was present for half the working day. */
    HALF_DAY,

    /**
     * Employee checked in after the grace period
     * (e.g., more than 15 minutes after shift start).
     */
    LATE
}

