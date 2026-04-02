package com.salary.backend_salary.dto.dashboard;

public record DashboardSummaryDTO(
    Long totalEmployees,
    Long totalDivisions,
    Long pendingApprovals,  
    Double totalSalary
) {}
