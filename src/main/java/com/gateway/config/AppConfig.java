package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring Boot Configuration Class
 * 
 * Java 8 / Spring Boot Features:
 * - @Configuration: Marks this class as a Spring configuration source
 * - @Bean: Defines Spring-managed beans that can be injected anywhere
 * - This class centralizes all bean definitions for the application
 * 
 * Spring Framework will automatically create instances of these beans
 * and make them available for @Autowired injection.
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate Bean
     * 
     * RestTemplate is used to make HTTP calls to backend microservices.
     * By defining it as a @Bean, Spring manages its lifecycle and 
     * we can inject it anywhere using @Autowired
     * 
     * @return RestTemplate instance configured for the application
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
