package com.ems.employeemanagement.service;

import com.ems.employeemanagement.dto.request.LoginRequest;
import com.ems.employeemanagement.dto.request.RefreshTokenRequest;
import com.ems.employeemanagement.dto.request.RegisterRequest;
import com.ems.employeemanagement.dto.response.AuthResponse;

/**
 * Service contract for authentication, user registration, token refresh, and logout.
 */
public interface AuthService {

    /**
     * Registers a new user account with encrypted credentials and assigned roles.
     *
     * @param request user registration details
     * @return authentication response containing tokens and user summary
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates user credentials and issues access and refresh tokens.
     *
     * @param request login credentials (username/email and password)
     * @return authentication response with JWT tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Issues a new access token using a valid, unexpired database refresh token.
     *
     * @param request refresh token request wrapper
     * @return authentication response with newly generated access token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Invalidates a user's refresh token session upon logout.
     *
     * @param refreshToken the token string to revoke/delete
     */
    void logout(String refreshToken);
}

