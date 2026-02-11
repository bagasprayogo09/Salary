package com.salary.backend_salary.service.document;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salary.backend_salary.dto.document.DocumentRequest;
import com.salary.backend_salary.dto.document.DocumentResponse;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.document.DocumentSubmission;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.repository.document.DocumentRepository;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submitDocument(Long employeeId, DocumentRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("AppUser not found"));

        DocumentSubmission doc = new DocumentSubmission();   
        doc.setEmployee(employee); 
        doc.setFilename(request.getFilename());
        doc.setDocumentType(request.getType());
        doc.setDocumentContent(request.getContent());
        doc.setCreatedAt(LocalDateTime.now());
        doc.setCreatedBy(currentUser);

        documentRepository.save(doc);
    }

  @Transactional(readOnly = true)  
  public List<DocumentResponse> listDocuments(Long employeeId) {
        List<DocumentSubmission> docs = documentRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        
        return docs.stream().map(doc -> {
            DocumentResponse res = new DocumentResponse();
            res.setId(doc.getId());
            res.setFilename(doc.getFilename());
            res.setDocumentType(doc.getDocumentType());
            res.setDocumentContent(doc.getDocumentContent());
            res.setCreatedAt(doc.getCreatedAt());
            
            if (doc.getCreatedBy() != null) {
                res.setCreatedBy(doc.getCreatedBy().getUsername());
            } else {
                res.setCreatedBy("SYSTEM");
            }
            
            return res;
        }).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public DocumentSubmission getDocumentById(Long docId) {
        return documentRepository.findById(docId).orElseThrow(() -> new RuntimeException("No Documents found"));
    }

    @Transactional(readOnly = true)
    public byte[] downloadAllDocumentsAsZip(Long employeeId) {
        List<DocumentSubmission> docs = documentRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        
        if (docs.isEmpty()) {
            throw new RuntimeException("No documents found");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (DocumentSubmission doc : docs) {
                ZipEntry entry = new ZipEntry(doc.getFilename());
                zos.putNextEntry(entry);
                zos.write(doc.getDocumentContent().getBytes());
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to zip documents", e);
        }
    }
}

