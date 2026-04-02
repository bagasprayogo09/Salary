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
public class SalaryController {

    private final SalaryService salaryService;

    @GetMapping("/list")
    public ResponseEntity<Page<SalaryResponse>> getAllSalaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String employeeName) {

        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        SalaryFilterRequest filter = new SalaryFilterRequest(month, employeeName);

        return ResponseEntity.ok(salaryService.getAllSalaries(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaryResponse> getSalaryById(@PathVariable Long id) {
        return ResponseEntity.ok(salaryService.getSalaryById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<SalaryResponse> createSalary(@Valid @RequestBody CreateSalaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaryService.createSalary(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryResponse> updateSalary(
            @PathVariable Long id,
            @Valid @RequestBody CreateSalaryRequest request) {
        return ResponseEntity.ok(salaryService.updateSalary(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalary(@PathVariable Long id) {
        salaryService.deleteSalary(id);
        return ResponseEntity.noContent().build();
    }

    private Sort buildSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.unsorted();
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim() : "asc";
        return "desc".equalsIgnoreCase(direction)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }
}