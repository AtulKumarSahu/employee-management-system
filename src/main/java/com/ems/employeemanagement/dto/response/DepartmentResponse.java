package com.ems.employeemanagement.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for a Department resource.
 *
 * <p>Contains all data safe to expose via the REST API.
 * Note: manager is represented as a simple {@link ManagerSummary}
 * nested record — not the full EmployeeResponse — to avoid deep nesting
 * and circular reference issues.
 *
 * @param id             department ID
 * @param name           department name
 * @param description    department description
 * @param code           short department code
 * @param active         whether the department is active
 * @param manager        summary of the department manager (or null)
 * @param employeeCount  total number of active employees in this department
 * @param createdAt      when the department was created
 * @param updatedAt      when the department was last updated
 */
public record DepartmentResponse(
        Long id,
        String name,
        String description,
        String code,
        boolean active,
        ManagerSummary manager,
        long employeeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Nested record for manager summary inside DepartmentResponse.
     * A "nested record" keeps the type scoped to its usage context.
     * Avoids circular references that would occur with full EmployeeResponse.
     *
     * @param id       manager's employee ID
     * @param fullName manager's full name
     * @param email    manager's work email
     */
    public record ManagerSummary(
            Long id,
            String fullName,
            String email
    ) {}
}

