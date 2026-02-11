package com.salary.backend_salary.entity.export;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class ExportJob {

    private String id;             
    private String status;        
    private Integer progress;       
    private String downloadUrl;     
    private String errorMessage;    
    private LocalDateTime createdAt;
    
}
