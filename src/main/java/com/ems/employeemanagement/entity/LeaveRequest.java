package com.ems.employeemanagement.entity;

import com.ems.employeemanagement.enums.LeaveStatus;
import com.ems.employeemanagement.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a leave request submitted by an employee.
 *
 * <p>Lifecycle: PENDING → APPROVED or REJECTED
 *              PENDING → CANCELLED (by employee before decision)
 *
 * <p>Business rules enforced in the service layer:
 * <ul>
 *   <li>No overlapping leave requests for the same employee</li>
 *   <li>Start date must not be in the past (for new requests)</li>
 *   <li>End date must be >= start date</li>
 *   <li>Sufficient leave balance must exist (except UNPAID)</li>
 * </ul>
 */
@Entity
@Table(
    name = "leave_requests",
    indexes = {
        @Index(name = "idx_leave_employee",   columnList = "employee_id"),
        @Index(name = "idx_leave_status",     columnList = "status"),
        @Index(name = "idx_leave_type",       columnList = "leave_type"),
        @Index(name = "idx_leave_dates",      columnList = "start_date, end_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee who submitted the leave request.
     * @ManyToOne: Multiple leave requests per employee.
     * This side owns the FK (employee_id on leave_requests table).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Total number of leave days (calculated in service layer).
     * Stored to avoid recalculating and for historical accuracy
     * even if business rules change.
     */
    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    /**
     * Reason provided by the employee for the leave.
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    /**
     * The reviewer (manager/HR) who acted on this request.
     * Null if request is still PENDING.
     * @ManyToOne: Multiple requests can be reviewed by the same person.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    /**
     * Timestamp when the request was approved or rejected.
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * Notes or reason from the reviewer (e.g., reason for rejection).
     */
    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

