package com.salary.backend_salary.controller.document;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salary.backend_salary.dto.document.DocumentRequest;
import com.salary.backend_salary.dto.document.DocumentResponse;
import com.salary.backend_salary.entity.document.DocumentSubmission;
import com.salary.backend_salary.service.document.DocumentService;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadDocument(@PathVariable Long employeeId, @RequestBody DocumentRequest request) {
        documentService.submitDocument(employeeId, request);
        return ResponseEntity.ok("Document saved succesfully");
    }

    @GetMapping("/list/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<List<DocumentResponse>> listDocuments(@PathVariable Long employeeId) {
        return ResponseEntity.ok(documentService.listDocuments(employeeId));
    }
    @GetMapping("/{docId}/download")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long docId) {        
        DocumentSubmission doc = documentService.getDocumentById(docId);

        MediaType mediaType = MediaType.TEXT_PLAIN;
        if ("csv".equalsIgnoreCase(doc.getDocumentType())) {
            mediaType = new MediaType("text","csv");
        }

        byte[] data = doc.getDocumentContent().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFilename() + "\"")
                .contentType(mediaType)
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/{docId}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER')")
    public ResponseEntity<String> previewDocument(@PathVariable Long docId) {
        DocumentSubmission doc = documentService.getDocumentById(docId);
        return ResponseEntity.ok(doc.getDocumentContent());
    }

    @GetMapping("/download-all/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<Resource> downloadAllZip(@PathVariable Long employeeId) {
        byte[] data = documentService.downloadAllDocumentsAsZip(employeeId);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
    }
}
        
