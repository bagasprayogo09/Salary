package com.salary.backend_salary.controller.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.salary.backend_salary.entity.export.ExportJob;
import com.salary.backend_salary.service.export.ExportService;
import com.salary.backend_salary.vm.employee.EmployeeVM;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    
    private static final String UPLOAD_DIR = "uploads"; 

    @PostMapping("/start")
    public ResponseEntity<ExportJob> startExport(@RequestBody(required = false) EmployeeVM filterVm) {
        ExportJob job = exportService.triggerExport();
        exportService.processExportAsync(job.getId(), filterVm);
        
        return ResponseEntity.ok(job);
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<ExportJob> checkStatus(@PathVariable String jobId) {
        ExportJob job = exportService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path baseDir = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Path filePath = baseDir.resolve(fileName).normalize();

            if (!filePath.startsWith(baseDir)) {
                return ResponseEntity.badRequest().build(); 
            }

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath.toFile());

            var contentDisposition = ContentDisposition.attachment()
                    .filename(fileName)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}