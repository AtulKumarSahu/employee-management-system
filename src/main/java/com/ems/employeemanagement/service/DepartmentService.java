package com.ems.employeemanagement.service;

import com.ems.employeemanagement.dto.request.DepartmentRequest;
import com.ems.employeemanagement.dto.response.DepartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract for Department business operations.
 *
 * <p>Programming to an interface (not the implementation) is a core SOLID principle
 * (Dependency Inversion). Controllers depend on this interface, not on
 * DepartmentServiceImpl. Benefits:
 * <ul>
 *   <li>Easier to swap implementations (e.g., caching layer, test stub)</li>
 *   <li>Cleaner dependency injection</li>
 *   <li>Mockito can mock interfaces for unit tests</li>
 * </ul>
 */
public interface DepartmentService {

    /**
     * Creates a new department.
     *
     * @param request the department creation request
     * @return the created department response
     * @throws com.ems.employeemanagement.exception.DuplicateResourceException if name or code exists
     */
    DepartmentResponse createDepartment(DepartmentRequest request);

    /**
     * Returns a paginated list of all active departments.
     */
    Page<DepartmentResponse> getAllDepartments(Pageable pageable);

    /**
     * Returns all active departments as a flat list (for dropdowns).
     */
    List<DepartmentResponse> getAllDepartmentsList();

    /**
     * Returns a department by ID.
     *
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException if not found
     */
    DepartmentResponse getDepartmentById(Long id);

    /**
     * Updates an existing department.
     *
     * @throws com.ems.employeemanagement.exception.ResourceNotFoundException  if not found
     * @throws com.ems.employeemanagement.exception.DuplicateResourceException if name conflicts
     */
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    /**
     * Deletes a department (hard delete — only if no active employees).
     *
     * @throws com.ems.employeemanagement.exception.InvalidRequestException if employees exist
     */
    void deleteDepartment(Long id);

    /**
     * Assigns a manager to a department.
     *
     * @param departmentId the department to update
     * @param managerId    the employee ID of the new manager
     */
    DepartmentResponse assignManager(Long departmentId, Long managerId);
}

