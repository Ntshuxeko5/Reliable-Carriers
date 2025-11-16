package com.reliablecarriers.Reliable.Carriers.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine
 * Provides in-memory caching for frequently accessed data
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "quotes",           // Quote lookups
            "users",            // User data
            "shipments",        // Shipment data
            "tracking",         // Tracking information
            "drivers",          // Driver information
            "analytics",        // Analytics data
            "geocoding"         // Geocoding results (NEW)
        );
        
        Caffeine<Object, Object> caffeine = Objects.requireNonNull(Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)      // Expire after 1 hour
            .expireAfterAccess(30, TimeUnit.MINUTES)  // Expire if not accessed for 30 minutes
            .maximumSize(1000)                         // Maximum 1000 entries per cache
            .recordStats());                           // Enable cache statistics
        
        cacheManager.setCaffeine(caffeine);
        
        return cacheManager;
    }
}
