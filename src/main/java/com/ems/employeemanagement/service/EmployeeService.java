package com.ems.employeemanagement.service;

import com.ems.employeemanagement.dto.request.EmployeeRequest;
import com.ems.employeemanagement.dto.request.EmployeeUpdateRequest;
import com.ems.employeemanagement.dto.response.EmployeeResponse;
import com.ems.employeemanagement.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for Employee business operations.
 */
public interface EmployeeService {

    /**
     * Creates a new employee record.
     *
     * @throws com.ems.employeemanagement.exception.DuplicateResourceException if email exists
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException  if department/manager not found
     */
    EmployeeResponse createEmployee(EmployeeRequest request);

    /**
     * Returns a paginated list of all active (non-deleted) employees.
     */
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);

    /**
     * Returns an employee by their database ID.
     *
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException if not found
     */
    EmployeeResponse getEmployeeById(Long id);

    /**
     * Returns an employee by their unique employee code.
     *
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException if not found
     */
    EmployeeResponse getEmployeeByCode(String code);

    /**
     * Updates an employee's profile.
     *
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException if not found
     */
    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);

    /**
     * Soft-deletes an employee (sets is_deleted = true, status = TERMINATED).
     *
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException if not found
     */
    void deleteEmployee(Long id);

    /**
     * Searches employees by name or email keyword with pagination.
     */
    Page<EmployeeResponse> searchEmployees(String keyword, Pageable pageable);

    /**
     * Filters employees by optional department and/or status.
     */
    Page<EmployeeResponse> filterEmployees(Long departmentId, EmploymentStatus status, Pageable pageable);

    /**
     * Returns all direct reports of a manager, paginated.
     *
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException if manager not found
     */
    Page<EmployeeResponse> getTeamMembers(Long managerId, Pageable pageable);
}

