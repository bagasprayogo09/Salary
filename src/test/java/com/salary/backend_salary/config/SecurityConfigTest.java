package com.salary.backend_salary.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private AuthenticationConfiguration authConfig;

    @Mock
    private HttpSecurity httpSecurity;

    @Test
    void testPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void testAuthenticationManager_Success() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager result = securityConfig.authenticationManager(authConfig);

        assertNotNull(result);
        assertEquals(mockManager, result);
    }

    @Test
    void testAuthenticationManager_Exception() throws Exception {
        when(authConfig.getAuthenticationManager()).thenThrow(new RuntimeException("Config Error"));

        assertThrows(RuntimeException.class, () -> securityConfig.authenticationManager(authConfig));
    }

    @Test
    void testCorsConfigurationSource() {
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) securityConfig.corsConfigurationSource();

        Map<String, CorsConfiguration> configMap = source.getCorsConfigurations();
        CorsConfiguration config = configMap.get("/**");

        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("http://localhost:4200"));
        assertTrue(config.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "DELETE")));
        assertTrue(config.getAllowCredentials());
        assertEquals(List.of("*"), config.getAllowedHeaders());
    }

    @Test
    void testFilterChain_Success() throws Exception {
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.exceptionHandling(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);

        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);
        when(httpSecurity.build()).thenReturn(mockChain);

        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);

        assertNotNull(result);
        verify(httpSecurity).build();
    }

    @Test
    void testFilterChain_Exception() throws Exception {
        when(httpSecurity.csrf(any())).thenThrow(new RuntimeException("Security Error"));

        assertThrows(RuntimeException.class, () -> securityConfig.filterChain(httpSecurity));
    }
}