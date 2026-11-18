package com.ems.employeemanagement.repository;

import com.ems.employeemanagement.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for {@link RefreshToken} entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its raw token string.
     * Called on every POST /auth/refresh request.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Delete all refresh tokens for a specific user.
     * Called on logout — invalidates all sessions for that user.
     *
     * @Modifying: Required for all @Query methods that modify data (INSERT/UPDATE/DELETE).
     * Without it, Spring throws "Query must not update/modify objects".
     *
     * @Transactional should be on the calling service method.
     * Adding it here as a safety net.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Delete all expired and revoked tokens — cleanup job.
     * Can be called by a @Scheduled task.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    void deleteExpiredAndRevoked(@Param("now") Instant now);

    /**
     * Check if any valid (non-expired, non-revoked) token exists for a user.
     */
    @Query("""
        SELECT COUNT(rt) > 0 FROM RefreshToken rt
        WHERE rt.user.id = :userId
          AND rt.revoked = false
          AND rt.expiresAt > :now
        """)
    boolean hasValidToken(@Param("userId") Long userId, @Param("now") Instant now);
}

