package com.ems.employeemanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 *
 * <p>Supports authentication via either username or registered email address.
 *
 * @param usernameOrEmail user's registered username or email
 * @param password        plaintext password to verify
 */
@Schema(description = "User login credentials payload")
public record LoginRequest(

        @Schema(description = "Registered username or email address", example = "admin@ems.com")
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @Schema(description = "Plaintext account password", example = "Admin@123")
        @NotBlank(message = "Password is required")
        String password
) {}
