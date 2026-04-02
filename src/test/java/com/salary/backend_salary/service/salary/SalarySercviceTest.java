package com.salary.backend_salary.service.salary;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.salary.backend_salary.dto.salary.CreateSalaryRequest;
import com.salary.backend_salary.dto.salary.SalaryResponse;
import com.salary.backend_salary.entity.departmen.Department;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.EmployeeSalaryComponent;
import com.salary.backend_salary.entity.salary.Salary;
import com.salary.backend_salary.entity.salary.SalaryComponent;
import com.salary.backend_salary.entity.salary.SalarySlipDetail;
import com.salary.backend_salary.enums.ComponentType;
import com.salary.backend_salary.exception.ResourceNotFoundException;
import com.salary.backend_salary.exception.SalaryAlreadyExistsException;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.employee.EmployeeSalaryComponentRepository;
import com.salary.backend_salary.repository.salary.SalaryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalaryService Tests")
class SalaryServiceTest {

    @Mock private SalaryRepository salaryRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeSalaryComponentRepository employeeComponentRepository;
    @Mock private JPAQueryFactory queryFactory;

    @InjectMocks
    private SalaryService salaryService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Employee employee;
    private Salary salary;
    private EmployeeSalaryComponent empEarning;
    private EmployeeSalaryComponent empDeduction;

    @BeforeEach
    void setUp() {
        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");

        employee = new Employee();
        employee.setId(1L);
        employee.setName("Budi Santoso");
        employee.setNpp("NPP-001");
        employee.setDepartmen(department);

        SalaryComponent earningComponent = new SalaryComponent();
        earningComponent.setId(1L);
        earningComponent.setName("Base Salary");
        earningComponent.setType(ComponentType.EARNING);
        earningComponent.setAmount(new BigDecimal("5000000"));

        SalaryComponent deductionComponent = new SalaryComponent();
        deductionComponent.setId(2L);
        deductionComponent.setName("BPJS");
        deductionComponent.setType(ComponentType.DEDUCTION);
        deductionComponent.setAmount(new BigDecimal("500000"));

        empEarning = new EmployeeSalaryComponent();
        empEarning.setEmployee(employee);
        empEarning.setSalaryComponent(earningComponent);
        empEarning.setAmount(new BigDecimal("5000000"));

        empDeduction = new EmployeeSalaryComponent();
        empDeduction.setEmployee(employee);
        empDeduction.setSalaryComponent(deductionComponent);
        empDeduction.setAmount(new BigDecimal("500000"));

        salary = new Salary();
        salary.setId(1L);
        salary.setEmployee(employee);
        salary.setMonth("2024-01");
        salary.setAmount(new BigDecimal("4500000"));
    }

    // ── getSalaryById ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSalaryById")
    class GetSalaryById {

        @Test
        @DisplayName("should return salary response when found")
        void shouldReturnSalaryWhenFound() {
            // arrange
            mockSalaryQuery(salary);

            // act
            SalaryResponse response = salaryService.getSalaryById(1L);

            // assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getMonth()).isEqualTo("2024-01");
            assertThat(response.getEmployeeName()).isEqualTo("Budi Santoso");
            assertThat(response.getEmployeeNpp()).isEqualTo("NPP-001");
            assertThat(response.getDivision()).isEqualTo("Engineering");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when salary not found")
        void shouldThrowWhenSalaryNotFound() {
            // arrange
            mockSalaryQuery(null);

            // act & assert
            assertThatThrownBy(() -> salaryService.getSalaryById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── createSalary ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createSalary")
    class CreateSalary {

        private CreateSalaryRequest request;

        @BeforeEach
        void setUp() {
            request = new CreateSalaryRequest();
            request.setEmployeeId(1L);
            request.setMonth("2024-02");
        }

        @Test
        @DisplayName("should create salary and calculate total correctly — earning minus deduction")
        void shouldCreateSalaryWithCorrectTotal() {
            // arrange
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-02"))
                    .thenReturn(Optional.empty());
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning, empDeduction));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> {
                Salary s = inv.getArgument(0);
                s.setId(2L);
                return s;
            });

            // act
            SalaryResponse response = salaryService.createSalary(request);

            // assert — 5.000.000 - 500.000 = 4.500.000
            assertThat(response.getMonth()).isEqualTo("2024-02");
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("4500000"));
            assertThat(response.getAmountFormatted()).contains("4.500.000");
            verify(salaryRepository).save(any(Salary.class));
        }

        @Test
        @DisplayName("should create salary with only earning components")
        void shouldCreateSalaryWithOnlyEarning() {
            // arrange
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-02"))
                    .thenReturn(Optional.empty());
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            // act
            SalaryResponse response = salaryService.createSalary(request);

            // assert
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("5000000"));
        }

        @Test
        @DisplayName("should treat null amount on component as zero")
        void shouldHandleNullAmountAsZero() {
            // arrange
            empEarning.setAmount(null);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-02"))
                    .thenReturn(Optional.empty());
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            // act
            SalaryResponse response = salaryService.createSalary(request);

            // assert
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should build correct number of slip details")
        void shouldBuildSlipDetailsForEachComponent() {
            // arrange
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-02"))
                    .thenReturn(Optional.empty());
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning, empDeduction));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            // act
            salaryService.createSalary(request);

            // assert
            verify(salaryRepository).save(argThat(savedSalary -> {
                List<SalarySlipDetail> details = savedSalary.getSlipDetails();
                return details.size() == 2
                    && details.stream().anyMatch(d ->
                            d.getAmount().compareTo(new BigDecimal("5000000")) == 0)
                    && details.stream().anyMatch(d ->
                            d.getAmount().compareTo(new BigDecimal("500000")) == 0);
            }));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void shouldThrowWhenEmployeeNotFound() {
            // arrange
            when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

            // act & assert
            assertThatThrownBy(() -> salaryService.createSalary(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("should throw SalaryAlreadyExistsException when salary for month already exists")
        void shouldThrowWhenSalaryAlreadyExists() {
            // arrange
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-02"))
                    .thenReturn(Optional.of(salary));

            // act & assert
            assertThatThrownBy(() -> salaryService.createSalary(request))
                    .isInstanceOf(SalaryAlreadyExistsException.class)
                    .hasMessageContaining("2024-02");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when employee has no salary components")
        void shouldThrowWhenNoSalaryComponents() {
            // arrange
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-02"))
                    .thenReturn(Optional.empty());
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(Collections.emptyList());

            // act & assert
            assertThatThrownBy(() -> salaryService.createSalary(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("salary components");
        }
    }

    // ── updateSalary ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateSalary")
    class UpdateSalary {

        private CreateSalaryRequest request;

        @BeforeEach
        void setUp() {
            request = new CreateSalaryRequest();
            request.setEmployeeId(1L);
            request.setMonth("2024-01"); // same month as salary fixture
        }

        @Test
        @DisplayName("should update salary with same month — duplicate check not triggered")
        void shouldUpdateSalaryWithSameMonth() {
            // arrange
            when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning, empDeduction));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            // act
            SalaryResponse response = salaryService.updateSalary(1L, request);

            // assert
            assertThat(response.getMonth()).isEqualTo("2024-01");
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("4500000"));
            verify(salaryRepository, never()).findByEmployee_IdAndMonth(anyLong(), anyString());
        }

        @Test
        @DisplayName("should update salary with different month — duplicate check triggered")
        void shouldUpdateSalaryWithDifferentMonth() {
            // arrange
            request.setMonth("2024-03");

            when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-03"))
                    .thenReturn(Optional.empty());
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            // act
            SalaryResponse response = salaryService.updateSalary(1L, request);

            // assert
            assertThat(response.getMonth()).isEqualTo("2024-03");
            verify(salaryRepository).findByEmployee_IdAndMonth(1L, "2024-03");
        }

        @Test
        @DisplayName("should throw SalaryAlreadyExistsException when new month taken by another salary")
        void shouldThrowWhenNewMonthTakenByOtherSalary() {
            // arrange
            request.setMonth("2024-03");

            Salary otherSalary = new Salary();
            otherSalary.setId(99L);

            when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-03"))
                    .thenReturn(Optional.of(otherSalary));

            // act & assert
            assertThatThrownBy(() -> salaryService.updateSalary(1L, request))
                    .isInstanceOf(SalaryAlreadyExistsException.class)
                    .hasMessageContaining("2024-03");
        }

        @Test
        @DisplayName("should not throw when found salary is the same record being updated")
        void shouldNotThrowWhenFoundSalaryIsSameRecord() {
            // arrange
            request.setMonth("2024-03");

            Salary sameRecord = new Salary();
            sameRecord.setId(1L); // same id — update diri sendiri

            when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));
            when(salaryRepository.findByEmployee_IdAndMonth(1L, "2024-03"))
                    .thenReturn(Optional.of(sameRecord));
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            // act & assert — no exception
            assertThatNoException().isThrownBy(() -> salaryService.updateSalary(1L, request));
        }

        @Test
        @DisplayName("should clear old slip details before rebuilding")
        void shouldClearOldSlipDetailsBeforeRebuilding() {
            // arrange
            SalarySlipDetail oldDetail = new SalarySlipDetail();
            oldDetail.setAmount(new BigDecimal("3000000"));
            salary.getSlipDetails().add(oldDetail);

            when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));
            when(employeeComponentRepository.findByEmployee_Id(1L))
                    .thenReturn(List.of(empEarning, empDeduction));
            when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

            // act
            salaryService.updateSalary(1L, request);

            // assert — old cleared, 2 new details rebuilt
            verify(salaryRepository).save(argThat(s -> s.getSlipDetails().size() == 2));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when salary not found")
        void shouldThrowWhenSalaryNotFound() {
            when(salaryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> salaryService.updateSalary(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── deleteSalary ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteSalary")
    class DeleteSalary {

        @Test
        @DisplayName("should delete salary when found")
        void shouldDeleteSalaryWhenFound() {
            // arrange
            when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));

            // act
            salaryService.deleteSalary(1L);

            // assert
            verify(salaryRepository).delete(salary);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when salary not found")
        void shouldThrowWhenSalaryNotFound() {
            when(salaryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> salaryService.deleteSalary(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(salaryRepository, never()).delete(any());
        }
    }

    // ── mapToResponse / formatCurrency ────────────────────────────────────────

    @Nested
    @DisplayName("mapToResponse")
    class MapToResponse {

        @Test
        @DisplayName("should return 'Rp0,00' when amount is null")
        void shouldReturnZeroFormattedWhenAmountNull() {
            // arrange
            salary.setAmount(null);
            mockSalaryQuery(salary);

            // act
            SalaryResponse response = salaryService.getSalaryById(1L);

            // assert
            assertThat(response.getAmountFormatted()).isEqualTo("Rp0,00");
        }

        @Test
        @DisplayName("should return null division when employee has no department")
        void shouldReturnNullDivisionWhenNoDepartment() {
            // arrange
            employee.setDepartmen(null);
            mockSalaryQuery(salary);

            // act
            SalaryResponse response = salaryService.getSalaryById(1L);

            // assert
            assertThat(response.getDivision()).isNull();
        }

        @Test
        @DisplayName("should format amount in Indonesian locale")
        void shouldFormatCurrencyInIndonesianLocale() {
            // arrange
            salary.setAmount(new BigDecimal("10000000"));
            mockSalaryQuery(salary);

            // act
            SalaryResponse response = salaryService.getSalaryById(1L);

            // assert — Indonesian format: Rp10.000.000,00
            assertThat(response.getAmountFormatted()).contains("10.000.000");
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
    private void mockSalaryQuery(Salary returnValue) {
    JPAQuery<Salary> mockQuery = mock(JPAQuery.class);

    lenient().when(mockQuery.join(any(EntityPath.class))).thenReturn(mockQuery);
    lenient().when(mockQuery.join(any(EntityPath.class), any(Path.class))).thenReturn(mockQuery);
    lenient().when(mockQuery.fetchJoin()).thenReturn(mockQuery);
    lenient().when(mockQuery.leftJoin(any(EntityPath.class))).thenReturn(mockQuery);
    lenient().when(mockQuery.leftJoin(any(EntityPath.class), any(Path.class))).thenReturn(mockQuery);
    lenient().when(mockQuery.where(any(Predicate.class))).thenReturn(mockQuery);
    lenient().when(mockQuery.fetchOne()).thenReturn(returnValue);

    doReturn(mockQuery).when(queryFactory).selectFrom(any());
}
}