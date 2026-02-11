package com.salary.backend_salary.repository.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.salary.backend_salary.entity.employee.Employee;

public interface DashboardRepository extends JpaRepository<Employee, Long>, QuerydslPredicateExecutor<Employee>, DashboardRepositoryCustom{
    
}
