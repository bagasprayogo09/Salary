package com.salary.backend_salary.controller.document;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.salary.backend_salary.dto.document.DocumentRequest;
import com.salary.backend_salary.dto.document.DocumentResponse;
import com.salary.backend_salary.service.document.DocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @PathVariable Long employeeId, 
            @RequestBody DocumentRequest request) { 
        
        documentService.submitDocument(employeeId, request);
        
        return ResponseEntity.ok(Map.of("message", "Document saved successfully"));
    }

    @GetMapping("/list/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<List<DocumentResponse>> listDocuments(@PathVariable Long employeeId) {
        return ResponseEntity.ok(documentService.listDocuments(employeeId));
    }

    @GetMapping("/{docId}/download")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long docId) {        
        var doc = documentService.getDocumentById(docId); 

        var mediaType = "csv".equalsIgnoreCase(doc.getDocumentType()) 
                ? new MediaType("text", "csv") 
                : MediaType.TEXT_PLAIN;

        byte[] data = doc.getDocumentContent().getBytes(StandardCharsets.UTF_8);
        var resource = new ByteArrayResource(data);

        var contentDisposition = ContentDisposition.attachment()
                .filename(doc.getFilename()) 
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(mediaType)
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/{docId}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER')")
    public ResponseEntity<String> previewDocument(@PathVariable Long docId) {
        var doc = documentService.getDocumentById(docId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(doc.getDocumentContent());
    }

    @GetMapping("/download-all/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ResponseEntity<Resource> downloadAllZip(@PathVariable Long employeeId) {
        byte[] data = documentService.downloadAllDocumentsAsZip(employeeId);
        var resource = new ByteArrayResource(data);

        var contentDisposition = ContentDisposition.attachment()
                .filename("documents_emp_" + employeeId + ".zip")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
    }
}