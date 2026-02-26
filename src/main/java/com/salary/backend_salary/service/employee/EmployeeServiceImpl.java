package com.salary.backend_salary.service.employee;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.departmen.Department;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.QEmployee;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.mapper.EmployeeMapper;
import com.salary.backend_salary.repository.departmen.DepartmenRepository;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.user.UserRepository;
import com.salary.backend_salary.service.audit.AuditService;
import com.salary.backend_salary.vm.employee.EmployeeRequestVM;
import com.salary.backend_salary.vm.employee.EmployeeVM;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmenRepository departmenRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final EmployeeMapper employeeMapper; 

    @Override
    @Transactional
    public EmployeeDPO createEmployee(EmployeeRequestVM vm) {
        var actor = fetchUser(vm.getSubmitById());
        var department = fetchDepartment(vm.getDepartmentId());

        Employee employee = new Employee();
        employeeMapper.updateEntityFromVm(vm, employee);
        
        employee.setDepartmen(department);
        employee.setSubmitBy(actor);
        employee.setSubmittedAt(LocalDateTime.now());
        employee.setStatus(ApprovalStatus.PENDING_CREATE);

        var saved = employeeRepository.save(employee);
        
        auditService.logAudit("EMPLOYEE", saved.getId(), "REQUEST_CREATE", null, saved, actor);

        return employeeMapper.toDpo(saved);
    }

    @Override
    @Transactional
    public EmployeeDPO updateEmployee(Long id, EmployeeRequestVM vm) {
        var employee = employeeRepository.findWithDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        var actor = fetchUser(vm.getSubmitById());

        Employee oldState = employeeMapper.copy(employee);

        employeeMapper.updateEntityFromVm(vm, employee);

        if (vm.getDepartmentId() != null) {
            var dept = fetchDepartment(vm.getDepartmentId());
            employee.setDepartmen(dept);
        }

        employee.setSubmittedAt(LocalDateTime.now());
        employee.setApprovedBy(null);
        employee.setApprovedAt(null); 
        
        if (employee.getStatus() != ApprovalStatus.PENDING_CREATE) {
            employee.setStatus(ApprovalStatus.PENDING_UPDATE);
        }

        var updated = employeeRepository.save(employee);
        
        auditService.logAudit("EMPLOYEE", updated.getId(), "REQUEST_UPDATE", oldState, updated, actor);

        return employeeMapper.toDpo(updated);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id, Long submitterId) {
        var employee = employeeRepository.findWithDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        var actor = fetchUser(submitterId);
        
        Employee oldState = employeeMapper.copy(employee);

        employee.setStatus(ApprovalStatus.PENDING_DELETE);
        employee.setSubmittedAt(LocalDateTime.now());
       

        var saved = employeeRepository.save(employee);
        
        auditService.logAudit("EMPLOYEE", id, "REQUEST_DELETE", oldState, saved, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDPO getEmployeeById(Long id) {
        return employeeRepository.findWithDetailsById(id)
                .map(employeeMapper::toDpo)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDPO> searchEmployees(EmployeeVM vm, Pageable pageable) {
        var qEmployee = QEmployee.employee;
        var builder = new BooleanBuilder();

        if (vm != null) {
            if (isValid(vm.getName()))
                builder.and(qEmployee.name.containsIgnoreCase(vm.getName())); // contains lebih fleksibel
            if (isValid(vm.getPosition()))
                builder.and(qEmployee.position.containsIgnoreCase(vm.getPosition()));
            if (isValid(vm.getEmail()))
                builder.and(qEmployee.email.containsIgnoreCase(vm.getEmail()));
            if (isValid(vm.getDivisionName()))
                builder.and(qEmployee.departmen.name.containsIgnoreCase(vm.getDivisionName()));
            if (vm.getStatus() != null)
                builder.and(qEmployee.status.eq(vm.getStatus()));
        }

        return employeeRepository.searchWithDetails(builder, pageable)
                .map(employeeMapper::toDpo);
    }

    // Helper Methods untuk meringkas kode
    private boolean isValid(String text) {
        return text != null && !text.isBlank();
    }

    private AppUser fetchUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User ID " + id + " not found"));
    }

    private Department fetchDepartment(Long id) {
        if (id == null) return null;
        return departmenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department ID " + id + " not found"));
    }
}