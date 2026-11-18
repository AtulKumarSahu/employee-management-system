package com.ems.employeemanagement.enums;

/**
 * Employment status of an employee.
 *
 * <p>Used for filtering, reporting and business rule enforcement.
 * Stored as a string in the database.
 */
public enum EmploymentStatus {

    /** Employee is actively working. */
    ACTIVE,

    /** Employee is on an approved leave of absence. */
    ON_LEAVE,

    /** Employee is temporarily suspended. */
    SUSPENDED,

    /**
     * Employee has left the organization.
     * Records are soft-deleted — data is retained but status is TERMINATED.
     */
    TERMINATED
}

