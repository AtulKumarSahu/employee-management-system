package com.ems.employeemanagement.entity;

import com.ems.employeemanagement.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a single day's attendance record for an employee.
 *
 * <p>One record per employee per working day. The unique constraint on
 * (employee_id, attendance_date) prevents duplicate records.
 *
 * <p>Working hours are calculated from checkInTime and checkOutTime.
 * Late arrival is determined by comparing checkInTime against the
 * configured shift start time (e.g., 09:00).
 */
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = {
        // Prevents duplicate attendance records for the same employee on the same day
        @UniqueConstraint(
            name = "uq_attendance_emp_date",
            columnNames = {"employee_id", "attendance_date"}
        )
    },
    indexes = {
        @Index(name = "idx_attendance_employee", columnList = "employee_id"),
        @Index(name = "idx_attendance_date",     columnList = "attendance_date"),
        @Index(name = "idx_attendance_emp_date", columnList = "employee_id, attendance_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * The date this attendance record corresponds to.
     * LocalDate: date without time — no timezone complexity.
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /**
     * Time the employee checked in. Null if absent.
     * LocalTime: time without date — simple, clean.
     */
    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    /**
     * Time the employee checked out. Null if not checked out yet.
     */
    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    /**
     * Working hours for the day — computed from checkIn/checkOut.
     * Stored as DECIMAL(4,2): e.g., 8.50 = 8 hours 30 minutes.
     * Storing avoids recalculation in reports.
     */
    @Column(name = "working_hours", precision = 4, scale = 2)
    private Double workingHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    /**
     * Whether the employee arrived late (checked in after grace period).
     * Determined in service layer and stored for reporting.
     */
    @Column(name = "is_late", nullable = false)
    @Builder.Default
    private boolean late = false;

    /**
     * HR notes — e.g., "Official duty outside office", "On-site client visit".
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

