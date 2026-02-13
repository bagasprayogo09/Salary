package com.salary.backend_salary.dto.salary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalaryRequest {
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotBlank(message = "Month is required")
    private String month;
}
