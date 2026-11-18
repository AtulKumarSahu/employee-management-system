package com.ems.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a JWT refresh token stored in the database.
 *
 * <p>Refresh tokens are long-lived tokens used to obtain new access tokens
 * without requiring the user to log in again. We store them in the DB so we can:
 * <ul>
 *   <li>Invalidate individual sessions on logout</li>
 *   <li>Detect token reuse (security)</li>
 *   <li>Expire tokens server-side independently of token TTL</li>
 * </ul>
 *
 * <p>When a user logs out, the refresh token is deleted from this table.
 * When a user logs in from multiple devices, each device gets its own row.
 *
 * <p>We use {@code Instant} for expiry to work with UTC epoch timestamps
 * — avoids all timezone ambiguity.
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_token_value", columnList = "token", unique = true),
        @Index(name = "idx_refresh_token_user",  columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The raw token string (UUID or secure random).
     * Stored as the lookup key for the /auth/refresh endpoint.
     * Length 512 to safely store a UUID or JWT string.
     */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    /**
     * The user this token belongs to.
     * When the user is deleted, cascade deletes their refresh tokens.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * UTC expiry instant. Checked on every refresh attempt.
     * If Instant.now().isAfter(expiresAt), the token is expired.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Whether this token has been revoked (e.g., via logout from all devices).
     * An alternative to deletion — allows audit of token usage.
     */
    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    /**
     * When the token was created — for audit and TTL enforcement.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Checks if the token is currently valid (not expired and not revoked).
     *
     * @return true if the token can be used to issue a new access token
     */
    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }
}

