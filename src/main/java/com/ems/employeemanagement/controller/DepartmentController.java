package com.ems.employeemanagement.controller;

import com.ems.employeemanagement.constants.AppConstants;
import com.ems.employeemanagement.dto.request.DepartmentRequest;
import com.ems.employeemanagement.dto.response.ApiResponse;
import com.ems.employeemanagement.dto.response.DepartmentResponse;
import com.ems.employeemanagement.dto.response.PaginationResponse;
import com.ems.employeemanagement.service.DepartmentService;
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

import java.util.List;

/**
 * REST Controller for Department management endpoints.
 *
 * <p>@RestController = @Controller + @ResponseBody.
 * All methods return response bodies (not view names).
 *
 * <p>@RequestMapping sets the base path for all methods in this controller.
 *
 * <p>No business logic here — the controller:
 * <ol>
 *   <li>Receives HTTP requests</li>
 *   <li>Validates input (@Valid)</li>
 *   <li>Calls the service</li>
 *   <li>Returns ResponseEntity with appropriate HTTP status</li>
 * </ol>
 *
 * <p>@PreAuthorize: method-level security using SpEL expressions.
 * Requires @EnableMethodSecurity in security configuration (Phase 6).
 *
 * @Tag: Swagger UI grouping annotation.
 */
@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/departments")
@Slf4j
@Tag(name = "Department Management", description = "APIs for managing organizational departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * POST /api/v1/departments — Create a new department.
     *
     * <p>@Valid: triggers Jakarta Bean Validation on the @RequestBody.
     * If validation fails, Spring throws MethodArgumentNotValidException
     * which our GlobalExceptionHandler catches and returns a 400 response.
     *
     * <p>ResponseEntity.status(201): HTTP 201 Created is the correct status
     * for successful resource creation (not 200 OK).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create a new department",
               description = "Creates a new organizational department. Requires ADMIN or HR role.")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        log.info("POST /departments — name: {}", request.name());
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", response));
    }

    /**
     * GET /api/v1/departments — Get all departments (paginated).
     *
     * <p>Query parameters (all optional, have defaults):
     * - page: page number, 0-based (default: 0)
     * - size: records per page (default: 10)
     * - sortBy: field to sort by (default: "name")
     * - sortDir: "asc" or "desc" (default: "asc")
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all departments with pagination")
    public ResponseEntity<ApiResponse<PaginationResponse<DepartmentResponse>>> getAllDepartments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DepartmentResponse> pageResult = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(ApiResponse.success(
                "Departments retrieved successfully",
                PaginationResponse.from(pageResult)
        ));
    }

    /**
     * GET /api/v1/departments/list — Get all departments as a flat list (for dropdowns).
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get all departments as a list (for dropdowns)")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartmentsList() {
        List<DepartmentResponse> departments = departmentService.getAllDepartmentsList();
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved", departments));
    }

    /**
     * GET /api/v1/departments/{id} — Get a specific department.
     *
     * <p>@PathVariable: binds the {id} URI template variable to the method parameter.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @Parameter(description = "Department ID") @PathVariable Long id) {

        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", response));
    }

    /**
     * PUT /api/v1/departments/{id} — Update an existing department.
     *
     * <p>PUT = full replacement. All fields in the request body replace the existing ones.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update a department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        log.info("PUT /departments/{}", id);
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", response));
    }

    /**
     * DELETE /api/v1/departments/{id} — Delete a department.
     *
     * <p>Returns HTTP 200 with a message (not 204 No Content) so the client
     * receives a consistent ApiResponse structure even for deletes.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a department (soft delete — only if no active employees)")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        log.info("DELETE /departments/{}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }

    /**
     * PATCH /api/v1/departments/{id}/manager/{managerId} — Assign a manager.
     *
     * <p>PATCH is correct here: we're partially updating the department
     * (only the manager field), not replacing the whole resource.
     */
    @PatchMapping("/{id}/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Assign a manager to a department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> assignManager(
            @PathVariable Long id,
            @PathVariable Long managerId) {

        log.info("PATCH /departments/{}/manager/{}", id, managerId);
        DepartmentResponse response = departmentService.assignManager(id, managerId);
        return ResponseEntity.ok(ApiResponse.success("Manager assigned successfully", response));
    }
}

