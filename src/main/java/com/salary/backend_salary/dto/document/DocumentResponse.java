package com.salary.backend_salary.dto.document;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DocumentResponse {
    private Long id;
    private String filename;
    private String documentType;
    private String documentContent;
    private String createdBy; 
    private LocalDateTime createdAt;
}
