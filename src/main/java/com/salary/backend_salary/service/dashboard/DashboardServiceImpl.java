package com.salary.backend_salary.service.dashboard;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salary.backend_salary.dto.dashboard.DashboardEmployeeDTO;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.repository.dashboard.DashboardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    @Async("dashboardTaskExecutor")
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<DashboardEmployeeDTO>> getEmployees(
            ApprovalStatus status,
            Integer limit
    ) {
        int safeLimit = normalizeLimit(limit);

        List<Employee> employees = (limit == null)
                ? dashboardRepository.findAllEmployeesByStatus(status)
                : dashboardRepository.findRecentEmployeesByStatus(status, safeLimit);

        List<DashboardEmployeeDTO> result = employees.stream()
                .map(this::mapToDTO)
                .toList();

        return CompletableFuture.completedFuture(result);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) return 10;
        if (limit > 100) return 100;
        return limit;
    }

    private DashboardEmployeeDTO mapToDTO(Employee employee) {
        return new DashboardEmployeeDTO(
                employee.getName(),
                employee.getPosition(),
                employee.getDepartmen() != null
                        ? employee.getDepartmen().getName()
                        : "N/A"
        );
    }
}
