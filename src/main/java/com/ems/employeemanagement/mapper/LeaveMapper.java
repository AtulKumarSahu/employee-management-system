package com.ems.employeemanagement.mapper;

import com.ems.employeemanagement.dto.response.LeaveBalanceResponse;
import com.ems.employeemanagement.dto.response.LeaveRequestResponse;
import com.ems.employeemanagement.entity.Employee;
import com.ems.employeemanagement.entity.LeaveBalance;
import com.ems.employeemanagement.entity.LeaveRequest;
import com.ems.employeemanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for converting Leave entities into Java Record DTOs.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface LeaveMapper {

    @Mapping(target = "employee",   source = "employee")
    @Mapping(target = "reviewedBy", source = "reviewedBy")
    LeaveRequestResponse toResponse(LeaveRequest leaveRequest);

    @Mapping(target = "id",           source = "id")
    @Mapping(target = "employeeCode", source = "employeeCode")
    @Mapping(target = "fullName",     expression = "java(employee != null ? employee.getFullName() : null)")
    @Mapping(target = "email",        source = "email")
    LeaveRequestResponse.EmployeeSummary toEmployeeSummary(Employee employee);

    @Mapping(target = "id",       source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email",    source = "email")
    LeaveRequestResponse.ReviewerSummary toReviewerSummary(User user);

    @Mapping(target = "employeeId",   source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(leaveBalance.getEmployee() != null ? leaveBalance.getEmployee().getFullName() : null)")
    LeaveBalanceResponse toResponse(LeaveBalance leaveBalance);
}
