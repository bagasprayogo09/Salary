package com.salary.backend_salary.entity.salary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.salary.backend_salary.entity.employee.Employee;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "salary")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class Salary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnore 
    private Employee employee;

    private String month;

    private BigDecimal amount;

    @OneToMany(mappedBy = "salary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalarySlipDetail> slipDetails = new ArrayList<>();

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Salary)) return false;
        return id != null && id.equals(((Salary) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}