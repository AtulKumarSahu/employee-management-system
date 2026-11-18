package com.ems.employeemanagement.repository;

import com.ems.employeemanagement.entity.LeaveBalance;
import com.ems.employeemanagement.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link LeaveBalance} entity.
 */
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    /**
     * Find balance for a specific employee, leave type and year.
     * Used when checking if an employee has sufficient balance.
     */
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndLeaveYear(
            Long employeeId, LeaveType leaveType, int leaveYear);

    /**
     * Find all balances for an employee for the current year.
     */
    List<LeaveBalance> findByEmployeeIdAndLeaveYear(Long employeeId, int leaveYear);

    /**
     * Check if any balance exists for an employee (used during initialization).
     */
    boolean existsByEmployeeIdAndLeaveYear(Long employeeId, int leaveYear);
}
