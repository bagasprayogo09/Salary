package com.salary.backend_salary.exception;

public class ResourceNotFoundException  extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }


    public static ResourceNotFoundException salary(Long id) {
        return new ResourceNotFoundException("Salary not found with id: " + id);
    }

     public static ResourceNotFoundException employee(Long id) {
        return new ResourceNotFoundException("Employee not found with id: " + id);
    }
}
