package com.ems.employeemanagement.repository;

import com.ems.employeemanagement.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Attendance} entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Find today's attendance record for an employee.
     * Used to validate that check-in doesn't happen twice on the same day.
     */
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    /**
     * Check if an attendance record already exists for today.
     */
    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    /**
     * Find all attendance records for an employee (paginated).
     */
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Find attendance records for an employee in a date range.
     * Used for monthly summary and working hours calculation.
     */
    @Query("""
            SELECT a FROM Attendance a
            WHERE a.employee.id = :employeeId
              AND a.attendanceDate BETWEEN :from AND :to
            ORDER BY a.attendanceDate ASC
            """)
    List<Attendance> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /**
     * Monthly attendance summary: count records by status for an employee.
     * Returns a custom projection as Object[] — mapped in service layer.
     *
     * <p>
     * Alternative: Use a dedicated interface-based projection (Phase 9 refinement).
     */
    @Query("""
            SELECT a.status, COUNT(a)
            FROM Attendance a
            WHERE a.employee.id = :employeeId
              AND YEAR(a.attendanceDate)  = :year
              AND MONTH(a.attendanceDate) = :month
            GROUP BY a.status
            """)
    List<Object[]> countByStatusForMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    /**
     * Calculate total working hours for an employee in a month.
     */
    @Query("""
            SELECT COALESCE(SUM(a.workingHours), 0)
            FROM Attendance a
            WHERE a.employee.id = :employeeId
              AND YEAR(a.attendanceDate)  = :year
              AND MONTH(a.attendanceDate) = :month
            """)
    Double sumWorkingHoursForMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);
}
