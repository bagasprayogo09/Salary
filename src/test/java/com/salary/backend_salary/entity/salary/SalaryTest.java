package com.salary.backend_salary.entity.salary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SalaryTest {
    
    @Test
    void testEqualsAndHashCode() {
        Salary salary1 = new Salary();
        salary1.setId(100L);

        Salary salary2 = new Salary();
        salary2.setId(100L);

        Salary salary3 = new Salary();
        salary3.setId(999L);

        Salary salaryNullId = new Salary();

        assertEquals(salary1, salary1, "Objek yang sama harus dianggap sama");

        assertNotEquals(null, salary1, "Dibandingkan dengan null harus false");
        assertNotEquals(new Object(), salary1, "Beda class tidak boleh equals");

        assertEquals(salary1, salary2, "Dua entitas dengan id yang sama harus dianggap sama");
        assertNotEquals(salary1, salary3, "Dua entitas dengan id yang berbeda harus dianggap berbeda");
        assertNotEquals(salaryNullId, salary1, "Entitas dengan ID null tidak boleh dianggap sama dengan entitas yang memiliki ID");
        
        
        assertEquals(salary1.hashCode(), salary2.hashCode(), "Entitas dengan id yang sama harus memiliki hashCode yang sama");
        assertEquals(salary1.hashCode(), salaryNullId.hashCode(), "Entitas dengan ID null harus memiliki hashCode yang sama");
    }
}       
