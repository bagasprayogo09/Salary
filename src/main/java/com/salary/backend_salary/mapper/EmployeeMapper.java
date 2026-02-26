package com.salary.backend_salary.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.vm.employee.EmployeeRequestVM;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "departmen.id", target = "departmentId")
    @Mapping(source = "departmen.name", target = "departmentName")
    @Mapping(source = "submitBy.username", target = "submitByName")
   
    EmployeeDPO toDpo(Employee employee);

    
    Employee copy(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departmen", ignore = true)
    @Mapping(target = "submitBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    
    
    @Mapping(target = "salaryComponents", ignore = true) 
    
    void updateEntityFromVm(EmployeeRequestVM vm, @MappingTarget Employee employee);
}