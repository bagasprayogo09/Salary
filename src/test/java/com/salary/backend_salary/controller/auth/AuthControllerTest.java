package com.salary.backend_salary.controller.auth;

import com.salary.backend_salary.dto.auth.AuthResponse;
import com.salary.backend_salary.dto.auth.LoginRequest;
import com.salary.backend_salary.dto.auth.RegisterRequest;
import com.salary.backend_salary.enums.Role;
import com.salary.backend_salary.security.service.UserDetailsImpl;
import com.salary.backend_salary.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest("bagas", "password123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication auth = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn("bagas");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(userDetails).getAuthorities();

        ResponseEntity<AuthResponse> res = authController.login(req, request, response);

        assertEquals(200, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertEquals("bagas", res.getBody().username());
        assertEquals(Role.ADMIN, res.getBody().role());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest("bagas", "password123", Role.ADMIN);
        
        ResponseEntity<String> res = authController.register(req);

        assertEquals(200, res.getStatusCode().value());
        assertEquals("User registered successfully", res.getBody());
        verify(authService).register(req);
    }

    @Test
    void testMe_Success() {
        Authentication auth = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(10L);
        when(userDetails.getUsername()).thenReturn("bagas");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(userDetails).getAuthorities();

        ResponseEntity<AuthResponse> res = authController.me(auth);

        assertEquals(200, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertEquals(Role.ADMIN, res.getBody().role());
        assertEquals(10L, res.getBody().id());
    }

    @Test
    void testMe_NotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        ResponseEntity<AuthResponse> res = authController.me(auth);

        assertEquals(401, res.getStatusCode().value());
    }

    @Test
    void testMe_NullAuthentication() {
        ResponseEntity<AuthResponse> res = authController.me(null);
        
        assertEquals(401, res.getStatusCode().value());
    }

    @Test
    void testBuildAuthResponse_Exception() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        
        when(auth.getPrincipal()).thenReturn("Tipe Data Invalid"); 

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authController.me(auth);
        });

        assertEquals("Principal is not instance of UserDetailsImpl", exception.getMessage());
    }
}