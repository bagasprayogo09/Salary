package com.salary.backend_salary.dto.auth;

import com.salary.backend_salary.enums.Role;
import com.salary.backend_salary.security.service.UserDetailsImpl;
import org.springframework.security.core.Authentication;

public record AuthResponse(Long id, String username, Role role) {

   public static AuthResponse from(Authentication authentication) {
    if (!(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
        throw new IllegalStateException("Principal is not instance of UserDetailsImpl");
    }

    Role role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(a -> Role.valueOf(a.getAuthority().replace("ROLE_", "")))
            .orElseThrow(() -> new IllegalStateException("User has no role assigned"));

    return new AuthResponse(userDetails.getId(), userDetails.getUsername(), role);
    }
}