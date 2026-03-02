package com.salary.backend_salary.service.export;

import com.querydsl.core.types.Predicate;
import com.salary.backend_salary.entity.departmen.Department;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.entity.export.ExportJob;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.vm.employee.EmployeeVM;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ExportService exportService;

    private static final String UPLOAD_DIR = "uploads";

    @BeforeEach
    void setUp() throws Exception {
        getJobStorageMap().clear();
        
        Files.createDirectories(Paths.get(UPLOAD_DIR));
    }

    @AfterEach
    void tearDown() throws Exception {
        getJobStorageMap().clear();
    }

    @SuppressWarnings("unchecked")
    private Map<String, ExportJob> getJobStorageMap() throws Exception {
        Field field = ExportService.class.getDeclaredField("JOB_STORAGE");
        field.setAccessible(true);
        return (Map<String, ExportJob>) field.get(null); 
    }

    @Test
    void testTriggerExport() {
        ExportJob job = exportService.triggerExport();

        assertNotNull(job);
        assertNotNull(job.getId());
        assertEquals("PENDING", job.getStatus());
        assertEquals(0, job.getProgress());
        assertNotNull(exportService.getJob(job.getId()));
    }

    @Test
    void testProcessExportAsync_Success_WithData() throws IOException {
        ExportJob job = exportService.triggerExport();
        String jobId = job.getId();
        EmployeeVM filter = new EmployeeVM();

        Department dept = new Department();
        dept.setName("IT");

        Employee emp = new Employee();
        emp.setId(101L);
        emp.setName("Bagas");
        emp.setPosition("Dev");
        emp.setEmail("bagas@mail.com");
        emp.setNpp("12345");
        emp.setDepartmen(dept);
        emp.setStatus(ApprovalStatus.APPROVED);

        Page<Employee> page = new PageImpl<>(List.of(emp));

        when(employeeRepository.count(any(Predicate.class))).thenReturn(1L);
        when(employeeRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

        exportService.processExportAsync(jobId, filter);

        ExportJob updatedJob = exportService.getJob(jobId);
        assertEquals("COMPLETED", updatedJob.getStatus());
        assertEquals(100, updatedJob.getProgress());
        assertNotNull(updatedJob.getDownloadUrl());
        assertTrue(updatedJob.getDownloadUrl().contains(jobId));

        Path filePath = Paths.get(UPLOAD_DIR, "employees_" + jobId + ".xlsx");
        assertTrue(Files.exists(filePath), "File Excel harusnya terbentuk");

        Files.deleteIfExists(filePath);
    }

    @Test
    void testProcessExportAsync_Success_NoData() {
        ExportJob job = exportService.triggerExport();
        String jobId = job.getId();

        when(employeeRepository.count(any(Predicate.class))).thenReturn(0L);

        exportService.processExportAsync(jobId, new EmployeeVM());

        ExportJob updatedJob = exportService.getJob(jobId);
        assertEquals("COMPLETED", updatedJob.getStatus());
        assertEquals(100, updatedJob.getProgress());
        
        verify(employeeRepository, never()).findAll(any(Predicate.class), any(Pageable.class));
    }

    @Test
    void testProcessExportAsync_Failed_Exception() {
        ExportJob job = exportService.triggerExport();
        String jobId = job.getId();

        when(employeeRepository.count(any(Predicate.class))).thenThrow(new RuntimeException("Database Error"));

        exportService.processExportAsync(jobId, null);

        ExportJob updatedJob = exportService.getJob(jobId);
        assertEquals("FAILED", updatedJob.getStatus());
        assertEquals(0, updatedJob.getProgress());
        assertTrue(updatedJob.getErrorMessage().contains("Database Error"));
    }

    @Test
    void testProcessExportAsync_JobNotFound() {
        exportService.processExportAsync("invalid-id", null);

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void testCleanupOldJobs() throws Exception {
        Map<String, ExportJob> storage = getJobStorageMap();

        ExportJob newJob = new ExportJob();
        newJob.setId("new-1");
        newJob.setCreatedAt(LocalDateTime.now());
        storage.put(newJob.getId(), newJob);

        ExportJob oldJob = new ExportJob();
        oldJob.setId("old-1");
        oldJob.setCreatedAt(LocalDateTime.now().minusMinutes(40));
        storage.put(oldJob.getId(), oldJob);

        assertEquals(2, storage.size());

        exportService.cleanupOldJobs();

        assertEquals(1, storage.size());
        assertTrue(storage.containsKey("new-1"));
        assertFalse(storage.containsKey("old-1"));
    }
}