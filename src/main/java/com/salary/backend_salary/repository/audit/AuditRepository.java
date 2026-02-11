package com.salary.backend_salary.repository.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.salary.backend_salary.entity.audit.Audit;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long>{
    
    List<Audit> findByEntityNameAndEntityIdOrderByPerformedAtDesc(String entityName, Long entityId);
    List<Audit> findByPerformedBy_UsernameOrderByPerformedAtDesc(String username);
    List<Audit> findAllByOrderByPerformedAtDesc();
}
