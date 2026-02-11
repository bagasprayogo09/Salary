package com.salary.backend_salary.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salary.backend_salary.entity.appusers.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
    Boolean existsByUsername(String username);
    
} 
