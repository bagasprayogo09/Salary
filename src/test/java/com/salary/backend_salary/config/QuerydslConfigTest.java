package com.salary.backend_salary.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class QuerydslConfigTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private QuerydslConfig querydslConfig;

    @Test
    void testJpaQueryFactory() {
        JPAQueryFactory factory = querydslConfig.jpaQueryFactory();

        assertNotNull(factory, "JPAQueryFactory tidak boleh null");
    }
}