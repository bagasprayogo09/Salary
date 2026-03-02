package com.salary.backend_salary.controller.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.security.service.UserDetailsImpl;
import com.salary.backend_salary.service.employee.EmployeeService;
import com.salary.backend_salary.vm.employee.EmployeeRequestVM;
import com.salary.backend_salary.vm.employee.EmployeeVM;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
        
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDPO> createEmployee(
            @RequestBody EmployeeRequestVM vm,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }
        
        vm.setSubmitById(userDetails.getId());

        var created = employeeService.createEmployee(vm);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER')") 
    public ResponseEntity<EmployeeDPO> getEmployeeById(@PathVariable Long id) {
        var dpo = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(dpo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDPO> updateEmployee(
            @PathVariable Long id, 
            @RequestBody EmployeeRequestVM vm,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }
        
        vm.setSubmitById(userDetails.getId());
        
        var updated = employeeService.updateEmployee(id, vm);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) { 
        
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        employeeService.deleteEmployee(id, userDetails.getId());
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<Page<EmployeeDPO>> searchEmployees(
            @ModelAttribute EmployeeVM searchVM, 
            @PageableDefault(
                size = 10,
                sort = "submittedAt",
                direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        var result = employeeService.searchEmployees(searchVM, pageable);
        return ResponseEntity.ok(result);
    }
}