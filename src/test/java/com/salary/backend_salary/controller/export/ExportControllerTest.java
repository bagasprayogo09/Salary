    package com.salary.backend_salary.controller.export;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.salary.backend_salary.entity.export.ExportJob;
    import com.salary.backend_salary.service.export.ExportService;
    import com.salary.backend_salary.vm.employee.EmployeeVM;
    import org.junit.jupiter.api.AfterEach;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.MediaType;
    import org.springframework.test.web.servlet.MockMvc;
    import org.springframework.test.web.servlet.setup.MockMvcBuilders;

    import java.io.File;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.Comparator;
    import java.util.UUID;
    import java.util.stream.Stream;

    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.ArgumentMatchers.eq;
    import static org.mockito.Mockito.*;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @ExtendWith(MockitoExtension.class)
    class ExportControllerTest {

        @Mock
        private ExportService exportService;

        @InjectMocks
        private ExportController exportController;

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        private static final String UPLOAD_DIR = "uploads";

        @BeforeEach
        void setUp() throws IOException {
            mockMvc = MockMvcBuilders.standaloneSetup(exportController).build();
            objectMapper = new ObjectMapper();

            Files.createDirectories(Paths.get(UPLOAD_DIR));
        }

        @AfterEach
        void tearDown() throws IOException {
            Path path = Paths.get(UPLOAD_DIR);
            if (Files.exists(path)) {
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
        }

        @Test
        void testStartExport_Success() throws Exception {
            EmployeeVM vm = new EmployeeVM();
            vm.setName("Bagas");

            ExportJob mockJob = new ExportJob();
            mockJob.setId(UUID.randomUUID().toString());
            mockJob.setStatus("PENDING");

            when(exportService.triggerExport()).thenReturn(mockJob);

            mockMvc.perform(post("/api/export/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(vm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(mockJob.getId()))
                    .andExpect(jsonPath("$.status").value("PENDING"));

            verify(exportService).triggerExport();
            verify(exportService).processExportAsync(eq(mockJob.getId()), any(EmployeeVM.class));
        }

        @Test
        void testCheckStatus_Found() throws Exception {
            String jobId = "job-123";
            ExportJob mockJob = new ExportJob();
            mockJob.setId(jobId);
            mockJob.setStatus("COMPLETED");

            when(exportService.getJob(jobId)).thenReturn(mockJob);

            mockMvc.perform(get("/api/export/status/{jobId}", jobId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(jobId))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        void testCheckStatus_NotFound() throws Exception {
            String jobId = "job-999";
            when(exportService.getJob(jobId)).thenReturn(null);

            mockMvc.perform(get("/api/export/status/{jobId}", jobId))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testDownloadFile_Success() throws Exception {
            String fileName = "test_export.xlsx";
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            Files.writeString(filePath, "DUMMY CONTENT EXCEL");

            mockMvc.perform(get("/api/export/download/{fileName}", fileName))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\""))
                    .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .andExpect(content().string("DUMMY CONTENT EXCEL"));
        }

        @Test
        void testDownloadFile_NotFound() throws Exception {
            String fileName = "ghost_file.xlsx";

            mockMvc.perform(get("/api/export/download/{fileName}", fileName))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testDownloadFile_PathTraversal_SecurityCheck() throws Exception {
            String maliciousFileName = "../pom.xml";

            mockMvc.perform(get("/api/export/download/" + maliciousFileName))
                    .andExpect(status().isNotFound());
        }
    }