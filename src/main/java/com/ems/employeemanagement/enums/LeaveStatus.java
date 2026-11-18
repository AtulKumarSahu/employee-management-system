package com.ems.employeemanagement.enums;

/**
 * Status of a leave request through its approval lifecycle.
 */
public enum LeaveStatus {

    /** Request submitted, awaiting manager/HR review. */
    PENDING,

    /** Request approved by authorized reviewer. */
    APPROVED,

    /** Request declined by authorized reviewer. */
    REJECTED,

    /** Request withdrawn by the employee before decision. */
    CANCELLED
}

