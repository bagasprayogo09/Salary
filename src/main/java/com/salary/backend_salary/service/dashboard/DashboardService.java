package com.salary.backend_salary.service.dashboard;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.salary.backend_salary.dto.dashboard.DashboardEmployeeDTO;
import com.salary.backend_salary.dto.dashboard.DashboardSalaryByDivisionDTO;
import com.salary.backend_salary.dto.dashboard.DashboardSummaryDTO;
import com.salary.backend_salary.enums.ApprovalStatus;

public interface DashboardService {

    CompletableFuture<List<DashboardEmployeeDTO>> getEmployees(
            ApprovalStatus status,
            Integer limit
    );

    CompletableFuture<List<DashboardSalaryByDivisionDTO>> getSalaryByDivision();
    CompletableFuture<DashboardSummaryDTO> getSummary();
}