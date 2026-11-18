package com.ems.employeemanagement.enums;

/**
 * Types of leave available to employees.
 *
 * <p>Each leave type has different accrual rules and balance limits.
 * These are managed in the LeaveBalance entity per employee.
 */
public enum LeaveType {

    /** Short-term personal leave — typically 12 days/year. */
    CASUAL,

    /** Medical/health-related leave — typically 12 days/year. */
    SICK,

    /**
     * Earned/privileged leave — accrued based on days worked.
     * Typically 1 day per 20 days worked.
     */
    EARNED,

    /**
     * Leave without pay.
     * Allowed when other leave balances are exhausted.
     */
    UNPAID
}

