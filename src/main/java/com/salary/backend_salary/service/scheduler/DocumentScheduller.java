package com.salary.backend_salary.service.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.salary.backend_salary.entity.document.DocumentSubmission;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.repository.document.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentScheduller {

    private final DocumentRepository documentRepository;
    private static final String FINAL_SUFFIX = "_FINAL";

    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void generateAutoFinalDocuments() {
        log.info("---- SCHEDULER START: Scan Dokumen yang belum Final ----");

        Pageable limit = PageRequest.of(0, 100);
        List<Employee> targetEmployees = documentRepository.findTargetsFromDocuments(limit);

        if (targetEmployees.isEmpty()) {
            log.info("-> Semua dokumen sudah punya FINAL. Aman.");
            return;
        }

        log.info("-> Ditemukan {} karyawan yang dokumennya belum difinalisasi.", targetEmployees.size());

        List<DocumentSubmission> newDocs = new ArrayList<>();

        for (Employee emp : targetEmployees) {
            processEmployee(emp).ifPresent(newDocs::add);
        }

        if (!newDocs.isEmpty()) {
            documentRepository.saveAll(newDocs);
            log.info("---- BATCH SELESAI: {} dokumen dibuat ----", newDocs.size());
        }
    }

   
    private Optional<DocumentSubmission> processEmployee(Employee emp) {
        try {
            Optional<DocumentSubmission> lastDocOpt = documentRepository
                    .findTopByEmployeeIdOrderByCreatedAtDesc(emp.getId());

            if (lastDocOpt.isEmpty()) {
                return Optional.empty();
            }

            DocumentSubmission lastDoc = lastDocOpt.get();
            String originalFilename = lastDoc.getFilename();

            if (originalFilename == null || originalFilename.isBlank()) {
                return Optional.empty();
            }

            String finalFilename = generateFinalFilename(originalFilename);

            DocumentSubmission doc = new DocumentSubmission();
            doc.setEmployee(emp);
            doc.setFilename(finalFilename);
            doc.setDocumentType(lastDoc.getDocumentType());
            doc.setDocumentContent(""); 
            doc.setCreatedAt(LocalDateTime.now());
            doc.setCreatedBy(null);

            log.info("   [CREATE] {}", finalFilename);
            return Optional.of(doc);

        } catch (Exception e) {
            log.error("Gagal memproses dokumen untuk employee ID: {}", emp.getId(), e);
            return Optional.empty();
        }
    }

   
    private String generateFinalFilename(String originalFilename) {
        String baseName;
        String extension;
        int dotIndex = originalFilename.lastIndexOf(".");

        if (dotIndex > 0) {
            baseName = originalFilename.substring(0, dotIndex);
            extension = originalFilename.substring(dotIndex);
        } else {
            baseName = originalFilename;
            extension = "";
        }

        if (baseName.toUpperCase().endsWith(FINAL_SUFFIX)) {
            baseName = baseName.substring(0, baseName.length() - FINAL_SUFFIX.length());
        }

        return baseName + FINAL_SUFFIX + extension;
    }
}