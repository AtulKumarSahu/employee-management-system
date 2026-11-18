package com.ems.employeemanagement.repository;

import com.ems.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link User} entity.
 *
 * <p>Extends {@link JpaRepository} which provides:
 * <ul>
 *   <li>save(), findById(), findAll(), delete() etc. — CRUD</li>
 *   <li>flush(), saveAndFlush() — transaction control</li>
 *   <li>Pagination and sorting support</li>
 * </ul>
 *
 * <p>Spring Data JPA generates the implementation at runtime — we
 * only define the interface and method signatures.
 *
 * <p>Method naming convention for derived queries:
 * findBy{Field} → SELECT * FROM users WHERE {field} = ?
 * existsBy{Field} → SELECT COUNT(*) > 0 FROM users WHERE {field} = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used by Spring Security's UserDetailsService for authentication.
     *
     * Derived query: Spring generates → SELECT u FROM User u WHERE u.email = :email
     *
     * @param email the email to search for
     * @return Optional.empty() if not found, avoiding NullPointerException
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their username.
     * Alternative login method.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if an email is already registered.
     * Used during registration to prevent duplicates.
     * Returns a boolean — more efficient than loading the full entity.
     */
    boolean existsByEmail(String email);

    /**
     * Check if a username is already taken.
     */
    boolean existsByUsername(String username);

    /**
     * Find a user by email or username — supports flexible login.
     * Custom JPQL query (not derivable from method name alone).
     *
     * @Query uses JPQL (not SQL) — references entity class and field names.
     * :emailOrUsername is a named parameter bound from @Param.
     */
    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername OR u.username = :emailOrUsername")
    Optional<User> findByEmailOrUsername(@Param("emailOrUsername") String emailOrUsername);
}

