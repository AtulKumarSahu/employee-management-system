package com.ems.employeemanagement.mapper;

import com.ems.employeemanagement.dto.request.EmployeeRequest;
import com.ems.employeemanagement.dto.request.EmployeeUpdateRequest;
import com.ems.employeemanagement.dto.response.EmployeeResponse;
import com.ems.employeemanagement.entity.Department;
import com.ems.employeemanagement.entity.Employee;
import com.ems.employeemanagement.enums.EmploymentStatus;
import org.mapstruct.*;

/**
 * MapStruct mapper: Employee entity ↔ DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = EmploymentStatus.class
)
public interface EmployeeMapper {

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "employeeCode",    ignore = true)
    @Mapping(target = "department",      ignore = true)
    @Mapping(target = "manager",         ignore = true)
    @Mapping(target = "user",            ignore = true)
    @Mapping(target = "directReports",   ignore = true)
    @Mapping(target = "leaveRequests",   ignore = true)
    @Mapping(target = "leaveBalances",   ignore = true)
    @Mapping(target = "attendanceRecords", ignore = true)
    @Mapping(target = "deleted",         ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(
        target = "employmentStatus",
        expression = "java(request.employmentStatus() != null ? request.employmentStatus() : EmploymentStatus.ACTIVE)"
    )
    Employee toEntity(EmployeeRequest request);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "employeeCode",    ignore = true)
    @Mapping(target = "email",           ignore = true)
    @Mapping(target = "department",      ignore = true)
    @Mapping(target = "manager",         ignore = true)
    @Mapping(target = "user",            ignore = true)
    @Mapping(target = "directReports",   ignore = true)
    @Mapping(target = "leaveRequests",   ignore = true)
    @Mapping(target = "leaveBalances",   ignore = true)
    @Mapping(target = "attendanceRecords", ignore = true)
    @Mapping(target = "deleted",         ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    void updateEntityFromRequest(EmployeeUpdateRequest request, @MappingTarget Employee employee);

    @Mapping(target = "fullName",   expression = "java(employee != null ? employee.getFullName() : null)")
    @Mapping(target = "userId",     source = "user.id")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "manager",    source = "manager")
    EmployeeResponse toResponse(Employee employee);

    @Mapping(target = "id",   source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    EmployeeResponse.DepartmentSummary toDepartmentSummary(Department department);

    @Mapping(target = "id",           source = "id")
    @Mapping(target = "employeeCode", source = "employeeCode")
    @Mapping(target = "fullName",     expression = "java(manager != null ? manager.getFullName() : null)")
    @Mapping(target = "designation",  source = "designation")
    EmployeeResponse.ManagerSummary toManagerSummary(Employee manager);
}
