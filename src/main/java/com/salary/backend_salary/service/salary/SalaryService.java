package com.salary.backend_salary.service.salary;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.salary.backend_salary.dto.salary.CreateSalaryRequest;
import com.salary.backend_salary.dto.salary.SalaryFilterRequest;
import com.salary.backend_salary.dto.salary.SalaryResponse;
import com.salary.backend_salary.entity.departmen.QDepartment;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.EmployeeSalaryComponent;
import com.salary.backend_salary.entity.employee.QEmployee;
import com.salary.backend_salary.entity.salary.QSalary;
import com.salary.backend_salary.entity.salary.Salary;
import com.salary.backend_salary.entity.salary.SalaryComponent;
import com.salary.backend_salary.entity.salary.SalarySlipDetail;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.employee.EmployeeSalaryComponentRepository;
import com.salary.backend_salary.repository.salary.SalaryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSalaryComponentRepository employeeComponentRepository;
    private final JPAQueryFactory queryFactory;

    public Page<SalaryResponse> getAllSalaries(SalaryFilterRequest filter, Pageable pageable) {

        QSalary salary = QSalary.salary;
        QEmployee employee = QEmployee.employee;

        QDepartment departmen = QDepartment.department;

        BooleanBuilder builder = buildFilter(filter, salary, employee);

        List<Salary> content = queryFactory
                .selectFrom(salary)
                .join(salary.employee, employee).fetchJoin()
                .leftJoin(employee.departmen, departmen).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QuerydslSortUtil.getOrderSpecifiers(pageable.getSort(), salary, employee))
                .fetch();

        return PageableExecutionUtils.getPage(
                content.stream().map(this::mapToResponse).toList(),
                pageable,
                () -> queryFactory
                        .select(salary.count())
                        .from(salary)
                        .join(salary.employee, employee)
                        .where(builder)
                        .fetchOne()
        );
    }

    public SalaryResponse getSalaryById(Long id) {

        QSalary salary = QSalary.salary;
        QEmployee employee = QEmployee.employee;

        Salary result = queryFactory
                .selectFrom(salary)
                .join(salary.employee, employee).fetchJoin()
                .where(salary.id.eq(id))
                .fetchOne();

        if (result == null) {
            throw new EntityNotFoundException("Salary not found with id: " + id);
        }

        return mapToResponse(result);
    }

    @Transactional
    public SalaryResponse createSalary(CreateSalaryRequest request) {

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        salaryRepository.findByEmployee_IdAndMonth(request.getEmployeeId(), request.getMonth())
                .ifPresent(s -> { 
                    throw new IllegalArgumentException("Salary already exists for this month"); 
                });

        List<EmployeeSalaryComponent> employeeComponents = 
                employeeComponentRepository.findByEmployee_Id(employee.getId());

        if (employeeComponents.isEmpty()) {
            throw new IllegalArgumentException("Employee does not have any salary components setup");
        }

        Salary salary = new Salary();
        salary.setEmployee(employee);
        salary.setMonth(request.getMonth());

        buildSalaryDetails(salary, employeeComponents);

        Salary saved = salaryRepository.save(salary);

        return mapToResponse(saved);
    }

    @Transactional
    public SalaryResponse updateSalary(Long id, CreateSalaryRequest request) {

        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Salary not found with id: " + id));

        if (!salary.getMonth().equals(request.getMonth())) {

            salaryRepository.findByEmployee_IdAndMonth(
                    salary.getEmployee().getId(),
                    request.getMonth()
            ).ifPresent(s -> {
                if (!s.getId().equals(id)) {
                    throw new IllegalArgumentException("Salary already exists for this month");
                }
            });

            salary.setMonth(request.getMonth());
        }

        salary.getSlipDetails().clear();

        List<EmployeeSalaryComponent> employeeComponents = 
                employeeComponentRepository.findByEmployee_Id(salary.getEmployee().getId());

        buildSalaryDetails(salary, employeeComponents);

        Salary updated = salaryRepository.save(salary);

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSalary(Long id) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Salary not found with id: " + id));

        salaryRepository.delete(salary);
    }


    private BooleanBuilder buildFilter(SalaryFilterRequest filter,
                                       QSalary salary,
                                       QEmployee employee) {

        BooleanBuilder builder = new BooleanBuilder();

        if (filter.getMonth() != null && !filter.getMonth().isBlank()) {
            builder.and(salary.month.containsIgnoreCase(filter.getMonth().trim()));
        }

        if (filter.getEmployeeName() != null && !filter.getEmployeeName().isBlank()) {
            builder.and(employee.name.containsIgnoreCase(filter.getEmployeeName().trim()));
        }

        return builder;
    }

   private void buildSalaryDetails(Salary salary, List<EmployeeSalaryComponent> employeeComponents) {
    BigDecimal total = BigDecimal.ZERO;

    for (EmployeeSalaryComponent empComp : employeeComponents) {
        SalaryComponent masterComponent = empComp.getSalaryComponent();
        
        BigDecimal amount = empComp.getAmount() != null ? empComp.getAmount() : BigDecimal.ZERO;

        SalarySlipDetail detail = new SalarySlipDetail();
        detail.setSalary(salary);
        detail.setSalaryComponent(masterComponent);
        detail.setAmount(amount); 

        salary.getSlipDetails().add(detail);

        if (masterComponent.getType() != null && "Deduction".equalsIgnoreCase(masterComponent.getType().trim())) {
            total = total.subtract(amount); 
        } else {
            total = total.add(amount); 
        }
    }

    salary.setAmount(total);
}

    private SalaryResponse mapToResponse(Salary salary) {

        Employee employee = salary.getEmployee();

        return SalaryResponse.builder()
                .id(salary.getId())
                .month(salary.getMonth())
                .amount(salary.getAmount())
                .amountFormatted(formatCurrency(salary.getAmount()))
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .employeeNpp(employee.getNpp())
                .division(employee.getDepartmen() != null ?
                        employee.getDepartmen().getName() : null)
                .build();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "Rp0,00";
        return NumberFormat
                .getCurrencyInstance(Locale.of("id", "ID"))
                .format(amount);
    }   

    public static class QuerydslSortUtil {
    private QuerydslSortUtil() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static OrderSpecifier<Comparable>[] getOrderSpecifiers(
            Sort sort, QSalary salary, QEmployee employee) {
        
        return sort.stream()
                .map(order -> {
                    OrderSpecifier<?> orderSpecifier = switch (order.getProperty()) {
                        case "month" -> order.isAscending() ? salary.month.asc() : salary.month.desc();
                        case "amount" -> order.isAscending() ? salary.amount.asc() : salary.amount.desc();
                        case "employeeName" -> order.isAscending() ? employee.name.asc() : employee.name.desc();
                        default -> order.isAscending() ? salary.id.asc() : salary.id.desc();
                    };
                    return (OrderSpecifier<Comparable>) orderSpecifier;
                })
                .toArray(OrderSpecifier[]::new);
    }
}
}