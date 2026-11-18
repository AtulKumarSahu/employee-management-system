package com.ems.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an organizational department.
 *
 * <p>A department groups employees together under a common function
 * (e.g., Engineering, HR, Finance). Each department may have one
 * designated manager (who is also an Employee).
 *
 * <p>Department entity is defined before Employee to handle the
 * circular reference: Employee references Department, and Department
 * has a managerId that references Employee. We handle this with
 * @JoinColumn referencing the employee's id directly (not the full entity
 * in the foreign key direction that would cause circular dependency).
 */
@Entity
@Table(
    name = "departments",
    indexes = {
        @Index(name = "idx_dept_name", columnList = "name", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Department name must be unique across the organization.
     * e.g., "Engineering", "Human Resources", "Finance".
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Brief description of the department's responsibilities.
     * TEXT column type in PostgreSQL — no length limit.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Department code — a short identifier like "ENG", "HR", "FIN".
     * Used in reporting and employee code generation.
     */
    @Column(name = "code", unique = true, length = 20)
    private String code;

    /**
     * Whether the department is currently active.
     * Inactive departments are hidden from selection but data is preserved.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * The employee who manages this department.
     *
     * @ManyToOne: Many departments CAN have the same manager
     *   (edge case, but schema supports it).
     * fetch = LAZY: Don't load the manager's full entity unless needed.
     * @JoinColumn: This table (departments) holds the foreign key column "manager_id".
     * nullable = true: A department may exist without a designated manager.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    private Employee manager;

    /**
     * All employees in this department.
     *
     * mappedBy = "department" tells JPA that the Employee entity
     * owns the foreign key (department_id on employees table).
     * This side is "inverse" — it doesn't create a column.
     *
     * FetchType.LAZY: critical for performance. Never load all employees
     * of a department unless explicitly requested.
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

