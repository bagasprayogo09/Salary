package com.salary.backend_salary.service.salary;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.salary.backend_salary.dto.salary.CreateSalaryRequest;
import com.salary.backend_salary.dto.salary.SalaryFilterRequest;
import com.salary.backend_salary.dto.salary.SalaryResponse;
import com.salary.backend_salary.entity.departmen.Department;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.EmployeeSalaryComponent;
import com.salary.backend_salary.entity.salary.Salary;
import com.salary.backend_salary.entity.salary.SalaryComponent;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.employee.EmployeeSalaryComponentRepository;
import com.salary.backend_salary.repository.salary.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeSalaryComponentRepository employeeComponentRepository;

   @Mock
    private JPAQueryFactory queryFactory;

    @Mock(answer = Answers.RETURNS_SELF)
    private JPAQuery<Salary> mockSalaryQuery;

    @Mock(answer = Answers.RETURNS_SELF)
    private JPAQuery<Long> mockCountQuery;
    @InjectMocks
    private SalaryService salaryService;

    private Employee mockEmployee;
    private Salary mockSalary;
    private SalaryComponent mockComponent;
    private EmployeeSalaryComponent mockEmployeeComponent;
    private CreateSalaryRequest createRequest;

    @BeforeEach
    void setUp() {
        Department mockDept = new Department();
        mockDept.setId(10L);
        mockDept.setName("IT");

        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setName("Bagas");
        mockEmployee.setNpp("NPP123");
        mockEmployee.setDepartmen(mockDept);

        mockSalary = new Salary();
        mockSalary.setId(100L);
        mockSalary.setEmployee(mockEmployee);
        mockSalary.setMonth("2026-02");
        mockSalary.setAmount(new BigDecimal("5000000"));
        mockSalary.setSlipDetails(new ArrayList<>()); 

        mockComponent = new SalaryComponent();
        mockComponent.setId(5L);
        mockComponent.setName("Gaji Pokok");
        mockComponent.setAmount(new BigDecimal("5000000"));
        mockComponent.setType("Allowance");

        mockEmployeeComponent = new EmployeeSalaryComponent();
        mockEmployeeComponent.setId(1L);
        mockEmployeeComponent.setEmployee(mockEmployee);
        mockEmployeeComponent.setSalaryComponent(mockComponent);
        mockEmployeeComponent.setAmount(new BigDecimal("5000000"));

        createRequest = new CreateSalaryRequest();
        createRequest.setEmployeeId(1L);
        createRequest.setMonth("2026-02");
    }

    @Test
    void testCreateSalary_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(salaryRepository.findByEmployee_IdAndMonth(1L, "2026-02")).thenReturn(Optional.empty());
        when(employeeComponentRepository.findByEmployee_Id(1L)).thenReturn(List.of(mockEmployeeComponent));
        when(salaryRepository.save(any(Salary.class))).thenReturn(mockSalary);

        SalaryResponse result = salaryService.createSalary(createRequest);

        assertNotNull(result);
        assertEquals("2026-02", result.getMonth());
        assertEquals("Bagas", result.getEmployeeName());
        verify(salaryRepository).save(any(Salary.class));
    }

    @Test
    void testCreateSalary_EmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            salaryService.createSalary(createRequest);
        });

        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(salaryRepository, never()).save(any());
    }

    @Test
    void testCreateSalary_AlreadyExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(salaryRepository.findByEmployee_IdAndMonth(1L, "2026-02")).thenReturn(Optional.of(mockSalary));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            salaryService.createSalary(createRequest);
        });

        assertTrue(exception.getMessage().contains("Salary already exists"));
    }

    @Test
    void testUpdateSalary_Success_SameMonth() {
        when(salaryRepository.findById(100L)).thenReturn(Optional.of(mockSalary));
        when(employeeComponentRepository.findByEmployee_Id(1L)).thenReturn(List.of(mockEmployeeComponent));
        when(salaryRepository.save(any(Salary.class))).thenReturn(mockSalary);

        SalaryResponse result = salaryService.updateSalary(100L, createRequest);

        assertNotNull(result);
        verify(salaryRepository, never()).findByEmployee_IdAndMonth(anyLong(), anyString());
        verify(salaryRepository).save(mockSalary);
    }

    @Test
    void testUpdateSalary_Success_DifferentMonth() {
        createRequest.setMonth("2026-03");
        
        when(salaryRepository.findById(100L)).thenReturn(Optional.of(mockSalary));
        when(salaryRepository.findByEmployee_IdAndMonth(1L, "2026-03")).thenReturn(Optional.empty());
        when(employeeComponentRepository.findByEmployee_Id(1L)).thenReturn(List.of(mockEmployeeComponent));
        when(salaryRepository.save(any(Salary.class))).thenReturn(mockSalary);

        SalaryResponse result = salaryService.updateSalary(100L, createRequest);

        assertNotNull(result);
        verify(salaryRepository).findByEmployee_IdAndMonth(1L, "2026-03");
        verify(salaryRepository).save(mockSalary);
    }

    @Test
    void testDeleteSalary_Success() {
        when(salaryRepository.findById(100L)).thenReturn(Optional.of(mockSalary));

        assertDoesNotThrow(() -> {
            salaryService.deleteSalary(100L);
        });

        verify(salaryRepository).delete(mockSalary);
    }
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"}) 
    void testGetSalaryById_Success() {
        when(queryFactory.selectFrom(any())).thenReturn((JPAQuery) mockSalaryQuery);
        when(mockSalaryQuery.fetchOne()).thenReturn(mockSalary);

        SalaryResponse result = salaryService.getSalaryById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

   @Test
    @SuppressWarnings({"unchecked", "rawtypes"}) 
    void testGetSalaryById_NotFound() {
        when(queryFactory.selectFrom(any())).thenReturn((JPAQuery) mockSalaryQuery);
        when(mockSalaryQuery.fetchOne()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            salaryService.getSalaryById(999L);
        });
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testGetAllSalaries_Success() {
        SalaryFilterRequest filter = new SalaryFilterRequest();
        filter.setMonth("2026-02");
        filter.setEmployeeName("Bagas");

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "month"));

        when(queryFactory.selectFrom(any())).thenReturn((JPAQuery) mockSalaryQuery);
        when(mockSalaryQuery.fetch()).thenReturn(List.of(mockSalary));

        lenient().when(queryFactory.select(any(Expression.class))).thenReturn((JPAQuery) mockCountQuery);
        lenient().when(mockCountQuery.fetchOne()).thenReturn(1L);

        Page<SalaryResponse> result = salaryService.getAllSalaries(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(100L, result.getContent().get(0).getId());
        assertEquals("2026-02", result.getContent().get(0).getMonth());
    }
}