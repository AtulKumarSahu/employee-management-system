package com.ems.employeemanagement.repository;

import com.ems.employeemanagement.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Department} entity.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    Optional<Department> findByCode(String code);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    /**
     * Find all active departments (paginated).
     */
    Page<Department> findByActiveTrue(Pageable pageable);

    /**
     * Find all active departments (no pagination — for dropdowns).
     */
    List<Department> findByActiveTrueOrderByNameAsc();

    /**
     * Check if a department name is used by a DIFFERENT department (for update validation).
     * Excludes the current department from the duplicate check.
     *
     * @param name the name to check
     * @param excludeId the department being updated (excluded from check)
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.name = :name AND d.id <> :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);
}

