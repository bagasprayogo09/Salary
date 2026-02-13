package com.salary.backend_salary.repository.salary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.salary.backend_salary.entity.salary.SalaryComponent;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {
    
    List<SalaryComponent> findByType(String type);
}
