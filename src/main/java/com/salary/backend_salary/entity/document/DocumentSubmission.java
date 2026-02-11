package com.salary.backend_salary.entity.document;

import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.employee.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_document_submissions")
@Data
public class DocumentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String documentContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    private LocalDateTime createdAt;
}