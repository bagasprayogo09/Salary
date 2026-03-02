package com.salary.backend_salary.controller.document;

import com.salary.backend_salary.dto.document.DocumentRequest;
import com.salary.backend_salary.dto.document.DocumentResponse;
import com.salary.backend_salary.entity.document.DocumentSubmission;
import com.salary.backend_salary.service.document.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    void uploadDocument_Success() {
        Long employeeId = 1L;
        DocumentRequest request = new DocumentRequest(); 

        doNothing().when(documentService).submitDocument(employeeId, request);

        ResponseEntity<Map<String, String>> response = documentController.uploadDocument(employeeId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Document saved successfully", response.getBody().get("message"));
        verify(documentService).submitDocument(employeeId, request);
    }

    @Test
    void listDocuments_Success() {
        Long employeeId = 1L;
        when(documentService.listDocuments(employeeId)).thenReturn(Collections.singletonList(new DocumentResponse()));

        ResponseEntity<List<DocumentResponse>> response = documentController.listDocuments(employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void downloadDocument_Csv_Success() throws Exception {
        Long docId = 1L;
        String content = "id,name\n1,john";
        
        var mockSub = mock(DocumentSubmission.class);
        when(mockSub.getDocumentType()).thenReturn("csv");
        when(mockSub.getDocumentContent()).thenReturn(content);
        when(mockSub.getFilename()).thenReturn("report.csv");

        when(documentService.getDocumentById(docId)).thenReturn(mockSub);

        ResponseEntity<Resource> response = documentController.downloadDocument(docId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().contains("text/csv"));
        assertEquals(content, new String(response.getBody().getContentAsByteArray(), StandardCharsets.UTF_8));
    }

   @Test
    void downloadDocument_Txt_Success() throws Exception {
        Long docId = 2L;
        String content = "Hello World";

        DocumentSubmission mockSub = mock(DocumentSubmission.class);
        when(mockSub.getDocumentType()).thenReturn("txt");
        when(mockSub.getDocumentContent()).thenReturn(content);
        when(mockSub.getFilename()).thenReturn("note.txt");

        when(documentService.getDocumentById(docId)).thenReturn(mockSub);

        ResponseEntity<Resource> response = documentController.downloadDocument(docId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals(content, new String(response.getBody().getContentAsByteArray(), StandardCharsets.UTF_8));
    }


    @Test
    void previewDocument_Success() {
        Long docId = 1L;
        String content = "Preview Content";
        
        var mockSub = mock(DocumentSubmission.class);
        when(mockSub.getDocumentContent()).thenReturn(content);

        when(documentService.getDocumentById(docId)).thenReturn(mockSub);

        ResponseEntity<String> response = documentController.previewDocument(docId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(content, response.getBody());
    }

    @Test
    void downloadAllZip_Success() throws Exception {
        Long employeeId = 5L;
        byte[] zipData = new byte[]{1, 2, 3, 4, 5};

        when(documentService.downloadAllDocumentsAsZip(employeeId)).thenReturn(zipData);

        ResponseEntity<Resource> response = documentController.downloadAllZip(employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("documents_emp_5.zip"));
        assertArrayEquals(zipData, response.getBody().getContentAsByteArray());
    }
}