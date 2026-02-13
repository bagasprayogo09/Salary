package com.salary.backend_salary.repository.salary;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.salary.backend_salary.entity.salary.Salary;

public interface SalaryRepository
        extends JpaRepository<Salary, Long>,
                QuerydslPredicateExecutor<Salary> {

    Optional<Salary> findByEmployee_IdAndMonth(Long employeeId, String month);

    Page<Salary> findByEmployee_Id(Long employeeId, Pageable pageable);
}
