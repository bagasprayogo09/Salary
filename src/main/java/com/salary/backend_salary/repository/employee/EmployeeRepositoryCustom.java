package com.salary.backend_salary.repository.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;
import com.salary.backend_salary.entity.employee.Employee;

public interface EmployeeRepositoryCustom {
        Page<Employee> searchWithDetails(Predicate predicate, Pageable pageable);

}
