package com.salary.backend_salary.util;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.OrderSpecifier;
import com.salary.backend_salary.entity.employee.QEmployee;
import com.salary.backend_salary.entity.salary.QSalary;

public class QuerydslSortUtil {
    
    private QuerydslSortUtil() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
   public static OrderSpecifier<Comparable>[] getSalaryOrderSpecifiers(
            Sort sort, QSalary salary, QEmployee employee) {

        return sort.stream()
                .map(order -> {
                    OrderSpecifier<?> spec = switch (order.getProperty()) {
                        case "month"        -> order.isAscending() ? salary.month.asc()   : salary.month.desc();
                        case "amount"       -> order.isAscending() ? salary.amount.asc()  : salary.amount.desc();
                        case "employeeName" -> order.isAscending() ? employee.name.asc()  : employee.name.desc();
                        default             -> order.isAscending() ? salary.id.asc()      : salary.id.desc();
                    };
                    return (OrderSpecifier<Comparable>) spec;
                })
                .toArray(OrderSpecifier[]::new);
    }
}
