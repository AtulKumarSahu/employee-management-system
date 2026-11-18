package com.ems.employeemanagement.service.impl;

import com.ems.employeemanagement.dto.request.DepartmentRequest;
import com.ems.employeemanagement.dto.response.DepartmentResponse;
import com.ems.employeemanagement.entity.Department;
import com.ems.employeemanagement.entity.Employee;
import com.ems.employeemanagement.exception.DuplicateResourceException;
import com.ems.employeemanagement.exception.InvalidRequestException;
import com.ems.employeemanagement.exception.ResourceNotFoundException;
import com.ems.employeemanagement.mapper.DepartmentMapper;
import com.ems.employeemanagement.repository.DepartmentRepository;
import com.ems.employeemanagement.repository.EmployeeRepository;
import com.ems.employeemanagement.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link DepartmentService}.
 *
 * <p>
 * Constructor-based dependency injection (no @Autowired on fields).
 * With a single constructor, Spring injects dependencies automatically.
 * Benefits: immutable fields, testability, no hidden dependencies.
 *
 * <p>
 * @Transactional at method level:
 * - readOnly = true: optimization hint for read operations (Hibernate skips
 * dirty-checking, DB may use read replicas). Applied to all GET methods.
 * - Default (read-write): for create/update/delete — ensures rollback on
 * exception.
 *
 * <p>
 * @Service: marks this as a Spring service component (business layer).
 * 
 * @Slf4j: Lombok generates the SLF4J logger field.
 */
@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    // Constructor injection — Spring resolves all parameters automatically
    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        log.info("Creating department with name: {}", request.name());

        // 1. Business rule: department name must be unique
        if (departmentRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Department", "name", request.name());
        }

        // 2. Business rule: department code must be unique (if provided)
        if (request.code() != null && !request.code().isBlank()
                && departmentRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Department", "code", request.code());
        }

        // 3. Map DTO → entity
        Department department = departmentMapper.toEntity(request);

        // 4. Persist (Hibernate generates INSERT SQL, returns entity with generated id)
        Department saved = departmentRepository.save(department);

        log.info("Department created successfully: id={}, name={}", saved.getId(), saved.getName());

        // 5. Map entity → response DTO and return
        DepartmentResponse response = departmentMapper.toResponse(saved);

        // Set employee count to 0 (new department has no employees)
        return buildResponseWithCount(response, saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        log.debug("Fetching all departments — page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return departmentRepository.findByActiveTrue(pageable)
                .map(dept -> {
                    DepartmentResponse response = departmentMapper.toResponse(dept);
                    return buildResponseWithCount(response, dept.getId());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartmentsList() {
        return departmentRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(dept -> {
                    DepartmentResponse response = departmentMapper.toResponse(dept);
                    return buildResponseWithCount(response, dept.getId());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        log.debug("Fetching department by id: {}", id);

        Department department = findDepartmentByIdOrThrow(id);
        DepartmentResponse response = departmentMapper.toResponse(department);
        return buildResponseWithCount(response, id);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        log.info("Updating department id: {} with name: {}", id, request.name());

        // 1. Load existing entity — throw 404 if not found
        Department department = findDepartmentByIdOrThrow(id);

        // 2. Duplicate name check — exclude the current department itself
        if (departmentRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Department", "name", request.name());
        }

        // 3. Update entity fields using MapStruct (null fields in request are ignored)
        departmentMapper.updateEntityFromRequest(request, department);

        // 4. Save and return
        Department updated = departmentRepository.save(department);

        log.info("Department updated: id={}", id);

        DepartmentResponse response = departmentMapper.toResponse(updated);
        return buildResponseWithCount(response, id);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department id: {}", id);

        // 1. Confirm exists
        Department department = findDepartmentByIdOrThrow(id);

        // 2. Business rule: cannot delete a department with active employees
        boolean hasEmployees = employeeRepository.existsByDepartmentIdAndDeletedFalse(id);
        if (hasEmployees) {
            throw new InvalidRequestException(
                    "Cannot delete department '" + department.getName()
                            + "' because it still has active employees. "
                            + "Please reassign or terminate all employees first.");
        }

        // 3. Soft-disable instead of hard delete — retain department in history
        department.setActive(false);
        departmentRepository.save(department);

        log.info("Department soft-deleted: id={}, name={}", id, department.getName());
    }

    @Override
    @Transactional
    public DepartmentResponse assignManager(Long departmentId, Long managerId) {
        log.info("Assigning manager id: {} to department id: {}", managerId, departmentId);

        Department department = findDepartmentByIdOrThrow(departmentId);
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", managerId));

        department.setManager(manager);
        Department updated = departmentRepository.save(department);

        log.info("Manager assigned: employee={} → department={}", managerId, departmentId);

        DepartmentResponse response = departmentMapper.toResponse(updated);
        return buildResponseWithCount(response, departmentId);
    }

    // ==================== Private Helpers ====================

    /**
     * Looks up a department by id, throwing a consistent 404 if not found.
     * Extracted to avoid repeating the orElseThrow pattern.
     */
    private Department findDepartmentByIdOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    /**
     * Builds a DepartmentResponse with the actual employee count.
     * Since MapStruct sets employeeCount = 0, we override it here.
     * Records are immutable so we reconstruct with the correct count.
     */
    private DepartmentResponse buildResponseWithCount(DepartmentResponse response, Long departmentId) {
        long count = employeeRepository.countByDepartmentId(departmentId);
        return new DepartmentResponse(
                response.id(),
                response.name(),
                response.description(),
                response.code(),
                response.active(),
                response.manager(),
                count,
                response.createdAt(),
                response.updatedAt());
    }
}
