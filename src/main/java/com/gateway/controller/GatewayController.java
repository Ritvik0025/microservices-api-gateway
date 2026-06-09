package com.gateway.controller;

import com.gateway.loadbalancer.LoadBalancer;
import com.gateway.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Controller - REST API endpoint for the gateway
 * 
 * This controller handles incoming requests and routes them to appropriate backend services
 * using the LoadBalancer component.
 * 
 * Java 8 Features:
 * - @RestController: Simplified REST endpoint definition
 * - Lambda expressions in stream operations
 * - Optional and null-safe operations
 */
@Slf4j
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @Autowired
    private LoadBalancer loadBalancer;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Initialize gateway with some backend servers
     * This is called when the application starts
     */
    @PostConstruct
    public void initializeServers() {
        // Add sample backend servers
        loadBalancer.addServer(new Server("localhost", 8081, true));
        loadBalancer.addServer(new Server("localhost", 8082, true));
        loadBalancer.addServer(new Server("localhost", 8083, true));
        log.info("Gateway initialized with 3 backend servers");
    }

    /**
     * Route a request to the next available server
     * 
     * Java 8 Feature: Optional handling
     * 
     * @param path The API path to route
     * @return Response from the backend server
     */
    @PostMapping("/route")
    public ResponseEntity<?> routeRequest(@RequestBody Map<String, Object> request) {
        log.info("Received routing request: {}", request);

        // JAVA 8 FEATURE: Optional handling
        Server server = loadBalancer.getNextServer();
        
        if (server == null) {
            log.error("No healthy servers available");
            return ResponseEntity.serviceUnavailable()
                    .body(Map.of("error", "No healthy servers available"));
        }

        log.info("Routing to server: {}", server.getUrl());
        
        try {
            // In real scenario, this would forward the actual HTTP request
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("routed_to", server.getUrl());
            response.put("request", request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error routing request", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error routing request: " + e.getMessage()));
        }
    }

    /**
     * Get gateway statistics
     * 
     * @return Current state of all servers
     */
    @GetMapping("/health")
    public ResponseEntity<?> getHealth() {
        log.debug("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("statistics", loadBalancer.getStatistics());
        health.put("servers", loadBalancer.getAllServers().stream()
                .map(s -> Map.of(
                        "host", s.getHost(),
                        "port", s.getPort(),
                        "healthy", s.isHealthy(),
                        "url", s.getUrl()
                ))
                .toList());
        
        return ResponseEntity.ok(health);
    }

    /**
     * Mark a server as healthy or unhealthy
     * 
     * @param host The host of the server
     * @param healthy The health status
     */
    @PutMapping("/servers/{host}/health")
    public ResponseEntity<?> updateServerHealth(@PathVariable String host, @RequestParam boolean healthy) {
        log.info("Updating server {} health status to: {}", host, healthy);
        
        loadBalancer.getAllServers().stream()
                .filter(s -> s.getHost().equals(host))
                .forEach(s -> s.setHealthy(healthy));
        
        return ResponseEntity.ok(Map.of(
                "message", "Server health updated",
                "host", host,
                "healthy", healthy
        ));
    }

    /**
     * Add a new backend server to the gateway
     * 
     * @param host The server host
     * @param port The server port
     */
    @PostMapping("/servers")
    public ResponseEntity<?> addServer(@RequestParam String host, @RequestParam int port) {
        log.info("Adding new server: {}:{}", host, port);
        
        Server newServer = new Server(host, port, true);
        loadBalancer.addServer(newServer);
        
        return ResponseEntity.created(null).body(Map.of(
                "message", "Server added",
                "server", Map.of(
                        "host", host,
                        "port", port,
                        "healthy", true,
                        "url", newServer.getUrl()
                )
        ));
    }

    /**
     * Get all registered servers
     */
    @GetMapping("/servers")
    public ResponseEntity<?> getServers() {
        log.debug("Fetching all servers");
        
        return ResponseEntity.ok(Map.of(
                "servers", loadBalancer.getAllServers().stream()
                        .map(s -> Map.of(
                                "host", s.getHost(),
                                "port", s.getPort(),
                                "healthy", s.isHealthy(),
                                "url", s.getUrl()
                        ))
                        .toList(),
                "total", loadBalancer.getAllServers().size(),
                "healthy", loadBalancer.getHealthyServers().size()
        ));
    }

    /**
     * Get gateway statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics() {
        log.debug("Statistics requested");
        
        return ResponseEntity.ok(Map.of(
                "statistics", loadBalancer.getStatistics(),
                "healthy_servers", loadBalancer.getHealthyServers().size(),
                "total_servers", loadBalancer.getAllServers().size()
        ));
    }
}
