package com.salary.backend_salary.entity.audit;

import java.time.LocalDateTime;

import com.salary.backend_salary.entity.appusers.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit")
@Data
@NoArgsConstructor
public class Audit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId; 

    private String action; 

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; 

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    @ManyToOne
    @JoinColumn(name = "performed_by")
    private AppUser performedBy; 

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    public Audit(String entityName, Long entityId, String action, String oldValue, String newValue, AppUser performedBy) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.performedBy = performedBy;
        this.performedAt = LocalDateTime.now();
    }
}