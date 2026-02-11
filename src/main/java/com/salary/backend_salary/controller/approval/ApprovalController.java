package com.salary.backend_salary.controller.approval;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.service.employee.ApprovalService;
import com.salary.backend_salary.vm.employee.ApprovalRequestVM;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPROVER')")
public class ApprovalController {
    private final ApprovalService approvalService;

    @PutMapping("/employees/{id}")
    public ResponseEntity<?> processApproval(
            @PathVariable Long id,
            @RequestBody ApprovalRequestVM vm) {
        
        EmployeeDPO result = approvalService.processApproval(id, vm);

        if (result == null) {
            return ResponseEntity.ok().body("Data has been permanently deleted (APPROVED DELETE).");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/employees/{id}")
    public EmployeeDPO getDetail(@PathVariable Long id) {
    return approvalService.getDetail(id);
}

}
