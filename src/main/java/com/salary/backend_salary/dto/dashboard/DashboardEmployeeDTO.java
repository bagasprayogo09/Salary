package com.salary.backend_salary.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEmployeeDTO {
    private String name;
    private String position;
    private String division;
}
