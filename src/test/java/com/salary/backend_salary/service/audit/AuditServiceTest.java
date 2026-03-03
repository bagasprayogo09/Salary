package com.salary.backend_salary.service.audit;

import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.audit.Audit;
import com.salary.backend_salary.repository.audit.AuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditService auditService;

    // --- TEST LOG AUDIT ---

    @Test
    void logAudit_Success() {
        // Arrange
        String entityName = "EMPLOYEE";
        Long entityId = 1L;
        String action = "CREATE";
        Object oldObj = null;
        Object newObj = "New Data";
        AppUser actor = new AppUser();
        actor.setUsername("admin");

        // Act
        auditService.logAudit(entityName, entityId, action, oldObj, newObj, actor);

        // Assert
        // Kita gunakan ArgumentCaptor untuk menangkap objek Audit yang dikirim ke repository.save()
        ArgumentCaptor<Audit> auditCaptor = ArgumentCaptor.forClass(Audit.class);
        verify(auditRepository).save(auditCaptor.capture());

        Audit savedAudit = auditCaptor.getValue();
        assertEquals(entityName, savedAudit.getEntityName());
        assertEquals(entityId, savedAudit.getEntityId());
        assertEquals(action, savedAudit.getAction());
        assertNull(savedAudit.getOldValue());
        assertEquals("New Data", savedAudit.getNewValue());
        assertEquals(actor, savedAudit.getPerformedBy());
    }

    @Test
    void logAudit_Exception_ShouldNotThrow() {
        // Arrange
        doThrow(new RuntimeException("DB Error")).when(auditRepository).save(any(Audit.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            auditService.logAudit("TEST", 1L, "TEST", null, null, null)
        );

        verify(auditRepository).save(any(Audit.class));
    }

    // --- TEST GET ALL ---

    @Test
    void getAllAudits_Success() {
        // Arrange
        Audit audit = new Audit();
        when(auditRepository.findAllByOrderByPerformedAtDesc())
                .thenReturn(Collections.singletonList(audit));

        // Act
        List<Audit> result = auditService.getAllAudits();

        // Assert
        assertEquals(1, result.size());
        verify(auditRepository).findAllByOrderByPerformedAtDesc();
    }

    // --- TEST GET BY ENTITY ---

    @Test
    void getAuditsByEntity_Success() {
        // Arrange
        String entityName = "EMPLOYEE";
        Long entityId = 100L;
        Audit audit = new Audit();
        
        when(auditRepository.findByEntityNameAndEntityIdOrderByPerformedAtDesc(entityName, entityId))
                .thenReturn(List.of(audit));

        // Act
        List<Audit> result = auditService.getAuditsByEntity(entityName, entityId);

        // Assert
        assertEquals(1, result.size());
        verify(auditRepository).findByEntityNameAndEntityIdOrderByPerformedAtDesc(entityName, entityId);
    }


    @Test
    void getAuditsByActor_Success() {
        String username = "bagas";
        Audit audit = new Audit();

        when(auditRepository.findByPerformedBy_UsernameOrderByPerformedAtDesc(username))
                .thenReturn(List.of(audit));

        List<Audit> result = auditService.getAuditsByActor(username);

        // Assert
        assertEquals(1, result.size());
        verify(auditRepository).findByPerformedBy_UsernameOrderByPerformedAtDesc(username);
    }
}