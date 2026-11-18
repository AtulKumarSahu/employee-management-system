package com.ems.employeemanagement.entity;

import com.ems.employeemanagement.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an authenticated system user.
 *
 * <p>A User is the security principal — it holds credentials and roles.
 * An Employee record (separate entity) holds HR/profile data.
 * This separation allows system users (e.g., admins) who may not
 * be employees, and allows employees to exist without system login access.
 *
 * <p>Implements {@link org.springframework.security.core.userdetails.UserDetails}
 * indirectly via Spring Security in Phase 6.
 *
 * <p>Why a normal class (not Record) for entities:
 * JPA requires a no-arg constructor and mutable state (setters), which
 * Java Records do not support. Records are final and immutable.
 */
@Entity
@Table(
    name = "users",
    // users is a reserved keyword in some DBs; prefer explicit table names
    indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_username", columnList = "username", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Primary key using IDENTITY strategy.
     * PostgreSQL maps this to a BIGSERIAL / GENERATED ALWAYS AS IDENTITY column.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Login username — unique across the system.
     * Typically the employee's email or a chosen handle.
     */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Login email — unique, used for password recovery and notifications.
     */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * BCrypt-hashed password. NEVER stored in plaintext.
     * BCrypt hash is always 60 characters long.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Whether the account is active.
     * Inactive accounts cannot log in. Used for soft-disabling without deletion.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Whether the account is locked (e.g., after too many failed login attempts).
     */
    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean locked = false;

    /**
     * @ElementCollection with @Enumerated stores enum values in a separate join table.
     *
     * FetchType.EAGER is intentional here: Spring Security's UserDetails.getAuthorities()
     * is called on every request. Lazy-loading here would cause a session-closed error
     * or N+1 problems in a stateless JWT setup.
     *
     * @CollectionTable defines the join table name and foreign key column.
     * @Enumerated(STRING) stores "ADMIN", "HR" etc. as strings — survives reordering.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    /**
     * Bidirectional One-to-One with Employee.
     * mappedBy = "user" means Employee owns the foreign key column.
     * cascade = ALL: when User is deleted, Employee is also deleted.
     * fetch = LAZY: load employee data only when accessed, not on every auth check.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employee employee;

    /**
     * One-to-Many with RefreshToken.
     * orphanRemoval = true: when a token is removed from this list, it is deleted.
     * cascade = ALL: saves/deletes tokens with the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    // ==================== Audit Fields ====================

    /**
     * @CreationTimestamp: Hibernate sets this automatically on INSERT.
     * updatable = false: Hibernate never tries to update this column after creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * @UpdateTimestamp: Hibernate sets this automatically on every UPDATE.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

