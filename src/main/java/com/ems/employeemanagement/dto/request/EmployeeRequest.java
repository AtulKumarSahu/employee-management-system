package com.ems.employeemanagement.dto.request;

import com.ems.employeemanagement.enums.EmploymentStatus;
import com.ems.employeemanagement.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new employee.
 */
@Schema(description = "Employee creation request payload")
public record EmployeeRequest(

        @Schema(description = "Employee first name", example = "Jane")
        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        String firstName,

        @Schema(description = "Employee last name", example = "Smith")
        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @Schema(description = "Unique work email address", example = "jane.smith@enterprise.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "Contact phone number", example = "+1-555-0199")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,

        @Schema(description = "Date of birth", example = "1995-06-15")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Schema(description = "Gender", example = "FEMALE")
        Gender gender,

        @Schema(description = "Residential address", example = "123 Innovation Way, Tech Park")
        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Schema(description = "Job title or designation", example = "Senior Software Engineer")
        @Size(max = 100, message = "Designation must not exceed 100 characters")
        String designation,

        @Schema(description = "Official joining date", example = "2024-01-15")
        @NotNull(message = "Joining date is required")
        LocalDate joiningDate,

        @Schema(description = "Base salary", example = "85000.00")
        @Positive(message = "Salary must be a positive value")
        @DecimalMax(value = "9999999.99", message = "Salary exceeds maximum allowed value")
        BigDecimal salary,

        @Schema(description = "Current employment status; defaults to ACTIVE", example = "ACTIVE")
        EmploymentStatus employmentStatus,

        @Schema(description = "Target department ID", example = "1")
        Long departmentId,

        @Schema(description = "Designated manager employee ID", example = "2")
        Long managerId
) {}
