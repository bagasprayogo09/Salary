package com.salary.backend_salary.service.employee;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.vm.employee.ApprovalRequestVM;

public interface ApprovalService {
    EmployeeDPO processApproval(Long employeeId, ApprovalRequestVM vm);
    
    EmployeeDPO getDetail(Long employeeId);
}
