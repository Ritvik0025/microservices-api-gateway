package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Spring Boot Application for Microservices API Gateway
 * 
 * This is the entry point of our gateway application.
 * @EnableCaching: Enables caching functionality throughout the application
 */
@SpringBootApplication
@EnableCaching
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
