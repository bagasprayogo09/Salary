package com.salary.backend_salary.repository.dashboard;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.salary.backend_salary.dto.dashboard.DashboardSalaryByDivisionDTO;
import com.salary.backend_salary.dto.dashboard.DashboardSummaryDTO;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.QEmployee;
import com.salary.backend_salary.entity.salary.QSalary;
import com.salary.backend_salary.enums.ApprovalStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DashboardRepositoryImpl implements DashboardRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Employee> findRecentEmployeesByStatus(ApprovalStatus status, int limit) {
        QEmployee qEmployee = QEmployee.employee;
        
        JPAQuery<Employee> query = new JPAQuery<>(entityManager);
        
        return query.from(qEmployee)
                .leftJoin(qEmployee.departmen).fetchJoin()
                .leftJoin(qEmployee.submitBy).fetchJoin()
                .where(qEmployee.status.eq(status))
                .orderBy(qEmployee.submittedAt.desc())
                .limit(limit)
                .distinct()
                .fetch();
    }

    @Override
    public List<Employee> findAllEmployeesByStatus(ApprovalStatus status) {
        QEmployee qEmployee = QEmployee.employee;
        
        JPAQuery<Employee> query = new JPAQuery<>(entityManager);
        
        return query.from(qEmployee)
                .leftJoin(qEmployee.departmen).fetchJoin()
                .leftJoin(qEmployee.submitBy).fetchJoin()
                .where(qEmployee.status.eq(status))
                .orderBy(qEmployee.name.asc())
                .distinct()
                .fetch();
    }

    @Override
    public List<DashboardSalaryByDivisionDTO> findSalaryByDivision() {
    QEmployee qEmployee = QEmployee.employee;
    QSalary qSalary = QSalary.salary;

    JPAQuery<DashboardSalaryByDivisionDTO> query = new JPAQuery<>(entityManager);

    return query
            .select(Projections.constructor(
                    DashboardSalaryByDivisionDTO.class,
                    qEmployee.departmen.name,
                    qEmployee.count(),
                    qSalary.amount.sum().coalesce(BigDecimal.ZERO)
            ))
            .from(qSalary)
            .join(qSalary.employee, qEmployee)
            .join(qEmployee.departmen)
            .groupBy(qEmployee.departmen.id, qEmployee.departmen.name)
            .orderBy(qSalary.amount.sum().desc())
            .fetch();
    }
    
    @Override
    public DashboardSummaryDTO findSummary() {
    QEmployee qEmployee = QEmployee.employee;
    QSalary qSalary = QSalary.salary;

    JPAQuery<Long> baseQuery = new JPAQuery<>(entityManager);

    Long totalEmployees = baseQuery
            .select(qEmployee.count())
            .from(qEmployee)
            .fetchOne();

    Long totalDivisions = new JPAQuery<Long>(entityManager)
            .select(qEmployee.departmen.countDistinct())
            .from(qEmployee)
            .fetchOne();

    Long pendingApprovals = new JPAQuery<Long>(entityManager)
            .select(qEmployee.count())
            .from(qEmployee)
            .where(qEmployee.status.in(
                    ApprovalStatus.PENDING_CREATE,
                    ApprovalStatus.PENDING_UPDATE,
                    ApprovalStatus.PENDING_DELETE
            ))
            .fetchOne();

    Double totalSalary = new JPAQuery<Double>(entityManager)
            .select(qSalary.amount.sum().doubleValue())
            .from(qSalary)
            .join(qSalary.employee, qEmployee)
            .where(qEmployee.status.in(
                ApprovalStatus.PENDING_CREATE,
                ApprovalStatus.PENDING_UPDATE,
                ApprovalStatus.APPROVED
            ))
            .fetchOne();

    return new DashboardSummaryDTO(
            totalEmployees != null ? totalEmployees : 0L,
            totalDivisions != null ? totalDivisions : 0L,
            pendingApprovals != null ? pendingApprovals : 0L,
            totalSalary != null ? totalSalary : 0.0
    );
    }
}
