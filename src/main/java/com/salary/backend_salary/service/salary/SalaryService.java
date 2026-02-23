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
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.QEmployee;
import com.salary.backend_salary.entity.salary.QSalary;
import com.salary.backend_salary.entity.salary.Salary;
import com.salary.backend_salary.entity.salary.SalaryComponent;
import com.salary.backend_salary.entity.salary.SalarySlipDetail;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.salary.SalaryComponentRepository;
import com.salary.backend_salary.repository.salary.SalaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final JPAQueryFactory queryFactory;

    // Get All
    public Page<SalaryResponse> getAllSalaries(SalaryFilterRequest filter, Pageable pageable) {

        QSalary salary = QSalary.salary;
        QEmployee employee = QEmployee.employee;

        BooleanBuilder builder = buildFilter(filter, salary, employee);

        List<Salary> content = queryFactory
                .selectFrom(salary)
                .join(salary.employee, employee).fetchJoin()
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

    // Get By Id
    public SalaryResponse getSalaryById(Long id) {

        QSalary salary = QSalary.salary;
        QEmployee employee = QEmployee.employee;

        Salary result = queryFactory
                .selectFrom(salary)
                .join(salary.employee, employee).fetchJoin()
                .where(salary.id.eq(id))
                .fetchOne();

        if (result == null) {
            throw new RuntimeException("Salary not found with id: " + id);
        }

        return mapToResponse(result);
    }

    //Create
    @Transactional
    public SalaryResponse createSalary(CreateSalaryRequest request) {

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + request.getEmployeeId()));

        salaryRepository.findByEmployee_IdAndMonth(request.getEmployeeId(), request.getMonth())
                .ifPresent(s ->
                        { throw new RuntimeException("Salary already exists for this month"); });

        List<SalaryComponent> components = salaryComponentRepository.findAll();

        Salary salary = new Salary();
        salary.setEmployee(employee);
        salary.setMonth(request.getMonth());

        buildSalaryDetails(salary, components);

        Salary saved = salaryRepository.save(salary);

        return mapToResponse(saved);
    }

    // Update
    @Transactional
    public SalaryResponse updateSalary(Long id, CreateSalaryRequest request) {

        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Salary not found with id: " + id));

        if (!salary.getMonth().equals(request.getMonth())) {

            salaryRepository.findByEmployee_IdAndMonth(
                    salary.getEmployee().getId(),
                    request.getMonth()
            ).ifPresent(s -> {
                if (!s.getId().equals(id)) {
                    throw new RuntimeException("Salary already exists for this month");
                }
            });

            salary.setMonth(request.getMonth());
        }

        salary.getSlipDetails().clear();

        List<SalaryComponent> components = salaryComponentRepository.findAll();
        buildSalaryDetails(salary, components);

        Salary updated = salaryRepository.save(salary);

        return mapToResponse(updated);
    }

    // Delete
    @Transactional
    public void deleteSalary(Long id) {

        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Salary not found with id: " + id));

        salaryRepository.delete(salary);
    }

    //Helper

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

    private void buildSalaryDetails(Salary salary, List<SalaryComponent> components) {

        BigDecimal total = BigDecimal.ZERO;

        for (SalaryComponent component : components) {

            SalarySlipDetail detail = new SalarySlipDetail();
            detail.setSalary(salary);
            detail.setSalaryComponent(component);
            detail.setAmount(component.getAmount());

            salary.getSlipDetails().add(detail);

            total = total.add(component.getAmount());
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
        return NumberFormat
                .getCurrencyInstance(Locale.of("id", "ID"))
                .format(amount);
    }

    public class QuerydslSortUtil {

    public static OrderSpecifier<?>[] getOrderSpecifiers(
            Sort sort,
            QSalary salary,
            QEmployee employee
    ) {
        return sort.stream()
                .map(order -> {
                    switch (order.getProperty()) {
                        case "month":
                            return order.isAscending() ? salary.month.asc() : salary.month.desc();
                        case "amount":
                            return order.isAscending() ? salary.amount.asc() : salary.amount.desc();
                        case "employeeName":
                            return order.isAscending() ? employee.name.asc() : employee.name.desc();
                        default:
                            return order.isAscending() ? salary.id.asc() : salary.id.desc();
                    }
                })
                .toArray(OrderSpecifier[]::new);
    }
}
}
