package com.salary.backend_salary.service.audit;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.audit.Audit;
import com.salary.backend_salary.repository.audit.AuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Async("auditTaskExecutor")
    @Transactional
    public void logAudit(
            String entityName,
            Long entityId,
            String action,
            Object oldObj,
            Object newObj,
            AppUser actor
    ) {
        try {
            String oldValue = oldObj != null ? oldObj.toString() : null;
            String newValue = newObj != null ? newObj.toString() : null;

            Audit audit = new Audit(
                    entityName,
                    entityId,
                    action,
                    oldValue,
                    newValue,
                    actor
            );

            auditRepository.save(audit);

            System.out.println(
                "[AUDIT ASYNC] thread=" 
                + Thread.currentThread().getName()
                + " entity=" + entityName
                + " action=" + action
            );

        } catch (Exception e) {
            System.err.println("[AUDIT ERROR] " + e.getMessage());
        }
    }



    @Transactional(readOnly = true)
    public List<Audit> getAllAudits() {
        return auditRepository.findAllByOrderByPerformedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Audit> getAuditsByEntity(String entityName, Long entityId) {
        return auditRepository
                .findByEntityNameAndEntityIdOrderByPerformedAtDesc(entityName, entityId);
    }

    @Transactional(readOnly = true)
    public List<Audit> getAuditsByActor(String username) {
        return auditRepository
                .findByPerformedBy_UsernameOrderByPerformedAtDesc(username);
    }
}
