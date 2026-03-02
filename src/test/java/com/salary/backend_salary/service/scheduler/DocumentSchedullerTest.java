package com.salary.backend_salary.service.scheduler;

import com.salary.backend_salary.entity.document.DocumentSubmission;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.repository.document.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentSchedullerTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentScheduller documentScheduller;

    @Captor
    private ArgumentCaptor<List<DocumentSubmission>> captor;

    @Test
    void testGenerateAutoFinalDocuments_NoTargets() {
        when(documentRepository.findTargetsFromDocuments(any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        documentScheduller.generateAutoFinalDocuments();

        verify(documentRepository, never()).saveAll(anyList());
    }

    @Test
    void testGenerateAutoFinalDocuments_Success() {
        Employee emp = new Employee();
        emp.setId(1L);

        DocumentSubmission lastDoc = new DocumentSubmission();
        lastDoc.setFilename("slip_gaji_januari.pdf");
        lastDoc.setDocumentType("PAYSLIP");
        lastDoc.setCreatedAt(LocalDateTime.now());

        when(documentRepository.findTargetsFromDocuments(any(Pageable.class)))
                .thenReturn(List.of(emp));
        when(documentRepository.findTopByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(lastDoc));

        documentScheduller.generateAutoFinalDocuments();

        verify(documentRepository, times(1)).saveAll(captor.capture());

        List<DocumentSubmission> savedDocs = captor.getValue();
        assertEquals(1, savedDocs.size());

        DocumentSubmission finalDoc = savedDocs.get(0);
        
        assertEquals("slip_gaji_januari_FINAL.pdf", finalDoc.getFilename());
        assertEquals(emp, finalDoc.getEmployee());
        assertEquals("PAYSLIP", finalDoc.getDocumentType());
        assertNull(finalDoc.getCreatedBy());
    }

    @Test
    void testGenerateAutoFinalDocuments_FilenameLogic_AlreadyFinal() {
        Employee emp = new Employee();
        emp.setId(2L);

        DocumentSubmission lastDoc = new DocumentSubmission();
        lastDoc.setFilename("laporan_tahunan_FINAL.docx");

        when(documentRepository.findTargetsFromDocuments(any(Pageable.class)))
                .thenReturn(List.of(emp));
        when(documentRepository.findTopByEmployeeIdOrderByCreatedAtDesc(2L))
                .thenReturn(Optional.of(lastDoc));

        documentScheduller.generateAutoFinalDocuments();

        verify(documentRepository).saveAll(captor.capture());
        DocumentSubmission result = captor.getValue().get(0);

        assertEquals("laporan_tahunan_FINAL.docx", result.getFilename());
    }

    @Test
    void testGenerateAutoFinalDocuments_FilenameLogic_NoExtension() {
        Employee emp = new Employee();
        emp.setId(3L);

        DocumentSubmission lastDoc = new DocumentSubmission();
        lastDoc.setFilename("README");

        when(documentRepository.findTargetsFromDocuments(any(Pageable.class)))
                .thenReturn(List.of(emp));
        when(documentRepository.findTopByEmployeeIdOrderByCreatedAtDesc(3L))
                .thenReturn(Optional.of(lastDoc));

        documentScheduller.generateAutoFinalDocuments();

        verify(documentRepository).saveAll(captor.capture());
        DocumentSubmission result = captor.getValue().get(0);

        assertEquals("README_FINAL", result.getFilename());
    }

    @Test
    void testGenerateAutoFinalDocuments_SkipIfFilenameNull() {
        Employee emp = new Employee();
        emp.setId(4L);

        DocumentSubmission lastDoc = new DocumentSubmission();
        lastDoc.setFilename(null);

        when(documentRepository.findTargetsFromDocuments(any(Pageable.class)))
                .thenReturn(List.of(emp));
        when(documentRepository.findTopByEmployeeIdOrderByCreatedAtDesc(4L))
                .thenReturn(Optional.of(lastDoc));

        documentScheduller.generateAutoFinalDocuments();

        verify(documentRepository, never()).saveAll(anyList());
    }

    @Test
    void testGenerateAutoFinalDocuments_ExceptionResilience() {
        Employee empError = new Employee(); 
        empError.setId(10L);
        
        Employee empSuccess = new Employee(); 
        empSuccess.setId(20L);

        DocumentSubmission docSuccess = new DocumentSubmission();
        docSuccess.setFilename("sukses.pdf");

        when(documentRepository.findTargetsFromDocuments(any(Pageable.class)))
                .thenReturn(List.of(empError, empSuccess));

        when(documentRepository.findTopByEmployeeIdOrderByCreatedAtDesc(10L))
                .thenThrow(new RuntimeException("Database Connection Failed"));

        when(documentRepository.findTopByEmployeeIdOrderByCreatedAtDesc(20L))
                .thenReturn(Optional.of(docSuccess));

        documentScheduller.generateAutoFinalDocuments();

        verify(documentRepository).saveAll(captor.capture());
        
        List<DocumentSubmission> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals("sukses_FINAL.pdf", saved.get(0).getFilename());
    }
}