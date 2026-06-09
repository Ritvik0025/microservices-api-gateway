package com.gateway.loadbalancer;

import java.util.List;

/**
 * Functional Interface for Load Balancing Strategies
 * 
 * Java 8 Feature: @FunctionalInterface
 * - Only one abstract method allowed
 * - Can be implemented using Lambda expressions
 * - Enables functional programming approach
 * 
 * Example usage:
 * LoadBalancingStrategy roundRobin = servers -> servers.get(0);
 * LoadBalancingStrategy random = servers -> servers.get(new Random().nextInt(servers.size()));
 */
@FunctionalInterface
public interface LoadBalancingStrategy {
    /**
     * Select a server from the available pool
     * @param servers List of healthy servers
     * @return Selected server
     */
    Server selectServer(List<Server> servers);
}
