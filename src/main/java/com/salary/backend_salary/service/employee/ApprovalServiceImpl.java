package com.salary.backend_salary.service.employee;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.mapper.EmployeeMapper;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.user.UserRepository;
import com.salary.backend_salary.service.audit.AuditService;
import com.salary.backend_salary.vm.employee.ApprovalRequestVM;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private static final String ENTITY_NAME = "EMPLOYEE";

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final EmployeeMapper employeeMapper; 

    @Override
    @Transactional
    public EmployeeDPO processApproval(Long employeeId, ApprovalRequestVM vm) {
        
        if (vm.getStatus() != ApprovalStatus.APPROVED && vm.getStatus() != ApprovalStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid approval action: " + vm.getStatus());
        }

        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        var approver = userRepository.findById(vm.getApproverId())
                .orElseThrow(() -> new EntityNotFoundException("Approver user not found with ID: " + vm.getApproverId()));

        var oldState = createAuditSnapshot(employee);

        ApprovalStatus currentStatus = employee.getStatus();
        boolean isApproved = (vm.getStatus() == ApprovalStatus.APPROVED);

        if (currentStatus == ApprovalStatus.PENDING_DELETE) {
            return handlePendingDelete(employee, approver, isApproved, oldState);
        }

        String auditAction = switch (currentStatus) {
            case PENDING_CREATE -> {
                employee.setStatus(isApproved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
                yield isApproved ? "APPROVE_CREATE" : "REJECT_CREATE";
            }
            case PENDING_UPDATE -> {
                employee.setStatus(isApproved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
                yield isApproved ? "APPROVE_UPDATE" : "REJECT_UPDATE";
            }
            default -> throw new IllegalStateException("Employee is not in a pending state. Current status: " + currentStatus);
        };

        employee.setApprovedBy(approver.getId());
        employee.setApprovedAt(LocalDateTime.now());

        var saved = employeeRepository.save(employee);

        auditService.logAudit(ENTITY_NAME, saved.getId(), auditAction, oldState, saved, approver);
        
        return employeeMapper.toDpo(saved);
    }

    private EmployeeDPO handlePendingDelete(Employee employee, AppUser approver, boolean isApproved, Employee oldState) {
        String auditAction;
        
        if (isApproved) {
            auditAction = "APPROVE_DELETE";
            auditService.logAudit(ENTITY_NAME, employee.getId(), auditAction, oldState, null, approver);
            
            employeeRepository.delete(employee);
            return null; 
        } else {
            auditAction = "REJECT_DELETE";
            employee.setStatus(ApprovalStatus.APPROVED);
            employee.setApprovedBy(approver.getId());
            employee.setApprovedAt(LocalDateTime.now());
            
            var saved = employeeRepository.save(employee);
            
            auditService.logAudit(ENTITY_NAME, saved.getId(), auditAction, oldState, saved, approver);
            return employeeMapper.toDpo(saved);
        }
    }

    private Employee createAuditSnapshot(Employee src) {
        Employee copy = new Employee();
        copy.setId(src.getId());
        copy.setName(src.getName());
        copy.setStatus(src.getStatus());
        copy.setDepartmen(src.getDepartmen());
        return copy;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDPO getDetail(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(employeeMapper::toDpo)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
    }
}