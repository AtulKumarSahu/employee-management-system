package com.ems.employeemanagement.mapper;

import com.ems.employeemanagement.dto.request.DepartmentRequest;
import com.ems.employeemanagement.dto.response.DepartmentResponse;
import com.ems.employeemanagement.entity.Department;
import com.ems.employeemanagement.entity.Employee;
import org.mapstruct.*;

/**
 * MapStruct mapper: Department entity ↔ DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DepartmentMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "manager",   ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "active",    constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Department toEntity(DepartmentRequest request);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "manager",   ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "active",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(DepartmentRequest request, @MappingTarget Department department);

    @Mapping(target = "employeeCount", constant = "0L")
    @Mapping(target = "manager", source = "manager")
    DepartmentResponse toResponse(Department department);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", expression = "java(employee != null ? employee.getFullName() : null)")
    @Mapping(target = "email", source = "email")
    DepartmentResponse.ManagerSummary toManagerSummary(Employee employee);
}
