package com.salary.backend_salary.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSalaryByDivisionDTO(
    String division,
    Long employeeCount,
    BigDecimal totalSalary
) {} 
