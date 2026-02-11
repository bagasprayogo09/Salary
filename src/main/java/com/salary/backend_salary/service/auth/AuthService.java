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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest req, HttpServletRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.username(),
                        req.password()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String roleStr = userDetails.getAuthorities()
                .iterator().next().getAuthority().replace("ROLE_", "");

        return new AuthResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                Role.valueOf(roleStr)
        );
    }

    public AppUser register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new RuntimeException("Username sudah dipakai!");
        }

        AppUser user = new AppUser();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(req.role());

        return userRepository.save(user);
    }
}
