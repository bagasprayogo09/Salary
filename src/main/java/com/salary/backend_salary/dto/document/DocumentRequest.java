package com.salary.backend_salary.dto.document;

import lombok.Data;

@Data
public class DocumentRequest {
    private String filename;
    private String content;
    private String type;
}
