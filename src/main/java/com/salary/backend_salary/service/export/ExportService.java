package com.salary.backend_salary.service.export;

import com.querydsl.core.BooleanBuilder;
import com.salary.backend_salary.entity.employee.QEmployee;
import com.salary.backend_salary.entity.export.ExportJob;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.vm.employee.EmployeeVM;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final EmployeeRepository employeeRepository;

    private static final Map<String, ExportJob> JOB_STORAGE = new ConcurrentHashMap<>();

    public ExportJob triggerExport() {
        ExportJob job = new ExportJob();
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
        ExportJob job = JOB_STORAGE.get(jobId);
        if (job == null) return;

        SXSSFWorkbook workbook = null;
        try {
            updateStatus(job, "PROCESSING", 0);

            QEmployee qEmployee = QEmployee.employee;
            BooleanBuilder builder = new BooleanBuilder();

            if (filterVm != null) {
                if (filterVm.getName() != null && !filterVm.getName().isEmpty())
                    builder.and(qEmployee.name.startsWithIgnoreCase(filterVm.getName()));
                if (filterVm.getPosition() != null && !filterVm.getPosition().isEmpty()) 
                    builder.and(qEmployee.position.startsWithIgnoreCase(filterVm.getPosition()));
                if (filterVm.getEmail() != null && !filterVm.getEmail().isEmpty())
                    builder.and(qEmployee.email.startsWithIgnoreCase(filterVm.getEmail()));
                if (filterVm.getDivisionName() != null && !filterVm.getDivisionName().isEmpty())
                    builder.and(qEmployee.departmen.name.startsWithIgnoreCase(filterVm.getDivisionName()));
                if (filterVm.getStatus() != null)
                    builder.and(qEmployee.status.eq(filterVm.getStatus()));
            }

            workbook = new SXSSFWorkbook(100);
            Sheet sheet = workbook.createSheet("Employees");
            createHeader(sheet);

            long totalRecords = employeeRepository.count(builder);
            
            if (totalRecords == 0) {
                updateStatus(job, "COMPLETED", 100);
                return;
            }

            int batchSize = 2000; 
            int totalPages = (int) Math.ceil((double) totalRecords / batchSize);
            int globalRowIndex = 1;

            for (int i = 0; i < totalPages; i++) {
                Page<Employee> page = employeeRepository.findAll(builder, PageRequest.of(i, batchSize));
                
                for (Employee emp : page.getContent()) {
                Row row = sheet.createRow(globalRowIndex++);
                
                row.createCell(0).setCellValue(emp.getId());
                row.createCell(1).setCellValue(emp.getName());
                row.createCell(2).setCellValue(emp.getPosition());
                row.createCell(3).setCellValue(emp.getEmail());
                row.createCell(4).setCellValue(emp.getNpp()); 
                
            
                if (emp.getDepartmen() != null) {
                    row.createCell(5).setCellValue(emp.getDepartmen().getName());
                } else {
                    row.createCell(5).setCellValue("-"); 
                }
                
                if (emp.getStatus() != null) {
                    row.createCell(6).setCellValue(emp.getStatus().name());
                } else {
                    row.createCell(6).setCellValue("-");
                }
            }

                int progress = (int) (((double) globalRowIndex / totalRecords) * 100);
                if (progress > 99) progress = 99; 
                
                updateStatus(job, "PROCESSING", progress);
            }

            String fileName = "employees_" + job.getId() + ".xlsx";
            String uploadDir = "./uploads/";
            new File(uploadDir).mkdirs(); 

            try (FileOutputStream out = new FileOutputStream(uploadDir + fileName)) {
                workbook.write(out);
            }

            job.setDownloadUrl("/api/export/download/" + fileName);
            updateStatus(job, "COMPLETED", 100);

        } catch (Exception e) {
            e.printStackTrace();
            job.setErrorMessage(e.getMessage());
            updateStatus(job, "FAILED", 0);
        } finally {
            if (workbook != null) workbook.dispose();
        }
    }

    private void updateStatus(ExportJob job, String status, int progress) {
        job.setStatus(status);
        job.setProgress(progress);
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Posisi");
        header.createCell(3).setCellValue("Email");
        header.createCell(4).setCellValue("Npp");
        header.createCell(5).setCellValue("Divisi");
        header.createCell(6).setCellValue("Status");
    }

    @Scheduled(fixedRate = 600000) 
    public void cleanupOldJobs() {
        LocalDateTime limit = LocalDateTime.now().minusMinutes(30);
        JOB_STORAGE.entrySet().removeIf(entry -> 
            entry.getValue().getCreatedAt().isBefore(limit)
        );
    }
}