package com.salary.backend_salary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendSalaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendSalaryApplication.class, args);
	}

}
