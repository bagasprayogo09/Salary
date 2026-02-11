package com.salary.backend_salary.repository.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.QEmployee;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Employee> searchWithDetails(Predicate predicate, Pageable pageable) {
        QEmployee qEmployee = QEmployee.employee;
        
        JPAQuery<Employee> query = new JPAQuery<>(entityManager);
        query.from(qEmployee)
             .leftJoin(qEmployee.departmen).fetchJoin()
             .leftJoin(qEmployee.submitBy).fetchJoin()
             .where(predicate)
             .distinct(); 
        JPAQuery<Long> countQuery = new JPAQuery<>(entityManager);
        long total = countQuery.from(qEmployee)
                               .where(predicate)
                               .select(com.querydsl.core.types.dsl.Expressions.asNumber(1).count())
                               .fetchOne();
        
        query.offset(pageable.getOffset())
             .limit(pageable.getPageSize());
        
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                PathBuilder<Employee> entityPath = new PathBuilder<>(Employee.class, "employee");
                query.orderBy(order.isAscending() 
                    ? entityPath.getString(order.getProperty()).asc()
                    : entityPath.getString(order.getProperty()).desc());
            });
        }
        
        return new PageImpl<>(query.fetch(), pageable, total);
    }
}