package com.salary.backend_salary.repository.employee;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.salary.backend_salary.entity.employee.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
                                            QuerydslPredicateExecutor<Employee>,
                                            EmployeeRepositoryCustom { 

    boolean existsByNpp(String npp);

    @EntityGraph(attributePaths = {"departmen", "submitBy"})
    Optional<Employee> findWithDetailsById(Long id);
}