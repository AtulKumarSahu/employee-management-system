package com.ems.employeemanagement.mapper;

import com.ems.employeemanagement.dto.response.AttendanceResponse;
import com.ems.employeemanagement.entity.Attendance;
import com.ems.employeemanagement.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for converting Attendance entities to AttendanceResponse records.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AttendanceMapper {

    @Mapping(target = "employee", source = "employee")
    AttendanceResponse toResponse(Attendance attendance);

    @Mapping(target = "id",           source = "id")
    @Mapping(target = "employeeCode", source = "employeeCode")
    @Mapping(target = "fullName",     expression = "java(employee != null ? employee.getFullName() : null)")
    @Mapping(target = "email",        source = "email")
    AttendanceResponse.EmployeeSummary toEmployeeSummary(Employee employee);
}
