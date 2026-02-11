package com.salary.backend_salary.vm.employee;

import com.salary.backend_salary.enums.ApprovalStatus;

import lombok.Data;

@Data
public class EmployeeVM {
    private String name;
    private String position;
    private String email;          
    private String divisionName;   
    private ApprovalStatus status;
}
