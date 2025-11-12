package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database migration service to add coordinate columns to shipments table
 * Runs on application startup to ensure schema is up to date
 * 
 * This migration is safe to run multiple times - it checks if columns exist first
 */
@Component
public class ShipmentCoordinatesMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("=== Shipment Coordinates Migration ===");
            System.out.println("Checking shipments table for coordinate columns...");
            
            // Get database name dynamically
            String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            System.out.println("Database: " + dbName);
            
            // Check if columns exist
            boolean pickupLatExists = checkColumnExists("pickup_latitude");
            boolean pickupLngExists = checkColumnExists("pickup_longitude");
            boolean deliveryLatExists = checkColumnExists("delivery_latitude");
            boolean deliveryLngExists = checkColumnExists("delivery_longitude");
            
            if (!pickupLatExists || !pickupLngExists || !deliveryLatExists || !deliveryLngExists) {
                System.out.println("Adding coordinate columns to shipments table...");
                
                // Add pickup coordinates
                if (!pickupLatExists) {
                    System.out.println("  Adding pickup_latitude...");
                    jdbcTemplate.execute("ALTER TABLE shipments ADD COLUMN pickup_latitude DECIMAL(10, 8) NULL COMMENT 'Pickup location latitude from Google Maps geocoding'");
                } else {
                    System.out.println("  pickup_latitude already exists");
                }
                
                if (!pickupLngExists) {
                    System.out.println("  Adding pickup_longitude...");
                    jdbcTemplate.execute("ALTER TABLE shipments ADD COLUMN pickup_longitude DECIMAL(11, 8) NULL COMMENT 'Pickup location longitude from Google Maps geocoding'");
                } else {
                    System.out.println("  pickup_longitude already exists");
                }
                
                // Add delivery coordinates
                if (!deliveryLatExists) {
                    System.out.println("  Adding delivery_latitude...");
                    jdbcTemplate.execute("ALTER TABLE shipments ADD COLUMN delivery_latitude DECIMAL(10, 8) NULL COMMENT 'Delivery location latitude from Google Maps geocoding'");
                } else {
                    System.out.println("  delivery_latitude already exists");
                }
                
                if (!deliveryLngExists) {
                    System.out.println("  Adding delivery_longitude...");
                    jdbcTemplate.execute("ALTER TABLE shipments ADD COLUMN delivery_longitude DECIMAL(11, 8) NULL COMMENT 'Delivery location longitude from Google Maps geocoding'");
                } else {
                    System.out.println("  delivery_longitude already exists");
                }
                
                // Add indexes for performance (ignore if they already exist)
                try {
                    System.out.println("  Creating indexes...");
                    jdbcTemplate.execute("CREATE INDEX idx_shipments_pickup_coords ON shipments(pickup_latitude, pickup_longitude)");
                    System.out.println("    Index idx_shipments_pickup_coords created");
                } catch (Exception e) {
                    System.out.println("    Index idx_shipments_pickup_coords already exists or error: " + e.getMessage());
                }
                
                try {
                    jdbcTemplate.execute("CREATE INDEX idx_shipments_delivery_coords ON shipments(delivery_latitude, delivery_longitude)");
                    System.out.println("    Index idx_shipments_delivery_coords created");
                } catch (Exception e) {
                    System.out.println("    Index idx_shipments_delivery_coords already exists or error: " + e.getMessage());
                }
                
                System.out.println("✅ Shipment coordinate columns migration completed successfully!");
            } else {
                System.out.println("✅ All coordinate columns already exist. Migration skipped.");
            }
            
            System.out.println("=== Migration Complete ===\n");
        } catch (Exception e) {
            System.err.println("❌ Error updating shipments table schema: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\n⚠️  Migration failed. You can:");
            System.err.println("   1. Run the SQL script manually: add-shipment-coordinates-simple.sql");
            System.err.println("   2. Or let Hibernate's ddl-auto=update handle it (if enabled)");
            // Don't fail the application startup if migration fails
        }
    }
    
    private boolean checkColumnExists(String columnName) {
        try {
            String query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'shipments' AND COLUMN_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(query, Integer.class, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            System.err.println("Error checking column " + columnName + ": " + e.getMessage());
            return false;
        }
    }
}


