package com.ems.employeemanagement.service.impl;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.EmployeeRequest;
import com.ems.employeemanagement.dto.request.EmployeeUpdateRequest;
import com.ems.employeemanagement.dto.response.EmployeeResponse;
import com.ems.employeemanagement.entity.Department;
import com.ems.employeemanagement.entity.Employee;
import com.ems.employeemanagement.enums.EmploymentStatus;
import com.ems.employeemanagement.exception.DuplicateResourceException;
import com.ems.employeemanagement.exception.ResourceNotFoundException;
import com.ems.employeemanagement.mapper.EmployeeMapper;
import com.ems.employeemanagement.repository.DepartmentRepository;
import com.ems.employeemanagement.repository.EmployeeRepository;
import com.ems.employeemanagement.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link EmployeeService}.
 *
 * <p>Key responsibilities:
 * <ol>
 *   <li>Validate business rules before persisting</li>
 *   <li>Generate employee codes</li>
 *   <li>Resolve department and manager references from IDs</li>
 *   <li>Delegate persistence to repositories</li>
 *   <li>Map entities to response DTOs</li>
 * </ol>
 */
@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository   employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper       employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                                DepartmentRepository departmentRepository,
                                EmployeeMapper employeeMapper) {
        this.employeeRepository   = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeMapper       = employeeMapper;
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating employee with email: {}", request.email());

        // 1. Validate email uniqueness
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Employee", "email", request.email());
        }

        // 2. Map DTO → entity (excludes department/manager — set below)
        Employee employee = employeeMapper.toEntity(request);

        // 3. Generate a unique employee code (EMP-000001 format)
        employee.setEmployeeCode(generateEmployeeCode());

        // 4. Resolve department reference if provided
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.departmentId()));
            employee.setDepartment(department);
        }

        // 5. Resolve manager reference if provided
        if (request.managerId() != null) {
            Employee manager = employeeRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee (Manager)", "id", request.managerId()));
            employee.setManager(manager);
        }

        // 6. Persist
        Employee saved = employeeRepository.save(employee);

        log.info("Employee created: id={}, code={}, email={}",
                saved.getId(), saved.getEmployeeCode(), saved.getEmail());

        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.debug("Fetching all employees — page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // @Where on Employee automatically filters soft-deleted records
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee by id: {}", id);
        return employeeMapper.toResponse(findEmployeeByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String code) {
        log.debug("Fetching employee by code: {}", code);
        Employee employee = employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", code));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        log.info("Updating employee id: {}", id);

        // 1. Load existing employee
        Employee employee = findEmployeeByIdOrThrow(id);

        // 2. Apply non-null fields from request to entity (via MapStruct null-ignore strategy)
        employeeMapper.updateEntityFromRequest(request, employee);

        // 3. Update department if a new departmentId is provided
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.departmentId()));
            employee.setDepartment(department);
        }

        // 4. Update manager if a new managerId is provided
        if (request.managerId() != null) {
            Employee manager = employeeRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee (Manager)", "id", request.managerId()));
            employee.setManager(manager);
        }

        Employee updated = employeeRepository.save(employee);

        log.info("Employee updated: id={}", id);

        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Soft-deleting employee id: {}", id);

        Employee employee = findEmployeeByIdOrThrow(id);

        // Update employment status before soft-delete for data consistency
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);

        // @SQLDelete on Employee converts the save → DELETE into an UPDATE is_deleted = true
        employeeRepository.delete(employee);

        log.info("Employee soft-deleted: id={}, code={}", id, employee.getEmployeeCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(String keyword, Pageable pageable) {
        log.debug("Searching employees by keyword: '{}'", keyword);
        return employeeRepository.searchByKeyword(keyword, pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> filterEmployees(Long departmentId,
                                                   EmploymentStatus status,
                                                   Pageable pageable) {
        log.debug("Filtering employees — departmentId: {}, status: {}", departmentId, status);
        return employeeRepository.findByFilters(departmentId, status, pageable)
                .map(employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getTeamMembers(Long managerId, Pageable pageable) {
        log.debug("Fetching team members for manager id: {}", managerId);

        // Verify manager exists
        findEmployeeByIdOrThrow(managerId);

        return employeeRepository.findByManagerId(managerId, pageable)
                .map(employeeMapper::toResponse);
    }

    // ==================== Private Helpers ====================

    private Employee findEmployeeByIdOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    /**
     * Generates a unique employee code in the format EMP-000001.
     *
     * <p>Strategy: count all employees (including deleted) and pad to 6 digits.
     * In a high-concurrency environment, consider using a DB sequence instead.
     * This is sufficient for a portfolio/internship-level application.
     *
     * @return a unique employee code string
     */
    private String generateEmployeeCode() {
        // Count total employees including soft-deleted to avoid code reuse
        long count = employeeRepository.count() + 1;
        return String.format("%s%06d", AppConstants.EMPLOYEE_CODE_PREFIX, count);
    }
}

