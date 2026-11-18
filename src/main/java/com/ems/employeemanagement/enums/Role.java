package com.ems.employeemanagement.enums;

/**
 * User roles in the system.
 *
 * <p>These are stored as strings in the database (EnumType.STRING)
 * rather than ordinals, so renaming an enum constant doesn't corrupt data.
 *
 * <p>Role hierarchy (highest to lowest privilege):
 * ADMIN → HR → MANAGER → EMPLOYEE
 */
public enum Role {

    /**
     * Full system access.
     * Can manage employees, departments, users and roles.
     */
    ADMIN,

    /**
     * Human Resources role.
     * Can manage employees and departments, view employee records.
     */
    HR,

    /**
     * Team manager role.
     * Can view team members and manage team-related information.
     */
    MANAGER,

    /**
     * Regular employee role.
     * Can view own profile and update permitted personal information.
     */
    EMPLOYEE
}

