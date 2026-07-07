package com.financepro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Finance Pro - Personal Finance Dashboard Application
 * Main entry point for the Spring Boot application.
 */
@SpringBootApplication
public class FinanceProApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceProApplication.class, args);
        System.out.println("\n=========================================");
        System.out.println("  Finance Pro Application Started!");
        System.out.println("  URL: http://localhost:8080");
        System.out.println("  Login: admin / admin123");
        System.out.println("=========================================\n");
    }
}
