package com.salary.backend_salary.repository.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.QEmployee;

import jakarta.persistence.EntityManager;

@DataJpaTest
class EmployeeRepositoryImplTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should return paginated employees with default sorting")
    void searchWithDetails_defaultSort() {

        // given
        Employee emp1 = new Employee();
        emp1.setName("Budi");

        Employee emp2 = new Employee();
        emp2.setName("Andi");

        entityManager.persist(emp1);
        entityManager.persist(emp2);
        entityManager.flush();

        QEmployee qEmployee = QEmployee.employee;
        Predicate predicate = qEmployee.isNotNull();

        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<Employee> result = employeeRepository.searchWithDetails(predicate, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should sort by name ascending")
    void searchWithDetails_sortAscending() {

        // given
        Employee emp1 = new Employee();
        emp1.setName("Zaki");

        Employee emp2 = new Employee();
        emp2.setName("Andi");

        entityManager.persist(emp1);
        entityManager.persist(emp2);
        entityManager.flush();

        QEmployee qEmployee = QEmployee.employee;
        Predicate predicate = qEmployee.isNotNull();

        PageRequest pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.ASC, "name")
        );

        // when
        Page<Employee> result = employeeRepository.searchWithDetails(predicate, pageable);

        // then
        List<Employee> employees = result.getContent();

        assertThat(employees.get(0).getName()).isEqualTo("Andi");
        assertThat(employees.get(1).getName()).isEqualTo("Zaki");
    }

    @Test
    @DisplayName("Should filter employee by name")
    void searchWithDetails_filterByName() {

        // given
        Employee emp1 = new Employee();
        emp1.setName("Budi");

        Employee emp2 = new Employee();
        emp2.setName("Andi");

        entityManager.persist(emp1);
        entityManager.persist(emp2);
        entityManager.flush();

        QEmployee qEmployee = QEmployee.employee;
        BooleanExpression predicate = qEmployee.name.eq("Budi");

        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<Employee> result = employeeRepository.searchWithDetails(predicate, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Budi");
    }

    @Test
    @DisplayName("Should return empty page if no data found")
    void searchWithDetails_noResult() {

        QEmployee qEmployee = QEmployee.employee;
        BooleanExpression predicate = qEmployee.name.eq("TidakAda");

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Employee> result = employeeRepository.searchWithDetails(predicate, pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}