package com.salary.backend_salary.service.employee;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.user.UserRepository;
import com.salary.backend_salary.service.audit.AuditService;
import com.salary.backend_salary.vm.employee.ApprovalRequestVM;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public EmployeeDPO processApproval(Long employeeId, ApprovalRequestVM vm) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        AppUser approver = userRepository.findById(vm.getApproverId())
                .orElseThrow(() -> new RuntimeException("Approver user not found"));

        if (vm.getStatus() != ApprovalStatus.APPROVED &&
            vm.getStatus() != ApprovalStatus.REJECTED) {
            throw new RuntimeException("Invalid approval action");
        }

        Employee oldState = new Employee();
        oldState.setId(employee.getId());
        oldState.setName(employee.getName());
        oldState.setStatus(employee.getStatus());
        oldState.setDepartmen(employee.getDepartmen());

        ApprovalStatus currentStatus = employee.getStatus();
        ApprovalStatus action = vm.getStatus();
        String auditAction;


        if (currentStatus == ApprovalStatus.PENDING_DELETE) {

            if (action == ApprovalStatus.APPROVED) {
                auditAction = "APPROVE_DELETE";
                auditService.logAudit("EMPLOYEE", employeeId, auditAction, oldState, null, approver);
                employeeRepository.delete(employee);
                return null;
            } else {
                auditAction = "REJECT_DELETE";
                employee.setStatus(ApprovalStatus.APPROVED);
            }

        } else if (currentStatus == ApprovalStatus.PENDING_CREATE) {

            if (action == ApprovalStatus.APPROVED) {
                auditAction = "APPROVE_CREATE";
                employee.setStatus(ApprovalStatus.APPROVED);
            } else {
                auditAction = "REJECT_CREATE";
                employee.setStatus(ApprovalStatus.REJECTED);
            }

        } else if (currentStatus == ApprovalStatus.PENDING_UPDATE) {

            if (action == ApprovalStatus.APPROVED) {
                auditAction = "APPROVE_UPDATE";
                employee.setStatus(ApprovalStatus.APPROVED);
            } else {
                auditAction = "REJECT_UPDATE";
                employee.setStatus(ApprovalStatus.REJECTED);
            }

        } else {
            throw new RuntimeException("Employee not in pending state");
        }

        employee.setApprovedBy(approver.getId());
        employee.setApprovedAt(LocalDateTime.now());

        Employee saved = employeeRepository.save(employee);

        auditService.logAudit("EMPLOYEE", saved.getId(), auditAction, oldState, saved, approver);

        return mapEntityToDpo(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDPO getDetail(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return mapEntityToDpo(employee);
    }

        private EmployeeDPO mapEntityToDpo(Employee employee) {
        EmployeeDPO dpo = new EmployeeDPO();
        dpo.setId(employee.getId());
        dpo.setName(employee.getName());
        dpo.setPosition(employee.getPosition());
        dpo.setEmail(employee.getEmail());
        dpo.setNpp(employee.getNpp());
        dpo.setStatusemp(employee.getStatusemp());
        dpo.setStatus(employee.getStatus());

        dpo.setSubmittedAt(employee.getSubmittedAt());
        dpo.setApprovedAt(employee.getApprovedAt());
        dpo.setApprovedBy(employee.getApprovedBy());

        if (employee.getDepartmen() != null) {
            dpo.setDepartmentId(employee.getDepartmen().getId());
            dpo.setDepartmentName(employee.getDepartmen().getName());
        }

        if (employee.getSubmitBy() != null) {
            dpo.setSubmitByName(employee.getSubmitBy().getUsername());
        }

        return dpo;
    }
}
