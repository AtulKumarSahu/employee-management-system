package com.ems.employeemanagement.dto.response;

import com.ems.employeemanagement.enums.EmploymentStatus;
import com.ems.employeemanagement.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for an Employee resource.
 *
 * <p>This is what gets returned in API responses. We deliberately exclude:
 * <ul>
 *   <li>password / credentials — never expose via API</li>
 *   <li>is_deleted flag — deleted employees aren't visible at all</li>
 *   <li>Full User entity — we include only the userId for reference</li>
 * </ul>
 *
 * <p>Department and Manager are represented as lightweight summary records
 * to avoid deep nesting and circular references in JSON.
 *
 * @param id               employee's database ID
 * @param employeeCode     human-readable code (EMP-000001)
 * @param firstName        first name
 * @param lastName         last name
 * @param fullName         computed full name (firstName + " " + lastName)
 * @param email            work email
 * @param phoneNumber      contact phone
 * @param dateOfBirth      date of birth
 * @param gender           gender
 * @param address          address
 * @param designation      job title
 * @param joiningDate      date joined
 * @param salary           current salary
 * @param employmentStatus current status
 * @param department       department summary (or null if unassigned)
 * @param manager          manager summary (or null if no manager)
 * @param userId           linked user account ID (or null)
 * @param createdAt        record creation timestamp
 * @param updatedAt        record last update timestamp
 */
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        String designation,
        LocalDate joiningDate,
        BigDecimal salary,
        EmploymentStatus employmentStatus,
        DepartmentSummary department,
        ManagerSummary manager,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Minimal department information embedded in employee responses.
     * Prevents loading and serializing the full DepartmentResponse with its employee list.
     */
    public record DepartmentSummary(
            Long id,
            String name,
            String code
    ) {}

    /**
     * Minimal manager information embedded in employee responses.
     * Self-referential nesting must be bounded — we stop at manager summary.
     */
    public record ManagerSummary(
            Long id,
            String employeeCode,
            String fullName,
            String designation
    ) {}
}

