package com.salary.backend_salary.controller.audit;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salary.backend_salary.entity.audit.Audit;
import com.salary.backend_salary.service.audit.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {
    private final  AuditService auditService;

    @GetMapping
    public ResponseEntity<List<Audit>> getAllAudits() {
        return ResponseEntity.ok(auditService.getAllAudits());
    }

    @GetMapping("/history")
    public ResponseEntity<List<Audit>> getEntityHistory(
            @RequestParam String entity,
            @RequestParam Long id) {
        
        List<Audit> logs = auditService.getAuditsByEntity(entity.toUpperCase(), id);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/actor/{username}")
    public ResponseEntity<List<Audit>> getAuditsByActor(@PathVariable String username) {
        return ResponseEntity.ok(auditService.getAuditsByActor(username));
    }
}
