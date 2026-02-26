package com.salary.backend_salary.controller.dashboard;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.salary.backend_salary.dto.dashboard.DashboardEmployeeDTO;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.service.dashboard.DashboardService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Validated
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/recent-employees")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public CompletableFuture<ResponseEntity<List<DashboardEmployeeDTO>>> getRecentEmployees(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return dashboardService
                .getEmployees(ApprovalStatus.APPROVED, limit)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    return ResponseEntity.internalServerError().build();
                });
    }
}
