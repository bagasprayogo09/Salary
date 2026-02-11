package com.salary.backend_salary.repository.dashboard;

import java.util.List;

import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.enums.ApprovalStatus;

public interface DashboardRepositoryCustom {
    
    List<Employee> findRecentEmployeesByStatus(ApprovalStatus status, int limit);
    
    List<Employee> findAllEmployeesByStatus(ApprovalStatus status);
}