package com.salary.backend_salary.service.document;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.salary.backend_salary.dto.document.DocumentRequest;
import com.salary.backend_salary.dto.document.DocumentResponse;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.entity.document.DocumentSubmission;
import com.salary.backend_salary.entity.employee.Employee;
import com.salary.backend_salary.repository.document.DocumentRepository;
import com.salary.backend_salary.repository.employee.EmployeeRepository;
import com.salary.backend_salary.repository.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentService documentService;

    private Employee employee;
    private AppUser user;

    @BeforeEach
    void setup() {
        employee = new Employee();
        employee.setId(1L);

        user = new AppUser();
        user.setUsername("bagas");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("bagas", null)
        );
    }

    @Test
    void submitDocument_success() {
        DocumentRequest request = new DocumentRequest();
        request.setFilename("test.pdf");
        request.setType("PDF");
        request.setContent("CONTENT");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findByUsername("bagas")).thenReturn(Optional.of(user));

        documentService.submitDocument(1L, request);

        verify(documentRepository, times(1)).save(any(DocumentSubmission.class));
    }

    @Test
    void submitDocument_unauthenticated_shouldThrow() {
        SecurityContextHolder.clearContext();

        DocumentRequest request = new DocumentRequest();

        assertThatThrownBy(() ->
                documentService.submitDocument(1L, request)
        ).isInstanceOf(SecurityException.class);
    }

    @Test
    void submitDocument_employeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        DocumentRequest request = new DocumentRequest();

        assertThatThrownBy(() ->
                documentService.submitDocument(1L, request)
        ).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void submitDocument_userNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findByUsername("bagas")).thenReturn(Optional.empty());

        DocumentRequest request = new DocumentRequest();

        assertThatThrownBy(() ->
                documentService.submitDocument(1L, request)
        ).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void listDocuments_shouldMapCorrectly() {
        DocumentSubmission doc = new DocumentSubmission();
        doc.setId(10L);
        doc.setFilename("file.txt");
        doc.setDocumentType("TXT");
        doc.setDocumentContent("ABC");
        doc.setCreatedBy(user);

        when(documentRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(doc));

        List<DocumentResponse> responses = documentService.listDocuments(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFilename()).isEqualTo("file.txt");
        assertThat(responses.get(0).getCreatedBy()).isEqualTo("bagas");
    }

    @Test
    void listDocuments_createdByNull_shouldReturnSystem() {
        DocumentSubmission doc = new DocumentSubmission();
        doc.setId(10L);
        doc.setFilename("file.txt");

        when(documentRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(doc));

        List<DocumentResponse> responses = documentService.listDocuments(1L);

        assertThat(responses.get(0).getCreatedBy()).isEqualTo("SYSTEM");
    }

    @Test
    void getDocumentById_success() {
        DocumentSubmission doc = new DocumentSubmission();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        DocumentSubmission result = documentService.getDocumentById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getDocumentById_notFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                documentService.getDocumentById(1L)
        ).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void downloadAllDocumentsAsZip_success() {
        DocumentSubmission doc = new DocumentSubmission();
        doc.setFilename("file.txt");
        doc.setDocumentContent("HELLO");

        when(documentRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(doc));

        byte[] zipBytes = documentService.downloadAllDocumentsAsZip(1L);

        assertThat(zipBytes).isNotEmpty();
    }

    @Test
    void downloadAllDocumentsAsZip_emptyList_shouldThrow() {
        when(documentRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                documentService.downloadAllDocumentsAsZip(1L)
        ).isInstanceOf(EntityNotFoundException.class);
    }
}