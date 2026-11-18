package com.ems.employeemanagement.dto.request;

import com.ems.employeemanagement.enums.EmploymentStatus;
import com.ems.employeemanagement.enums.Gender;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating an existing employee's profile.
 *
 * <p>All fields are optional (nullable) because an update may change
 * only specific fields. Fields that are null in the request body
 * are ignored in the service layer (not overwritten with null).
 *
 * <p>This is a full-replacement PUT design — the client sends all
 * fields they want to keep. For true partial PATCH semantics, all
 * constraints must be nullable-compatible (which they are here).
 *
 * <p>Note: email and employeeCode are intentionally excluded from updates.
 * Email changes should go through a dedicated verified-change flow.
 */
public record EmployeeUpdateRequest(

        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        String firstName,

        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        Gender gender,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Size(max = 100, message = "Designation must not exceed 100 characters")
        String designation,

        LocalDate joiningDate,

        @Positive(message = "Salary must be a positive value")
        @DecimalMax(value = "9999999.99", message = "Salary exceeds maximum allowed value")
        BigDecimal salary,

        EmploymentStatus employmentStatus,

        Long departmentId,

        Long managerId
) {}

