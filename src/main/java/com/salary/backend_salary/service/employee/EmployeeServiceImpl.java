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

        @Override
        @Transactional
        public EmployeeDPO createEmployee(EmployeeRequestVM vm) {
            AppUser actor = userRepository.findById(vm.getSubmitById())
                    .orElseThrow(() -> new RuntimeException("Submitter User not found"));

            Employee employee = new Employee();
            mapVmToEntity(vm, employee, actor);

            employee.setSubmittedAt(LocalDateTime.now());
            employee.setStatus(ApprovalStatus.PENDING_CREATE);

            Employee saved = employeeRepository.save(employee);
            auditService.logAudit("EMPLOYEE", saved.getId(), "REQUEST_CREATE", null, saved, actor);

            return mapEntityToDpo(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public EmployeeDPO getEmployeeById(Long id) {
            Employee employee = employeeRepository.findWithDetailsById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            return mapEntityToDpo(employee);
        }

        @Override
        @Transactional
        public EmployeeDPO updateEmployee(Long id, EmployeeRequestVM vm) {
            Employee employee = employeeRepository.findWithDetailsById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            AppUser actor = userRepository.findById(vm.getSubmitById())
                    .orElseThrow(() -> new RuntimeException("Submitter User not found"));

            Employee oldState = copyForAudit(employee);

            mapVmToEntity(vm, employee, actor);
            employee.setSubmittedAt(LocalDateTime.now());
            employee.setApprovedBy(null);
            employee.setApprovedAt(null);

            if (employee.getStatus() != ApprovalStatus.PENDING_CREATE) {
                employee.setStatus(ApprovalStatus.PENDING_UPDATE);
            }

            Employee updated = employeeRepository.save(employee);
            auditService.logAudit("EMPLOYEE", updated.getId(), "REQUEST_UPDATE", oldState, updated, actor);

            return mapEntityToDpo(updated);
        }

        @Override
        @Transactional
        public void deleteEmployee(Long id) {
            Employee employee = employeeRepository.findWithDetailsById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            AppUser actor = employee.getSubmitBy();
            Employee oldState = copyForAudit(employee);

            employee.setStatus(ApprovalStatus.PENDING_DELETE);
            employee.setSubmittedAt(LocalDateTime.now());

            Employee saved = employeeRepository.save(employee);
            auditService.logAudit("EMPLOYEE", id, "REQUEST_DELETE", oldState, saved, actor);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<EmployeeDPO> searchEmployees(EmployeeVM vm, Pageable pageable) {

            QEmployee qEmployee = QEmployee.employee;
            BooleanBuilder builder = new BooleanBuilder();

            if (vm != null) {
                if (vm.getName() != null && !vm.getName().isBlank())
                    builder.and(qEmployee.name.startsWithIgnoreCase(vm.getName()));

                if (vm.getPosition() != null && !vm.getPosition().isBlank())
                    builder.and(qEmployee.position.startsWithIgnoreCase(vm.getPosition()));

                if (vm.getEmail() != null && !vm.getEmail().isBlank())
                    builder.and(qEmployee.email.startsWithIgnoreCase(vm.getEmail()));

                if (vm.getDivisionName() != null && !vm.getDivisionName().isBlank())
                    builder.and(qEmployee.departmen.name.startsWithIgnoreCase(vm.getDivisionName()));

                if (vm.getStatus() != null)
                    builder.and(qEmployee.status.eq(vm.getStatus()));
            }

            return employeeRepository
                    .searchWithDetails(builder, pageable)
                    .map(this::mapEntityToDpo);
        }

        private void mapVmToEntity(EmployeeRequestVM vm, Employee employee, AppUser actor) {
            employee.setName(vm.getName());
            employee.setPosition(vm.getPosition());
            employee.setEmail(vm.getEmail());
            employee.setNpp(vm.getNpp());
            employee.setStatusemp(vm.getStatusemp());
            employee.setSubmitBy(actor);

            if (vm.getDepartmentId() != null) {
                Department dept = departmenRepository.findById(vm.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Dept not found"));
                employee.setDepartmen(dept);
            }
        }

        private Employee copyForAudit(Employee original) {
            Employee copy = new Employee();
            copy.setId(original.getId());
            copy.setName(original.getName());
            copy.setPosition(original.getPosition());
            copy.setEmail(original.getEmail());
            copy.setNpp(original.getNpp());
            copy.setStatusemp(original.getStatusemp());
            copy.setStatus(original.getStatus());
            copy.setDepartmen(original.getDepartmen());
            return copy;
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
