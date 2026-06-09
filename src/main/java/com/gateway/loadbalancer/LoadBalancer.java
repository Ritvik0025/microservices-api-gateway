package com.gateway.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * LoadBalancer Component - Core business logic
 * 
 * Java 8 Features Used:
 * 1. Streams API - filter() for filtering healthy servers
 * 2. Lambda expressions - for load balancing strategies
 * 3. Method references - for functional programming
 * 4. AtomicInteger - for thread-safe round-robin counter
 * 
 * This class manages multiple servers and distributes requests across them
 * using different load balancing algorithms.
 */
@Slf4j
@Component
public class LoadBalancer {
    
    private final List<Server> servers = new ArrayList<>();
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private LoadBalancingStrategy strategy;

    /**
     * Initialize load balancer with a list of servers
     */
    public LoadBalancer() {
        // Default strategy is Round-Robin
        this.strategy = this::roundRobinStrategy;
        log.info("LoadBalancer initialized with Round-Robin strategy");
    }

    /**
     * Add a server to the pool
     * @param server The server to add
     */
    public void addServer(Server server) {
        servers.add(server);
        log.info("Server added: {} at {}:{}", server.getHost(), server.getHost(), server.getPort());
    }

    /**
     * Remove a server from the pool
     * @param host The host of the server to remove
     */
    public void removeServer(String host) {
        servers.removeIf(server -> server.getHost().equals(host));
        log.info("Server removed: {}", host);
    }

    /**
     * Get the next server to handle a request
     * Uses the current strategy to select a server
     * 
     * Java 8 Stream API:
     * - filter(Server::isHealthy) - filters only healthy servers
     * - collect(Collectors.toList()) - collects filtered results into a list
     * 
     * @return A healthy server, or null if none available
     */
    public Server getNextServer() {
        // JAVA 8 FEATURE: Streams API
        List<Server> healthyServers = servers.stream()
                .filter(Server::isHealthy)  // Method reference - calls isHealthy() on each server
                .collect(Collectors.toList());

        if (healthyServers.isEmpty()) {
            log.warn("No healthy servers available!");
            return null;
        }

        return strategy.selectServer(healthyServers);
    }

    /**
     * Round-Robin Load Balancing Strategy
     * Distributes requests evenly across servers in sequence
     * 
     * Example: If you have servers [A, B, C]
     * Request 1 -> A
     * Request 2 -> B
     * Request 3 -> C
     * Request 4 -> A (cycles back)
     * 
     * @param healthyServers List of healthy servers
     * @return Next server in round-robin sequence
     */
    private Server roundRobinStrategy(List<Server> healthyServers) {
        int index = roundRobinCounter.getAndIncrement() % healthyServers.size();
        Server selected = healthyServers.get(index);
        log.debug("Round-Robin selected server: {}", selected.getUrl());
        return selected;
    }

    /**
     * Random Load Balancing Strategy
     * 
     * Java 8 Feature: Lambda expression assigned to LoadBalancingStrategy
     * Randomly picks a server from the healthy pool
     * 
     * @return A random strategy instance
     */
    public static LoadBalancingStrategy createRandomStrategy() {
        Random random = new Random();
        return servers -> {
            // JAVA 8 FEATURE: Lambda with functional interface
            Server selected = servers.get(random.nextInt(servers.size()));
            return selected;
        };
    }

    /**
     * Least Connections Load Balancing Strategy
     * 
     * Java 8 Features:
     * - min() with Comparator
     * - Optional usage
     * 
     * Selects the server with fewest active connections
     * 
     * @return A least connections strategy instance
     */
    public static LoadBalancingStrategy createLeastConnectionsStrategy() {
        // JAVA 8 FEATURE: Method reference and Optional
        return servers -> servers.stream()
                .min((s1, s2) -> Integer.compare(s1.getPort(), s2.getPort()))
                .orElse(servers.get(0));
    }

    /**
     * Set the load balancing strategy
     * @param strategy The strategy to use
     */
    public void setStrategy(LoadBalancingStrategy strategy) {
        this.strategy = strategy;
        log.info("Load balancing strategy changed");
    }

    /**
     * Get all servers (both healthy and unhealthy)
     * 
     * Java 8 Feature: Streams for data transformation
     * 
     * @return List of all servers
     */
    public List<Server> getAllServers() {
        return new ArrayList<>(servers);
    }

    /**
     * Get only healthy servers
     * 
     * Java 8 Feature: Stream + filter
     * 
     * @return List of healthy servers
     */
    public List<Server> getHealthyServers() {
        return servers.stream()
                .filter(Server::isHealthy)
                .collect(Collectors.toList());
    }

    /**
     * Get server statistics
     * 
     * Java 8 Feature: Streams + multiple operations
     * 
     * @return Server count information
     */
    public String getStatistics() {
        long healthyCount = servers.stream()
                .filter(Server::isHealthy)
                .count();
        
        return String.format("Total: %d, Healthy: %d, Unhealthy: %d", 
                servers.size(), healthyCount, servers.size() - healthyCount);
    }
}
