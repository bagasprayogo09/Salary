package com.salary.backend_salary.controller.employee;

import com.salary.backend_salary.dpo.employee.EmployeeDPO;
import com.salary.backend_salary.security.service.UserDetailsImpl;
import com.salary.backend_salary.service.employee.EmployeeService;
import com.salary.backend_salary.vm.employee.EmployeeRequestVM;
import com.salary.backend_salary.vm.employee.EmployeeVM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    // --- TEST CREATE ---

    @Test
    void createEmployee_Success() {
        // Arrange
        EmployeeRequestVM vm = new EmployeeRequestVM();
        vm.setName("Bagas");

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(100L);

        EmployeeDPO createdDpo = new EmployeeDPO();
        createdDpo.setId(1L);
        createdDpo.setName("Bagas");

        when(employeeService.createEmployee(vm)).thenReturn(createdDpo);

        
        ResponseEntity<EmployeeDPO> response = employeeController.createEmployee(vm, userDetails);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        
        assertEquals(100L, vm.getSubmitById());
        verify(employeeService).createEmployee(vm);
    }

    @Test
    void createEmployee_Unauthorized_WhenUserNull() {
        EmployeeRequestVM vm = new EmployeeRequestVM();

       
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            employeeController.createEmployee(vm, null);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }


    @Test
    void getEmployeeById_Success() {
        // Arrange
        Long id = 1L;
        EmployeeDPO dpo = new EmployeeDPO();
        dpo.setId(id);
        
        when(employeeService.getEmployeeById(id)).thenReturn(dpo);

        // Act
        ResponseEntity<EmployeeDPO> response = employeeController.getEmployeeById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    // --- TEST UPDATE ---

    @Test
    void updateEmployee_Success() {
        // Arrange
        Long id = 1L;
        EmployeeRequestVM vm = new EmployeeRequestVM();
        vm.setName("Bagas Update");

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(100L);

        EmployeeDPO updatedDpo = new EmployeeDPO();
        updatedDpo.setId(id);
        updatedDpo.setName("Bagas Update");

        when(employeeService.updateEmployee(eq(id), any(EmployeeRequestVM.class))).thenReturn(updatedDpo);

        // Act
        ResponseEntity<EmployeeDPO> response = employeeController.updateEmployee(id, vm, userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bagas Update", response.getBody().getName());
        
        // Verifikasi ID submitter diset
        assertEquals(100L, vm.getSubmitById());
    }

    @Test
    void updateEmployee_Unauthorized_WhenUserNull() {
        // Arrange
        Long id = 1L;
        EmployeeRequestVM vm = new EmployeeRequestVM();

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            employeeController.updateEmployee(id, vm, null);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    // --- TEST DELETE ---

    @Test
    void deleteEmployee_Success() {
        // Arrange
        Long id = 1L;
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(100L);

        doNothing().when(employeeService).deleteEmployee(id, 100L);

        // Act
        ResponseEntity<Void> response = employeeController.deleteEmployee(id, userDetails);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(employeeService).deleteEmployee(id, 100L);
    }

    @Test
    void deleteEmployee_Unauthorized_WhenUserNull() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            employeeController.deleteEmployee(1L, null);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    // --- TEST SEARCH ---

    @Test
    void searchEmployees_Success() {
        // Arrange
        EmployeeVM searchVM = new EmployeeVM();
        Pageable pageable = PageRequest.of(0, 10);
        
        EmployeeDPO dpo = new EmployeeDPO();
        Page<EmployeeDPO> pageResult = new PageImpl<>(List.of(dpo));

        when(employeeService.searchEmployees(searchVM, pageable)).thenReturn(pageResult);

        // Act
        ResponseEntity<Page<EmployeeDPO>> response = employeeController.searchEmployees(searchVM, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        
        verify(employeeService).searchEmployees(searchVM, pageable);
    }
}