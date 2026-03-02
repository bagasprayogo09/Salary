package com.salary.backend_salary.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @Test
    void testDashboardTaskExecutor() {
        Executor executor = asyncConfig.dashboardTaskExecutor();

        assertNotNull(executor);
        assertTrue(executor instanceof TaskExecutorAdapter);
    }

    @Test
    void testAuditTaskExecutor() {
        Executor executor = asyncConfig.auditTaskExecutor();

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);

        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) executor;
        
        assertEquals(2, threadPool.getCorePoolSize());
        assertEquals(4, threadPool.getMaxPoolSize());
        assertEquals("audit-", threadPool.getThreadNamePrefix());
    }

    @Test
    void testTaskExecutor() {
        Executor executor = asyncConfig.taskExecutor();

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);

        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) executor;
        
        assertEquals(5, threadPool.getCorePoolSize());
        assertEquals(10, threadPool.getMaxPoolSize());
        assertEquals("salart-async", threadPool.getThreadNamePrefix());
    }
}