package com.salary.backend_salary.service.auth;

import org.springframework.stereotype.Service;

import com.salary.backend_salary.dto.auth.AuthResponse;
import com.salary.backend_salary.dto.auth.LoginRequest;
import com.salary.backend_salary.dto.auth.RegisterRequest;
import com.salary.backend_salary.entity.appusers.AppUser;
import com.salary.backend_salary.enums.Role;
import com.salary.backend_salary.repository.user.UserRepository;
import com.salary.backend_salary.security.service.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthResponse login(LoginRequest req, HttpServletRequest request, HttpServletResponse response) {

        var authenticationToken = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new IllegalStateException("Autentikasi gagal atau principal tidak valid");
        }

        if (userDetails.getAuthorities() == null || userDetails.getAuthorities().isEmpty()) {
            throw new IllegalStateException("User tidak memiliki akses/role");
        }

        String authority = userDetails.getAuthorities().iterator().next().getAuthority();
        if (authority == null) {
            throw new IllegalStateException("Role pada user bernilai null");
        }

        String roleStr = authority.replace("ROLE_", "");

        return new AuthResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                Role.valueOf(roleStr)
        );
    }

    public AppUser register(RegisterRequest req) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(req.username()))) {
            throw new IllegalArgumentException("Username sudah dipakai!");
        }

        var user = new AppUser(); 
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(req.role());

        return userRepository.save(user);
    }
}