package com.salary.backend_salary.repository.dashboard;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.QEmployee;
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
}
