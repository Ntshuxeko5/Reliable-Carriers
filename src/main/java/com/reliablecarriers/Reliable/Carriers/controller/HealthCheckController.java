package com.reliablecarriers.Reliable.Carriers.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reliablecarriers.Reliable.Carriers.repository.*;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive Health Check Controller
 * Provides detailed health information for monitoring and diagnostics
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Basic health check endpoint
     * GET /api/health
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with component status
     * GET /api/health/detailed
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        Map<String, Object> components = new HashMap<>();

        // Database health
        Map<String, Object> database = checkDatabase();
        components.put("database", database);

        // Application health
        Map<String, Object> application = new HashMap<>();
        application.put("status", "UP");
        application.put("version", "1.0.0");
        application.put("javaVersion", System.getProperty("java.version"));
        components.put("application", application);

        // Memory health
        Map<String, Object> memory = checkMemory();
        components.put("memory", memory);

        // Data statistics
        Map<String, Object> statistics = getStatistics();
        components.put("statistics", statistics);

        health.put("components", components);

        // Overall status
        boolean allUp = components.values().stream()
            .filter(c -> c instanceof Map)
            .map(c -> (Map<?, ?>) c)
            .allMatch(c -> "UP".equals(c.get("status")));

        health.put("status", allUp ? "UP" : "DEGRADED");

        return ResponseEntity.ok(health);
    }

    /**
     * Database connectivity check
     * GET /api/health/database
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        return ResponseEntity.ok(checkDatabase());
    }

    /**
     * Memory usage check
     * GET /api/health/memory
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> memoryHealth() {
        return ResponseEntity.ok(checkMemory());
    }

    /**
     * Statistics endpoint
     * GET /api/health/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        return ResponseEntity.ok(getStatistics());
    }

    /**
     * Check database connectivity
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> db = new HashMap<>();
        try {
            Connection connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            
            db.put("status", "UP");
            db.put("databaseProduct", metaData.getDatabaseProductName());
            db.put("databaseVersion", metaData.getDatabaseProductVersion());
            db.put("driverName", metaData.getDriverName());
            db.put("driverVersion", metaData.getDriverVersion());
            db.put("url", metaData.getURL());
            
            // Test query
            boolean isValid = connection.isValid(5);
            db.put("connectionValid", isValid);
            
            connection.close();
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            db.put("status", "DOWN");
            db.put("error", e.getMessage());
        }
        return db;
    }

    /**
     * Check memory usage
     */
    private Map<String, Object> checkMemory() {
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double usedPercent = (double) usedMemory / maxMemory * 100;
        
        memory.put("status", usedPercent > 90 ? "WARNING" : "UP");
        memory.put("totalMemoryMB", totalMemory / (1024 * 1024));
        memory.put("freeMemoryMB", freeMemory / (1024 * 1024));
        memory.put("usedMemoryMB", usedMemory / (1024 * 1024));
        memory.put("maxMemoryMB", maxMemory / (1024 * 1024));
        memory.put("usedPercent", Math.round(usedPercent * 100.0) / 100.0);
        
        return memory;
    }

    /**
     * Get application statistics
     */
    private Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("totalUsers", userRepository.count());
            stats.put("totalShipments", shipmentRepository.count());
            stats.put("totalBookings", bookingRepository.count());
            stats.put("status", "UP");
        } catch (Exception e) {
            logger.error("Failed to get statistics", e);
            stats.put("status", "ERROR");
            stats.put("error", e.getMessage());
        }
        return stats;
    }
}

