package com.salary.backend_salary.entity.employee;

import java.time.LocalDateTime;
import java.util.List;

import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.departmen.Department;
import com.salary.backend_salary.enums.ApprovalStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String position;

    private String email;

    private String npp;

    
    @ManyToOne
    @JoinColumn(name = "departmen_id")
    @ToString.Exclude
    private Department departmen;

    @Enumerated(EnumType.STRING) 
    @Column(name = "status")
    private ApprovalStatus status;

    private String statusemp;

    @ManyToOne
    @JoinColumn(name = "submit_by")
    private AppUser submitBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

   
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<EmployeeSalaryComponent> salaryComponents;
}

