package com.salary.backend_salary.dto.auth;

import com.salary.backend_salary.enums.Role;

public record AuthResponse(Long id,
    String username, Role role) {
    
}
