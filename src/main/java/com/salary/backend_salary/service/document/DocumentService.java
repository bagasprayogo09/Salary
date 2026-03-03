package com.salary.backend_salary.service.document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submitDocument(Long employeeId, DocumentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new SecurityException("User is not authenticated");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        AppUser currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("AppUser not found with username: " + auth.getName()));

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
        
        return docs.stream().map(this::mapToResponse).toList();
    }
    
    @Transactional(readOnly = true)
    public DocumentSubmission getDocumentById(Long docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with ID: " + docId));
    }

    @Transactional(readOnly = true)
    public byte[] downloadAllDocumentsAsZip(Long employeeId) {
        List<DocumentSubmission> docs = documentRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        
        if (docs.isEmpty()) {
            throw new EntityNotFoundException("No documents found for employee ID: " + employeeId);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (DocumentSubmission doc : docs) {
                ZipEntry entry = new ZipEntry(doc.getFilename());
                zos.putNextEntry(entry);
                if (doc.getDocumentContent() != null) {
                    zos.write(doc.getDocumentContent().getBytes());
                }
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate zip file for documents", e);
        }
    }

    private DocumentResponse mapToResponse(DocumentSubmission doc) {
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
    }
}