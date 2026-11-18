package com.ems.employeemanagement.dto.response;

import java.util.List;

/**
 * Response DTO returned upon successful authentication or token refresh.
 *
 * <p>Contains both the short-lived JWT access token and the long-lived
 * refresh token stored in the database.
 *
 * @param accessToken  signed JWT string for Authorization: Bearer {token}
 * @param refreshToken database-backed token string for /auth/refresh
 * @param tokenType    standard token scheme ("Bearer")
 * @param expiresIn    access token validity period in milliseconds
 * @param userId       database ID of the authenticated user
 * @param username     authenticated user's handle
 * @param email        authenticated user's email
 * @param roles        list of role names (e.g. ["ROLE_ADMIN", "ROLE_HR"])
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String username,
        String email,
        List<String> roles
) {
    public static AuthResponse of(
            String accessToken,
            String refreshToken,
            long expiresIn,
            Long userId,
            String username,
            String email,
            List<String> roles
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                userId,
                username,
                email,
                roles
        );
    }
}

