package com.quantumleap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for QuantumLeap
 * 
 * QuantumLeap is a real-time collaborative whiteboard application
 * that enables multiple users to work together on digital canvases
 * with real-time synchronization and event sourcing.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableAsync
public class QuantumLeapApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantumLeapApplication.class, args);
    }
}
