package com.salary.backend_salary.service.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.vm.employee.EmployeeRequestVM;
import com.salary.backend_salary.vm.employee.EmployeeVM;

public interface EmployeeService {

    EmployeeDPO createEmployee(EmployeeRequestVM vm);
    EmployeeDPO getEmployeeById(Long id);
    EmployeeDPO updateEmployee(Long id, EmployeeRequestVM vm);
    void deleteEmployee(Long id);

    Page<EmployeeDPO> searchEmployees(EmployeeVM vm, Pageable pageable);
    
}
