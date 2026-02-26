package com.salary.backend_salary.service.export;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.employee.QEmployee;
import com.salary.backend_salary.entity.export.ExportJob;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.vm.employee.EmployeeVM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final EmployeeRepository employeeRepository;
    
    private static final Map<String, ExportJob> JOB_STORAGE = new ConcurrentHashMap<>();
    private static final String UPLOAD_DIR = "uploads";

    public ExportJob triggerExport() {
        var job = new ExportJob();
        job.setId(UUID.randomUUID().toString());
        job.setStatus("PENDING");
        job.setProgress(0);
        job.setCreatedAt(LocalDateTime.now());

        JOB_STORAGE.put(job.getId(), job);
        return job;
    }

    public ExportJob getJob(String jobId) {
        return JOB_STORAGE.get(jobId);
    }

    @Async
    public void processExportAsync(String jobId, EmployeeVM filterVm) {
        var job = JOB_STORAGE.get(jobId);
        if (job == null) return;

        try (var workbook = new SXSSFWorkbook(100)) { 
            updateStatus(job, "PROCESSING", 0);

            var builder = buildPredicate(filterVm);
            long totalRecords = employeeRepository.count(builder);

            if (totalRecords == 0) {
                updateStatus(job, "COMPLETED", 100);
                return;
            }

            var sheet = workbook.createSheet("Employees");
            createHeader(sheet);

            int batchSize = 2000;
            int totalPages = (int) Math.ceil((double) totalRecords / batchSize);
            int globalRowIndex = 1;

            for (int i = 0; i < totalPages; i++) {
                var pageRequest = PageRequest.of(i, batchSize);
                var page = employeeRepository.findAll(builder, pageRequest);

                for (var emp : page.getContent()) {
                    writeRow(sheet, globalRowIndex++, emp);
                }

                int progress = (int) (((double) globalRowIndex / totalRecords) * 100);
                updateStatus(job, "PROCESSING", Math.min(progress, 99));
                
                
            }

            String fileName = "employees_" + job.getId() + ".xlsx";
            Path uploadPath = Paths.get(UPLOAD_DIR);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            try (var fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }

            workbook.dispose(); 

            job.setDownloadUrl("/api/export/download/" + fileName);
            updateStatus(job, "COMPLETED", 100);

        } catch (Exception e) {
            log.error("Export failed", e);
            job.setErrorMessage("Export failed: " + e.getMessage());
            updateStatus(job, "FAILED", 0);
        }
    }

    private BooleanBuilder buildPredicate(EmployeeVM filterVm) {
        var qEmployee = QEmployee.employee;
        var builder = new BooleanBuilder();

        if (filterVm != null) {
            if (isValid(filterVm.getName())) 
                builder.and(qEmployee.name.containsIgnoreCase(filterVm.getName())); 
            if (isValid(filterVm.getPosition())) 
                builder.and(qEmployee.position.containsIgnoreCase(filterVm.getPosition()));
            if (isValid(filterVm.getDivisionName())) 
                builder.and(qEmployee.departmen.name.containsIgnoreCase(filterVm.getDivisionName()));
            if (filterVm.getStatus() != null)
                builder.and(qEmployee.status.eq(filterVm.getStatus()));
        }
        return builder;
    }

    private void writeRow(Sheet sheet, int rowIndex, Employee emp) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(emp.getId());
        row.createCell(1).setCellValue(emp.getName());
        row.createCell(2).setCellValue(emp.getPosition());
        row.createCell(3).setCellValue(emp.getEmail());
        row.createCell(4).setCellValue(emp.getNpp());
        
        row.createCell(5).setCellValue(emp.getDepartmen() != null ? emp.getDepartmen().getName() : "-");
        row.createCell(6).setCellValue(emp.getStatus() != null ? emp.getStatus().name() : "-");
    }

    private boolean isValid(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private void updateStatus(ExportJob job, String status, int progress) {
        job.setStatus(status);
        job.setProgress(progress);
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Posisi", "Email", "NPP", "Divisi", "Status"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }

    @Scheduled(fixedRate = 600000)
    public void cleanupOldJobs() {
        LocalDateTime limit = LocalDateTime.now().minusMinutes(30);
        JOB_STORAGE.values().removeIf(job -> job.getCreatedAt().isBefore(limit));
        
    }
}