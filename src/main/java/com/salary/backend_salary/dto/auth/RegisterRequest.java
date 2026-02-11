package com.salary.backend_salary.dto.auth;

import com.salary.backend_salary.enums.Role;

public record RegisterRequest(String username,
    String password,
    Role role) {
    
}
