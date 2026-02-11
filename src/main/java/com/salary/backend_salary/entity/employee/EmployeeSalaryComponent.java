package com.salary.backend_salary.entity.employee;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.salary.backend_salary.entity.salary.SalaryComponent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "employee_salary_component")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "component_id", nullable = false)
    private SalaryComponent salaryComponent;

    @Column(nullable = false)
    private BigDecimal amount;
}
