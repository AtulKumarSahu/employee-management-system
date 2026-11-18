package com.ems.employeemanagement.controller;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.EmployeeRequest;
import com.ems.employeemanagement.dto.request.EmployeeUpdateRequest;
import com.ems.employeemanagement.dto.response.ApiResponse;
import com.ems.employeemanagement.dto.response.EmployeeResponse;
import com.ems.employeemanagement.dto.response.PaginationResponse;
import com.ems.employeemanagement.enums.EmploymentStatus;
import com.ems.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Employee management endpoints.
 *
 * <p>All routes are under /api/v1/employees.
 * Role-based access control is enforced per endpoint via @PreAuthorize.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/employees")
@Slf4j
@Tag(name = "Employee Management", description = "APIs for managing employee records")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * POST /api/v1/employees — Create a new employee.
     * Access: ADMIN, HR
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create a new employee",
               description = "Creates a new employee record. Requires ADMIN or HR role.")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        log.info("POST /employees — email: {}", request.email());
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response));
    }

    /**
     * GET /api/v1/employees — Get all employees (paginated).
     * Supports optional search and filter query parameters:
     *   - search: keyword to search by name or email
     *   - departmentId: filter by department
     *   - status: filter by employment status
     * Access: ADMIN, HR, MANAGER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get all employees with optional search/filter and pagination")
    public ResponseEntity<ApiResponse<PaginationResponse<EmployeeResponse>>> getAllEmployees(
            @Parameter(description = "Search keyword (name or email)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by department ID")
            @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Filter by employment status")
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EmployeeResponse> pageResult;

        if (search != null && !search.isBlank()) {
            // Search mode: keyword search overrides department/status filters
            pageResult = employeeService.searchEmployees(search.trim(), pageable);
        } else if (departmentId != null || status != null) {
            // Filter mode: filter by department and/or status
            pageResult = employeeService.filterEmployees(departmentId, status, pageable);
        } else {
            // Default: paginated list of all employees
            pageResult = employeeService.getAllEmployees(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Employees retrieved successfully",
                PaginationResponse.from(pageResult)
        ));
    }

    /**
     * GET /api/v1/employees/{id} — Get employee by ID.
     * Access: ADMIN, HR, MANAGER, EMPLOYEE (own record — enforced in Phase 6)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {

        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", response));
    }

    /**
     * GET /api/v1/employees/code/{code} — Get employee by employee code.
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get employee by employee code (e.g. EMP-000001)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByCode(
            @PathVariable String code) {

        EmployeeResponse response = employeeService.getEmployeeByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", response));
    }

    /**
     * PUT /api/v1/employees/{id} — Update employee details.
     * Access: ADMIN, HR
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update an employee's profile")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {

        log.info("PUT /employees/{}", id);
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    /**
     * DELETE /api/v1/employees/{id} — Soft-delete an employee.
     * Access: ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete an employee (sets status to TERMINATED)")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        log.info("DELETE /employees/{}", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully"));
    }

    /**
     * GET /api/v1/employees/{id}/team — Get direct reports of a manager.
     * Access: ADMIN, HR, MANAGER
     */
    @GetMapping("/{id}/team")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get direct reports (team members) of a manager")
    public ResponseEntity<ApiResponse<PaginationResponse<EmployeeResponse>>> getTeamMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<EmployeeResponse> pageResult = employeeService.getTeamMembers(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(
                "Team members retrieved successfully",
                PaginationResponse.from(pageResult)
        ));
    }
}

