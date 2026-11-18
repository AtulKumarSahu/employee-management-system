package com.ems.employeemanagement.service.impl;

import com.ems.employeemanagement.dto.request.LoginRequest;
import com.ems.employeemanagement.dto.request.RefreshTokenRequest;
import com.ems.employeemanagement.dto.request.RegisterRequest;
import com.ems.employeemanagement.dto.response.AuthResponse;
import com.ems.employeemanagement.entity.RefreshToken;
import com.ems.employeemanagement.entity.User;
import com.ems.employeemanagement.enums.Role;
import com.ems.employeemanagement.exception.DuplicateResourceException;
import com.ems.employeemanagement.exception.InvalidRequestException;
import com.ems.employeemanagement.exception.ResourceNotFoundException;
import com.ems.employeemanagement.repository.RefreshTokenRepository;
import com.ems.employeemanagement.repository.UserRepository;
import com.ems.employeemanagement.security.CustomUserDetails;
import com.ems.employeemanagement.security.JwtTokenProvider;
import com.ems.employeemanagement.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Production implementation of {@link AuthService}.
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshTokenExpirationMs;

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            @Value("${application.security.jwt.refresh-token.expiration}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing user registration for username: {}, email: {}",
                request.username(), request.email());

        // 1. Uniqueness validation
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        // 2. Resolve roles (default to EMPLOYEE if none provided)
        List<Role> assignedRoles = new ArrayList<>();
        if (request.roles() == null || request.roles().isEmpty()) {
            assignedRoles.add(Role.EMPLOYEE);
        } else {
            assignedRoles.addAll(request.roles());
        }

        // 3. Construct and persist User entity with BCrypt password
        User user = User.builder()
                .username(request.username().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .active(true)
                .locked(false)
                .roles(assignedRoles)
                .build();

        User savedUser = userRepository.save(user);

        // 4. Generate tokens
        CustomUserDetails userDetails = CustomUserDetails.build(savedUser);
        String accessToken = jwtTokenProvider.generateTokenFromUserDetails(userDetails);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        List<String> roleNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("User registered successfully with id: {}", savedUser.getId());

        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getExpirationMs(),
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                roleNames
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating login request for: {}", request.usernameOrEmail());

        // 1. Authenticate via AuthenticationManager (triggers DaoAuthenticationProvider + PasswordEncoder)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail(),
                        request.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        // 2. Generate new JWT access token
        String accessToken = jwtTokenProvider.generateToken(authentication);

        // 3. Create or rotate refresh token
        RefreshToken refreshToken = createRefreshToken(user);

        List<String> roleNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("User '{}' authenticated successfully", user.getUsername());

        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getExpirationMs(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleNames
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenStr = request.refreshToken();

        // 1. Look up refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new InvalidRequestException("Invalid or unrecognized refresh token"));

        // 2. Validate token validity and expiration
        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRequestException("Refresh token is expired or revoked. Please log in again.");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = CustomUserDetails.build(user);

        // 3. Generate new access token
        String newAccessToken = jwtTokenProvider.generateTokenFromUserDetails(userDetails);

        List<String> roleNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.debug("Refreshed access token for user: {}", user.getUsername());

        return AuthResponse.of(
                newAccessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getExpirationMs(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleNames
        );
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        log.info("Logging out and invalidating session token");
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .createdAt(Instant.now())
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}

