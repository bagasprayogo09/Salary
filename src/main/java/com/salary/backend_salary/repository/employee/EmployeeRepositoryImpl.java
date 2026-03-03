package com.salary.backend_salary.repository.employee;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparablePath; 
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

    @SuppressWarnings("unchecked") 
    @Override   
    public Page<Employee> searchWithDetails(Predicate predicate, Pageable pageable) {
        QEmployee qEmployee = QEmployee.employee;

        JPAQuery<Employee> query = new JPAQuery<>(entityManager);
        query.from(qEmployee)
             .leftJoin(qEmployee.departmen).fetchJoin()
             .leftJoin(qEmployee.submitBy).fetchJoin()
             .where(predicate);

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

       if (pageable.getSort().isSorted()) {
            PathBuilder<Employee> entityPath = new PathBuilder<>(Employee.class, "employee");
            
            for (Sort.Order order : pageable.getSort()) {
                
                ComparablePath<Comparable<?>> path = entityPath.getComparable(
                    order.getProperty(), 
                    (Class<Comparable<?>>) (Class<?>) Comparable.class
                );
                
               query.orderBy(new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC, 
                    path
                ));
            }
        } else {
            query.orderBy(qEmployee.id.desc());
        }

        List<Employee> content = query.fetch();

        JPAQuery<Long> countQuery = new JPAQuery<>(entityManager);
        Long total = countQuery.select(qEmployee.count())
                               .from(qEmployee)
                               .where(predicate)
                               .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}