package com.salary.backend_salary.dto.salary;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryResponse {
    
    private Long id;
    private String month;
    private BigDecimal amount;
    private String amountFormatted;
    
    private Long employeeId;
    private String employeeName;
    private String employeeNpp;
    private String division;
}
