package com.ems.employeemanagement.repository;

import com.ems.employeemanagement.entity.LeaveRequest;
import com.ems.employeemanagement.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link LeaveRequest} entity.
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find all leave requests for a specific employee (paginated).
     */
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Find all pending leave requests — for HR/Manager dashboard.
     */
    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    /**
     * Find leave requests for an employee filtered by status.
     */
    Page<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status, Pageable pageable);

    /**
     * Check for overlapping leave requests.
     *
     * <p>
     * Overlap condition: existing request's date range intersects with new
     * request's range.
     * The overlap formula: existing.startDate <= newEndDate AND existing.endDate >=
     * newStartDate
     *
     * <p>
     * Only PENDING and APPROVED requests are checked — REJECTED and CANCELLED don't
     * block.
     * excludeId allows excluding the current request when updating (edit
     * scenarios).
     */
    @Query("""
            SELECT COUNT(lr) > 0 FROM LeaveRequest lr
            WHERE lr.employee.id = :employeeId
              AND lr.id <> :excludeId
              AND lr.status IN ('PENDING', 'APPROVED')
              AND lr.startDate <= :endDate
              AND lr.endDate   >= :startDate
            """)
    boolean hasOverlappingLeave(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);

    /**
     * Find all approved leave requests for an employee in a date range.
     * Used for monthly/annual leave reports.
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.employee.id = :employeeId
              AND lr.status = 'APPROVED'
              AND lr.startDate >= :from
              AND lr.endDate   <= :to
            """)
    List<LeaveRequest> findApprovedLeavesInRange(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /**
     * Count pending requests for an employee — used for dashboard badges.
     */
    long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
}
