package com.salary.backend_salary.dto.api;

import org.apache.poi.ss.formula.functions.T;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;

    public static <R> ApiResponse<R> success(R data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <R> ApiResponse<R> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
