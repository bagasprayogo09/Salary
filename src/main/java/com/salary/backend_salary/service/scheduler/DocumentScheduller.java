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

@Component
@RequiredArgsConstructor
public class DocumentScheduller {
    
    private final DocumentRepository documentRepository;

    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void generateAutoFinalDocuments() {
        System.out.println("---- SCHEDULER START: Scan Dokumen yang belum Final ----");

        Pageable limit = PageRequest.of(0, 100);
        
        List<Employee> targetEmployees = documentRepository.findTargetsFromDocuments(limit);

        if (targetEmployees.isEmpty()) {
            System.out.println("-> Semua dokumen sudah punya FINAL. Aman.");
            return;
        }

        System.out.println("-> Ditemukan " + targetEmployees.size() + " karyawan yang dokumennya belum difinalisasi.");

        List<DocumentSubmission> newDocs = new ArrayList<>();

        for (Employee emp : targetEmployees) {
            try {
                Optional<DocumentSubmission> lastDocOpt = documentRepository.findTopByEmployeeIdOrderByCreatedAtDesc(emp.getId());
                
                if (lastDocOpt.isEmpty()) continue;

                DocumentSubmission lastDoc = lastDocOpt.get();
                String finalType = lastDoc.getDocumentType();
                String extension = "csv".equalsIgnoreCase(finalType) ? ".csv" : ".txt";
                
                String finalFilename = "AUTO_FINAL_" + emp.getNpp() + "_" + System.currentTimeMillis() + extension;
                
                DocumentSubmission doc = new DocumentSubmission();
                doc.setEmployee(emp);
                doc.setFilename(finalFilename);
                doc.setDocumentType(finalType); 
                doc.setDocumentContent("");     
                doc.setCreatedAt(LocalDateTime.now());
                doc.setCreatedBy(null);         

                newDocs.add(doc);
                System.out.println("   [CREATE] " + finalFilename);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!newDocs.isEmpty()) {
            documentRepository.saveAll(newDocs);
            System.out.println("---- BATCH SELESAI: " + newDocs.size() + " dokumen dibuat ----");
        }
    }
}