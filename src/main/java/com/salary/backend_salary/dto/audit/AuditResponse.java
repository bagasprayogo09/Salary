package com.salary.backend_salary.dto.audit;

import java.time.LocalDateTime;

public record AuditResponse(
    Long id,
    String entityName,
    Long entityId,
    String action,
    String oldValue,
    String newValue,
    String performedBy,
    LocalDateTime performedAt
) {}

