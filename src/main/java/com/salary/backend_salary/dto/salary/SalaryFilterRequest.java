package com.salary.backend_salary.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryFilterRequest {
    
    private String month;
    private String employeeName;
}
