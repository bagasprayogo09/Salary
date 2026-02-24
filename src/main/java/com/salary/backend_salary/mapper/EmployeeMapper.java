package com.salary.backend_salary.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.entity.employee.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "departmen.id", target = "departmentId")
    @Mapping(source = "departmen.name", target = "departmentName")
    @Mapping(source = "submitBy.username", target = "submitByName")
    EmployeeDPO toDpo(Employee employee);
} 
