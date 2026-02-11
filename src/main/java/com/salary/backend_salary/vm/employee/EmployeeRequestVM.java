package com.salary.backend_salary.vm.employee;

import lombok.Data;

@Data
public class EmployeeRequestVM {
    private String name;
    private String position;
    private String email;
    private String npp;
    private Long departmentId;
    private String statusemp;
    private Long submitById;
}
