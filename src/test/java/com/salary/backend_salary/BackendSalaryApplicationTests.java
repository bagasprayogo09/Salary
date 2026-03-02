package com.salary.backend_salary;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Skip loading context entirely to avoid DB connection errors")
@SpringBootTest
class BackendSalaryApplicationTests {

    @Test
    void contextLoads() {
    }

}