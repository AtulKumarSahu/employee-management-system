package com.ems.employeemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for exchanging an existing refresh token for a new access token.
 *
 * @param refreshToken the UUID or random refresh token string issued during login
 */
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}

