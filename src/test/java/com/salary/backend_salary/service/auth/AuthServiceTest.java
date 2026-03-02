package com.salary.backend_salary.service.auth;

import com.salary.backend_salary.dto.auth.AuthResponse;
import com.salary.backend_salary.dto.auth.LoginRequest;
import com.salary.backend_salary.dto.auth.RegisterRequest;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.enums.Role;
import com.salary.backend_salary.repository.user.UserRepository;
import com.salary.backend_salary.security.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("bagas", "password123");
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        
        when(request.getSession(true)).thenReturn(session);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn("bagas");
        
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(userDetails).getAuthorities();

        AuthResponse authResponse = authService.login(loginRequest, request, response);

        assertNotNull(authResponse);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(request).getSession(true);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest registerRequest = new RegisterRequest("karyawan_baru", "rahasia123", Role.ADMIN);

        when(userRepository.existsByUsername("karyawan_baru")).thenReturn(false);
        when(passwordEncoder.encode("rahasia123")).thenReturn("encoded_password");

        AppUser savedUser = new AppUser();
        savedUser.setUsername("karyawan_baru");
        savedUser.setPassword("encoded_password");
        savedUser.setRole(Role.ADMIN);

        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        AppUser result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("karyawan_baru", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        assertEquals(Role.ADMIN, result.getRole());
        
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("bagas", "rahasia", Role.ADMIN);

        when(userRepository.existsByUsername("bagas")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username sudah dipakai!", exception.getMessage());
        
        verify(userRepository, never()).save(any(AppUser.class));
    }
}