package com.ems.employeemanagement.repository;

import com.ems.employeemanagement.entity.Employee;
import com.ems.employeemanagement.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Employee} entity.
 *
 * <p>All methods automatically filter out soft-deleted employees
 * because the entity is annotated with @Where(clause = "is_deleted = false").
 *
 * <p>{@link Page} is Spring Data's paginated result container. It carries:
 * <ul>
 *   <li>content — the list of records for the current page</li>
 *   <li>totalElements — total matching records (for frontend pagination)</li>
 *   <li>totalPages, number, size, etc.</li>
 * </ul>
 *
 * <p>{@link Pageable} is passed from the controller and carries:
 * page number, page size, and sort criteria.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find employee by their unique employee code (e.g., EMP-000001).
     */
    Optional<Employee> findByEmployeeCode(String employeeCode);

    /**
     * Find employee by work email.
     * Used to check duplicates and for email-based lookups.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Check if an email is already used by another employee.
     */
    boolean existsByEmail(String email);

    /**
     * Check if an employee code already exists.
     */
    boolean existsByEmployeeCode(String employeeCode);

    /**
     * Find all employees in a specific department with pagination.
     * JOIN FETCH avoids N+1 queries when accessing department data.
     */
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId")
    Page<Employee> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);

    /**
     * Full-text search across name and email fields.
     *
     * <p>LOWER() ensures case-insensitive matching.
     * LIKE with % wildcards allows partial matching.
     * This covers common search scenarios: "john", "john doe", "@company.com".
     *
     * <p>For production with large datasets, consider PostgreSQL full-text search
     * (tsvector/tsquery) or Elasticsearch. LIKE with leading % is not index-friendly.
     *
     * @param keyword the search term
     * @param pageable pagination parameters
     * @return paginated matching employees
     */
    @Query("""
        SELECT e FROM Employee e
        WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(e.email)     LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Employee> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Combined filter by department and employment status with pagination.
     * Both parameters are optional — null values are skipped in the WHERE clause.
     *
     * <p>(:departmentId IS NULL OR e.department.id = :departmentId) is a common
     * JPQL pattern for optional filters — avoids building dynamic queries.
     */
    @Query("""
        SELECT e FROM Employee e
        WHERE (:departmentId IS NULL OR e.department.id = :departmentId)
          AND (:status IS NULL OR e.employmentStatus = :status)
        """)
    Page<Employee> findByFilters(
        @Param("departmentId") Long departmentId,
        @Param("status") EmploymentStatus status,
        Pageable pageable
    );

    /**
     * Find all direct reports of a given manager.
     */
    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId")
    Page<Employee> findByManagerId(@Param("managerId") Long managerId, Pageable pageable);

    /**
     * Count employees by department — used for department listings.
     */
    long countByDepartmentId(Long departmentId);

    /**
     * Check if any active employee is assigned to a department.
     * Used before deleting a department — prevents orphaned employees.
     */
    boolean existsByDepartmentIdAndDeletedFalse(Long departmentId);

    /**
     * Find employee by their linked User ID.
     * Used in security layer to load employee context from JWT claims.
     */
    Optional<Employee> findByUserId(Long userId);
}

