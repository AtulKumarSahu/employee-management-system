package com.ems.employeemanagement.entity;

import com.ems.employeemanagement.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks the leave balance for each employee for each leave type.
 *
 * <p>Each employee has one LeaveBalance record per LeaveType
 * (CASUAL, SICK, EARNED, UNPAID). These are initialized by HR
 * at the start of each leave year.
 *
 * <p>The unique constraint on (employee_id, leave_type) ensures
 * there is exactly one balance record per type per employee.
 */
@Entity
@Table(
    name = "leave_balances",
    uniqueConstraints = {
        // Composite unique constraint: one balance per employee per leave type
        @UniqueConstraint(
            name = "uq_leave_balance_emp_type",
            columnNames = {"employee_id", "leave_type"}
        )
    },
    indexes = {
        @Index(name = "idx_leave_balance_employee", columnList = "employee_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    /**
     * Total leave days allocated for the year (e.g., 12 for CASUAL).
     */
    @Column(name = "total_days", nullable = false)
    @Builder.Default
    private Integer totalDays = 0;

    /**
     * Leave days used so far in the current leave year.
     * Updated when a leave request is approved.
     */
    @Column(name = "used_days", nullable = false)
    @Builder.Default
    private Integer usedDays = 0;

    /**
     * Remaining balance — derived but stored for query performance.
     * Always computed as: totalDays - usedDays.
     */
    @Column(name = "remaining_days", nullable = false)
    @Builder.Default
    private Integer remainingDays = 0;

    /**
     * Leave year this balance applies to (e.g., 2024).
     * Allows multi-year records to be retained for auditing.
     */
    @Column(name = "leave_year", nullable = false)
    private Integer leaveYear;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Convenience method to check if sufficient balance is available.
     *
     * @param requestedDays the number of days requested
     * @return true if remaining >= requested
     */
    public boolean hasSufficientBalance(int requestedDays) {
        return remainingDays >= requestedDays;
    }

    /**
     * Deducts days from the balance when a leave request is approved.
     *
     * @param days number of days to deduct
     */
    public void deductDays(int days) {
        this.usedDays += days;
        this.remainingDays -= days;
    }

    /**
     * Restores days to the balance when an approved leave is cancelled.
     *
     * @param days number of days to restore
     */
    public void restoreDays(int days) {
        this.usedDays -= days;
        this.remainingDays += days;
    }
}

