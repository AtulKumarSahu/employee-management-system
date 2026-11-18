package com.ems.employeemanagement.dto.request;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request DTO for user account registration.
 *
 * <p>Implemented as a Java 21 Record for immutability and thread safety.
 *
 * @param username chosen unique username (3-50 characters)
 * @param email    valid work or personal email address
 * @param password raw password (minimum 8 characters, will be BCrypt-hashed)
 * @param roles    optional initial roles (defaults to EMPLOYEE if null or empty)
 */
@Schema(description = "User account registration request payload")
public record RegisterRequest(

        @Schema(description = "Unique user account handle", example = "john_doe")
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Schema(description = "Work or personal email", example = "john.doe@company.com")
        @NotBlank(message = AppConstants.EMAIL_REQUIRED)
        @Email(message = AppConstants.EMAIL_INVALID)
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "Plaintext password (min 8 chars)", example = "SecurePassword@123")
        @NotBlank(message = AppConstants.PASSWORD_REQUIRED)
        @Size(min = 8, max = 100, message = AppConstants.PASSWORD_MIN_LENGTH)
        String password,

        @Schema(description = "Assigned user roles; defaults to [EMPLOYEE] if empty", example = "[\"HR\"]")
        Set<Role> roles
) {}
