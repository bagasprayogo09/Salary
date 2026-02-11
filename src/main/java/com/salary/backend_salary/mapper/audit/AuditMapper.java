package com.salary.backend_salary.mapper.audit;

import com.salary.backend_salary.dto.audit.AuditResponse;
import com.salary.backend_salary.entity.audit.Audit;

public class AuditMapper {

    private AuditMapper() {
    }

    public static AuditResponse toResponse(Audit audit) {
        if (audit == null) {
            return null;
        }

        return new AuditResponse(
            audit.getId(),
            audit.getEntityName(),
            audit.getEntityId(),
            audit.getAction(),
            audit.getOldValue(),
            audit.getNewValue(),
            audit.getPerformedBy() != null
                ? audit.getPerformedBy().getUsername()
                : null,
            audit.getPerformedAt()
        );
    }
}
