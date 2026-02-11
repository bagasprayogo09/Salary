package com.salary.backend_salary.vm.employee;

import com.salary.backend_salary.enums.ApprovalStatus;

import lombok.Data;

@Data
public class ApprovalRequestVM {
    private Long approverId;
    private ApprovalStatus status;
}
