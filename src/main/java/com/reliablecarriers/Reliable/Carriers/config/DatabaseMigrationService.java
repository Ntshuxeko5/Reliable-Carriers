package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationService implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if the payments table exists and has the old schema
            // Only run on MySQL - PostgreSQL uses different syntax
            String dbUrl = System.getenv("DB_URL");
            if (dbUrl != null && dbUrl.contains("postgresql")) {
                System.out.println("PostgreSQL detected - skipping MySQL-specific migration");
                return;
            }
            
            String checkColumnQuery = "SELECT COLUMN_NAME, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'shipment_id'";
            
            var result = jdbcTemplate.queryForList(checkColumnQuery);
            
            if (!result.isEmpty()) {
                String isNullable = (String) result.get(0).get("IS_NULLABLE");
                if ("NO".equals(isNullable)) {
                    System.out.println("Updating payments table schema to allow nullable shipment_id and user_id...");
                    
                    // Make shipment_id nullable
                    jdbcTemplate.execute("ALTER TABLE payments MODIFY COLUMN shipment_id BIGINT NULL");
                    
                    // Make user_id nullable (if it exists)
                    try {
                        jdbcTemplate.execute("ALTER TABLE payments MODIFY COLUMN user_id BIGINT NULL");
                    } catch (Exception e) {
                        System.out.println("user_id column might not exist or already nullable: " + e.getMessage());
                    }
                    
                    System.out.println("Database schema updated successfully!");
                } else {
                    System.out.println("Payments table already has nullable shipment_id and user_id columns.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating database schema: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the application startup if migration fails
        }
    }
}
