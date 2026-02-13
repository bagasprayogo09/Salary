package com.salary.backend_salary.controller.salary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.salary.backend_salary.dto.salary.CreateSalaryRequest;
import com.salary.backend_salary.dto.salary.SalaryFilterRequest;
import com.salary.backend_salary.dto.salary.SalaryResponse;
import com.salary.backend_salary.service.salary.SalaryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SalaryControlller {
    
    private final SalaryService salaryService;
    
   
    @GetMapping("/list")
    public ResponseEntity<Page<SalaryResponse>> getAllSalaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String employeeName) {
        
        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            String field = sortParams[0];
            String direction = sortParams.length > 1 ? sortParams[1] : "asc";
            sorting = "desc".equalsIgnoreCase(direction) ? 
                      Sort.by(field).descending() : 
                      Sort.by(field).ascending();
        }
        
        Pageable pageable = PageRequest.of(page, size, sorting);
        SalaryFilterRequest filter = new SalaryFilterRequest(month, employeeName);
        
        Page<SalaryResponse> salaries = salaryService.getAllSalaries(filter, pageable);
        
        return ResponseEntity.ok(salaries);
    }
    
    
     
    @GetMapping("/{id}")
    public ResponseEntity<SalaryResponse> getSalaryById(@PathVariable Long id) {
        SalaryResponse salary = salaryService.getSalaryById(id);
        return ResponseEntity.ok(salary);
    }
    
    
     
    @PostMapping("/create")
    public ResponseEntity<SalaryResponse> createSalary(@Valid @RequestBody CreateSalaryRequest request) {
        SalaryResponse createdSalary = salaryService.createSalary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSalary);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SalaryResponse> updateSalary(
            @PathVariable Long id,
            @Valid @RequestBody CreateSalaryRequest request) {
        
        SalaryResponse updatedSalary = salaryService.updateSalary(id, request);
        return ResponseEntity.ok(updatedSalary);
    }
     
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalary(@PathVariable Long id) {
        salaryService.deleteSalary(id);
        return ResponseEntity.noContent().build();
    }
}