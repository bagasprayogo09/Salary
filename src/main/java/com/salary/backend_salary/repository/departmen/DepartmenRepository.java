package com.salary.backend_salary.repository.departmen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.salary.backend_salary.entity.departmen.Department;

@Repository
public interface DepartmenRepository extends JpaRepository<Department, Long>{

    
} 
