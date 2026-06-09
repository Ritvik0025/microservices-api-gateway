package com.gateway.loadbalancer;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * Model class representing a backend server in the load balancer
 * 
 * Java 8 Feature: Using Lombok @Data annotation to auto-generate:
 * - Getters and setters
 * - toString(), equals(), hashCode() methods
 * 
 * This reduces boilerplate code significantly!
 */
@Data
@AllArgsConstructor
public class Server {
    private String host;
    private int port;
    private boolean healthy;
    
    public String getUrl() {
        return "http://" + host + ":" + port;
    }
}
