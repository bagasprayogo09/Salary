package com.salary.backend_salary.controller.export;

import com.salary.backend_salary.entity.export.ExportJob;
import com.salary.backend_salary.service.export.ExportService;
import com.salary.backend_salary.vm.employee.EmployeeVM;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

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

    // 3. Download File
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        File file = new File("./uploads/" + fileName);
        if (!file.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}