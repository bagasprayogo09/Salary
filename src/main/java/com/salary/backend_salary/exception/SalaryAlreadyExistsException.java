package com.salary.backend_salary.exception;

public class SalaryAlreadyExistsException extends RuntimeException  {

   public SalaryAlreadyExistsException(String month) {
        super("Salary already exists for month: " + month);
    }
}
