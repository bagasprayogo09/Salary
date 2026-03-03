package com.salary.backend_salary.controller.dashboard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.salary.backend_salary.dto.dashboard.DashboardEmployeeDTO;
import com.salary.backend_salary.enums.ApprovalStatus;
import com.salary.backend_salary.service.dashboard.DashboardService;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(DashboardController.class)
@Import({
    DashboardControllerTest.TestExceptionHandler.class,
    DashboardControllerTest.MethodSecurityTestConfig.class
})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @BeforeEach
    void setUpDefaultStub() {
        when(dashboardService.getEmployees(any(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        
        @ExceptionHandler(ConstraintViolationException.class)
        ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        @ExceptionHandler(AuthorizationDeniedException.class)
        ResponseEntity<String> handleAuthorizationDenied(AuthorizationDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRecentEmployees_success() throws Exception {
        DashboardEmployeeDTO dto = new DashboardEmployeeDTO();
        when(dashboardService.getEmployees(ApprovalStatus.APPROVED, 5))
                .thenReturn(CompletableFuture.completedFuture(List.of(dto)));

        mockMvc.perform(get("/api/dashboard/recent-employees")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRecentEmployees_defaultLimit_shouldUse10() throws Exception {
        when(dashboardService.getEmployees(ApprovalStatus.APPROVED, 10))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        mockMvc.perform(get("/api/dashboard/recent-employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRecentEmployees_limitTooSmall_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/dashboard/recent-employees")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRecentEmployees_limitTooLarge_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/dashboard/recent-employees")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRecentEmployees_forbiddenRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/dashboard/recent-employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRecentEmployees_serviceThrows_shouldReturn500() throws Exception {
        when(dashboardService.getEmployees(any(), anyInt()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("error")));

        MvcResult asyncResult = mockMvc.perform(get("/api/dashboard/recent-employees"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isInternalServerError());
    }
}