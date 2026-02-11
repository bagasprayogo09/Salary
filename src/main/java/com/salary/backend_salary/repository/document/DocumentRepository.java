package com.salary.backend_salary.repository.document;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.salary.backend_salary.entity.document.DocumentSubmission;
import com.salary.backend_salary.entity.employee.Employee;

public interface DocumentRepository extends JpaRepository<DocumentSubmission, Long>{
    List<DocumentSubmission> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    Optional<DocumentSubmission> findTopByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    @Query("SELECT DISTINCT d.employee FROM DocumentSubmission d " +
           "WHERE UPPER(d.filename) NOT LIKE '%FINAL%' " +
           "AND NOT EXISTS (SELECT d2 FROM DocumentSubmission d2 WHERE d2.employee = d.employee AND UPPER(d2.filename) LIKE '%FINAL%')")
    List<Employee> findTargetsFromDocuments(Pageable pageable);
}
