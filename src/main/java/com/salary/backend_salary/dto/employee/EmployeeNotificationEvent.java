package com.salary.backend_salary.dto.employee;

import com.salary.backend_salary.enums.ApprovalStatus;

public record EmployeeNotificationEvent(
    Long EmployeeId,
    String EmployeeName,
    ApprovalStatus status,
    String message,
    String targetRole //"APPROVER" or "ADMIN"
) {}
