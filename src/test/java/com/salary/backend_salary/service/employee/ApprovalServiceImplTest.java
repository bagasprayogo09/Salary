package com.salary.backend_salary.service.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.mapper.EmployeeMapper;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.user.UserRepository;
import com.salary.backend_salary.service.audit.AuditService;
import com.salary.backend_salary.vm.employee.ApprovalRequestVM;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private Employee employee;
    private AppUser approver;
    private ApprovalRequestVM vm;
    private EmployeeDPO dpo;
    private com.salary.backend_salary.entity.departmen.Department department;

    @BeforeEach
    void setUp() {
        department = new com.salary.backend_salary.entity.departmen.Department();
        department.setId(1L);
        department.setName("IT");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("Bagas");
        employee.setDepartmen(department);

        approver = new AppUser();
        approver.setId(99L); 

        vm = new ApprovalRequestVM();
        vm.setApproverId(99L);
        vm.setStatus(ApprovalStatus.APPROVED);

        dpo = new EmployeeDPO();
        dpo.setId(1L);
        dpo.setName("Bagas");
    }

    @Test
    void processApproval_InvalidAction_ShouldThrowIllegalArgumentException() {
        vm.setStatus(ApprovalStatus.PENDING_CREATE); 

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            approvalService.processApproval(1L, vm);
        });

        assertTrue(exception.getMessage().contains("Invalid approval action"));
        verifyNoInteractions(employeeRepository, userRepository, auditService);
    }

    @Test
    void processApproval_EmployeeNotFound_ShouldThrowEntityNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            approvalService.processApproval(1L, vm);
        });
    }

    @Test
    void processApproval_ApproverNotFound_ShouldThrowEntityNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            approvalService.processApproval(1L, vm);
        });
    }

    @Test
    void processApproval_PendingCreate_Approved_ShouldReturnDpo() {
        employee.setStatus(ApprovalStatus.PENDING_CREATE);
        vm.setStatus(ApprovalStatus.APPROVED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(99L)).thenReturn(Optional.of(approver));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDpo(any(Employee.class))).thenReturn(dpo);

        EmployeeDPO result = approvalService.processApproval(1L, vm);

        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED, employee.getStatus());
        assertNotNull(employee.getApprovedAt());
        assertEquals(99L, employee.getApprovedBy());

        verify(auditService).logAudit(
                eq("EMPLOYEE"), eq(1L), eq("APPROVE_CREATE"), any(Employee.class), eq(employee), eq(approver)
        );
    }

    @Test
    void processApproval_PendingUpdate_Rejected_ShouldReturnDpo() {
        employee.setStatus(ApprovalStatus.PENDING_UPDATE);
        vm.setStatus(ApprovalStatus.REJECTED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(99L)).thenReturn(Optional.of(approver));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDpo(any(Employee.class))).thenReturn(dpo);

        EmployeeDPO result = approvalService.processApproval(1L, vm);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, employee.getStatus());

        verify(auditService).logAudit(
                eq("EMPLOYEE"), eq(1L), eq("REJECT_UPDATE"), any(Employee.class), eq(employee), eq(approver)
        );
    }

    @Test
    void processApproval_PendingDelete_Approved_ShouldSoftDeleteAndReturnDpo() {
        employee.setStatus(ApprovalStatus.PENDING_DELETE);
        vm.setStatus(ApprovalStatus.APPROVED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(99L)).thenReturn(Optional.of(approver));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDpo(any(Employee.class))).thenReturn(dpo);

        EmployeeDPO result = approvalService.processApproval(1L, vm);

        assertNotNull(result);
        assertEquals("INACTIVE", employee.getStatusemp()); 
        assertEquals(ApprovalStatus.APPROVED, employee.getStatus());
        
        verify(employeeRepository).save(employee);
        verify(employeeRepository, never()).delete(any()); 
        
        verify(auditService).logAudit(
                eq("EMPLOYEE"), eq(1L), eq("APPROVE_DELETE"), any(Employee.class), eq(employee), eq(approver)
        );
    }
    
    @Test
    void processApproval_PendingDelete_Rejected_ShouldRevertToApprovedAndReturnDpo() {
        employee.setStatus(ApprovalStatus.PENDING_DELETE);
        vm.setStatus(ApprovalStatus.REJECTED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(99L)).thenReturn(Optional.of(approver));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDpo(any(Employee.class))).thenReturn(dpo);

        EmployeeDPO result = approvalService.processApproval(1L, vm);

        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED, employee.getStatus()); 
        verify(employeeRepository).save(employee);
        verify(auditService).logAudit(
                eq("EMPLOYEE"), eq(1L), eq("REJECT_DELETE"), any(Employee.class), eq(employee), eq(approver)
        );
    }

    @Test
    void processApproval_InvalidState_ShouldThrowIllegalStateException() {
        employee.setStatus(ApprovalStatus.APPROVED);
        vm.setStatus(ApprovalStatus.APPROVED);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(99L)).thenReturn(Optional.of(approver));

        assertThrows(IllegalStateException.class, () -> {
            approvalService.processApproval(1L, vm);
        });
    }

    @Test
    void getDetail_Success_ShouldReturnDpo() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toDpo(employee)).thenReturn(dpo);

        EmployeeDPO result = approvalService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository).findById(1L);
    }

    @Test
    void getDetail_NotFound_ShouldThrowEntityNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            approvalService.getDetail(1L);
        });
    }
}