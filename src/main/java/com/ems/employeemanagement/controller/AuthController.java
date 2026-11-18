package com.ems.employeemanagement.controller;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.LoginRequest;
import com.ems.employeemanagement.dto.request.RefreshTokenRequest;
import com.ems.employeemanagement.dto.request.RegisterRequest;
import com.ems.employeemanagement.dto.response.ApiResponse;
import com.ems.employeemanagement.dto.response.AuthResponse;
import com.ems.employeemanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing public authentication endpoints.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/auth")
@Slf4j
@Tag(name = "Authentication & Authorization", description = "Endpoints for user registration, login, token refresh, and logout")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/auth/register — Register a new user account.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register new user: {}", request.username());
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * POST /api/v1/auth/login — Authenticate user and issue tokens.
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Validates credentials and returns JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user: {}", request.usernameOrEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    /**
     * POST /api/v1/auth/refresh — Refresh expired access token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Issues a fresh access token using a valid database refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * POST /api/v1/auth/logout — Invalidate refresh token session.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the given refresh token in the database")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.refreshToken() != null) {
            authService.logout(request.refreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}

