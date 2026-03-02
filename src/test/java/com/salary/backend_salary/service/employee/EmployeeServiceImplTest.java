package com.salary.backend_salary.service.employee;

import com.querydsl.core.BooleanBuilder;
import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.departmen.Department;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.mapper.EmployeeMapper;
import com.salary.backend_salary.repository.departmen.DepartmenRepository;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.user.UserRepository;
import com.salary.backend_salary.service.audit.AuditService;
import com.salary.backend_salary.vm.employee.EmployeeRequestVM;
import com.salary.backend_salary.vm.employee.EmployeeVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmenRepository departmenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequestVM requestVM;
    private AppUser mockUser;
    private Department mockDept;
    private Employee mockEmployee;
    private EmployeeDPO mockDpo;

    @BeforeEach
    void setUp() {
        requestVM = new EmployeeRequestVM();
        requestVM.setSubmitById(1L);
        requestVM.setDepartmentId(10L);

        mockUser = new AppUser();
        mockUser.setId(1L);

        mockDept = new Department();
        mockDept.setId(10L);

        mockEmployee = new Employee();
        mockEmployee.setId(100L);
        mockEmployee.setStatus(ApprovalStatus.PENDING_CREATE);

        mockDpo = new EmployeeDPO();
        mockDpo.setId(100L);
    }

    @Test
    void testCreateEmployee_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(departmenRepository.findById(10L)).thenReturn(Optional.of(mockDept));
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);
        when(employeeMapper.toDpo(any(Employee.class))).thenReturn(mockDpo);

        EmployeeDPO result = employeeService.createEmployee(requestVM);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        
        verify(employeeMapper).updateEntityFromVm(eq(requestVM), any(Employee.class));
        verify(employeeRepository).save(any(Employee.class));
        verify(auditService).logAudit("EMPLOYEE", 100L, "REQUEST_CREATE", null, mockEmployee, mockUser);
    }

    @Test
    void testCreateEmployee_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.createEmployee(requestVM);
        });

        assertEquals("User ID 1 not found", exception.getMessage());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdateEmployee_Success() {
        when(employeeRepository.findWithDetailsById(100L)).thenReturn(Optional.of(mockEmployee));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(departmenRepository.findById(10L)).thenReturn(Optional.of(mockDept));
        
        Employee oldState = new Employee();
        when(employeeMapper.copy(mockEmployee)).thenReturn(oldState);
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);
        when(employeeMapper.toDpo(any(Employee.class))).thenReturn(mockDpo);

        EmployeeDPO result = employeeService.updateEmployee(100L, requestVM);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(employeeRepository).save(mockEmployee);
        verify(auditService).logAudit("EMPLOYEE", 100L, "REQUEST_UPDATE", oldState, mockEmployee, mockUser);
    }

    @Test
    void testUpdateEmployee_EmployeeNotFound() {
        when(employeeRepository.findWithDetailsById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeService.updateEmployee(100L, requestVM);
        });

        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void testDeleteEmployee_Success() {
        when(employeeRepository.findWithDetailsById(100L)).thenReturn(Optional.of(mockEmployee));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        Employee oldState = new Employee();
        when(employeeMapper.copy(mockEmployee)).thenReturn(oldState);
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        assertDoesNotThrow(() -> {
            employeeService.deleteEmployee(100L, 1L);
        });

        assertEquals(ApprovalStatus.PENDING_DELETE, mockEmployee.getStatus());
        verify(employeeRepository).save(mockEmployee);
        verify(auditService).logAudit("EMPLOYEE", 100L, "REQUEST_DELETE", oldState, mockEmployee, mockUser);
    }

    @Test
    void testGetEmployeeById_Success() {
        when(employeeRepository.findWithDetailsById(100L)).thenReturn(Optional.of(mockEmployee));
        when(employeeMapper.toDpo(mockEmployee)).thenReturn(mockDpo);

        EmployeeDPO result = employeeService.getEmployeeById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void testSearchEmployees_Success() {
        EmployeeVM vm = new EmployeeVM();
        vm.setName("Bagas");
        vm.setPosition("Developer");
        vm.setEmail("bagas@mail.com");
        vm.setDivisionName("IT");
        vm.setStatus(ApprovalStatus.PENDING_CREATE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));

        when(employeeRepository.searchWithDetails(any(BooleanBuilder.class), eq(pageable))).thenReturn(mockPage);
        when(employeeMapper.toDpo(mockEmployee)).thenReturn(mockDpo);

        Page<EmployeeDPO> result = employeeService.searchEmployees(vm, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(100L, result.getContent().get(0).getId());
        verify(employeeRepository).searchWithDetails(any(BooleanBuilder.class), eq(pageable));
    }

    @Test
    void testSearchEmployees_NullVM() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> mockPage = new PageImpl<>(List.of(mockEmployee));

        when(employeeRepository.searchWithDetails(any(BooleanBuilder.class), eq(pageable))).thenReturn(mockPage);
        when(employeeMapper.toDpo(mockEmployee)).thenReturn(mockDpo);

        Page<EmployeeDPO> result = employeeService.searchEmployees(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}