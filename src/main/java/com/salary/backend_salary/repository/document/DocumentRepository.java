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

    @Query("""
            SELECT e
            FROM Employee e
            WHERE EXISTS (
                SELECT d
                FROM DocumentSubmission d
                WHERE d.employee = e
                AND d.createdAt = (
                    SELECT MAX(d2.createdAt)
                    FROM DocumentSubmission d2
                    WHERE d2.employee = e
                )
                AND UPPER(d.filename) NOT LIKE '%FINAL%'
            )
            """)
List<Employee> findTargetsFromDocuments(Pageable pageable);

}
