package com.example.sasa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * SASA Test Application
 *
 * This is a test Spring Boot application to demonstrate SASA's API spec extraction capabilities.
 * It includes the MainController with 25 test endpoints and automatically generates API spec on startup.
 */
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    public CommandLineRunner generateApiSpec(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("SASA Test Application Started");
            System.out.println("Generating API Specification...");
            System.out.println("========================================\n");

            // Generate API spec using SASA
            SasaApplication.generateApiSpec(mapping, applicationContext);

            System.out.println("\n========================================");
            System.out.println("API Spec Generation Completed!");
            System.out.println("Output: build/api-spec.json");
            System.out.println("Output: build/api-spec.html");
            System.out.println("========================================\n");

            // Exit after generating spec
            System.exit(0);
        };
    }
}
