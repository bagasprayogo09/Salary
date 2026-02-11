package com.salary.backend_salary.dpo.employee;
import java.time.LocalDateTime;
import com.salary.backend_salary.enums.ApprovalStatus;
import lombok.Data;

@Data
public class EmployeeDPO {
    private Long id;
    private String name;
    private String position;
    private String email;
    private String npp;
    
    private Long departmentId;
    private String departmentName; 
    
    private ApprovalStatus status;
    private Long approvedBy;
    private String statusemp;
    
    
    private String submitByName;
    private LocalDateTime submittedAt;
    
    private LocalDateTime approvedAt;
}