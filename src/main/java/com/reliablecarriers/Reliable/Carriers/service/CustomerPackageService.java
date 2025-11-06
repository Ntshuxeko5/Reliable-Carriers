package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageResponse;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
import com.reliablecarriers.Reliable.Carriers.model.Quote;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;

import java.util.List;
import java.math.BigDecimal;

public interface CustomerPackageService {
    
    /**
     * Create a quote for a package without requiring customer account
     */
    QuoteResponse createQuote(CustomerPackageRequest request);
    
    /**
     * Create a shipment from a quote
     */
    Shipment createShipmentFromQuote(String quoteId, CustomerPackageRequest request);
    
    /**
     * Get package details by tracking number (no account required)
     */
    CustomerPackageResponse getPackageByTrackingNumber(String trackingNumber);
    
    /**
     * Get all packages for a customer by email (for customers without accounts)
     */
    List<CustomerPackageResponse> getPackagesByEmail(String email);
    
    /**
     * Get all packages for a customer by phone (for customers without accounts)
     */
    List<CustomerPackageResponse> getPackagesByPhone(String phone);
    
    /**
     * Get packages by status for a customer
     */
    List<CustomerPackageResponse> getPackagesByStatus(String email, String status);
    
    /**
     * Get delivered packages for a customer
     */
    List<CustomerPackageResponse> getDeliveredPackages(String email);
    
    /**
     * Get current (in-transit) packages for a customer
     */
    List<CustomerPackageResponse> getCurrentPackages(String email);
    
    /**
     * Get pending packages for a customer
     */
    List<CustomerPackageResponse> getPendingPackages(String email);
    
    /**
     * Create multiple packages for different recipients (store functionality)
     */
    List<Shipment> createMultiplePackages(List<CustomerPackageRequest> requests, String businessName);
    
    /**
     * Update package tracking preferences
     */
    void updateTrackingPreferences(String trackingNumber, boolean emailNotifications, boolean smsNotifications);
    
    /**
     * Cancel a package (if still pending)
     */
    boolean cancelPackage(String trackingNumber, String email);
    
    /**
     * Request package pickup
     */
    void requestPickup(String trackingNumber, String email, String preferredDate, String notes);
    
    /**
     * Get package history for a customer
     */
    List<CustomerPackageResponse> getPackageHistory(String email, int limit);
    
    /**
     * Search packages by various criteria
     */
    List<CustomerPackageResponse> searchPackages(String email, String searchTerm);
    
    /**
     * Get package statistics for a customer
     */
    PackageStatistics getPackageStatistics(String email);
    
    /**
     * Validate tracking number format
     */
    boolean isValidTrackingNumber(String trackingNumber);
    
    /**
     * Get estimated delivery date for a package
     */
    String getEstimatedDeliveryDate(String trackingNumber);
    
    /**
     * Check if package is eligible for pickup
     */
    boolean isEligibleForPickup(String trackingNumber);
    
    /**
     * Get package insurance options
     */
    List<InsuranceOption> getInsuranceOptions(String trackingNumber);
    
    /**
     * Add insurance to package
     */
    boolean addInsurance(String trackingNumber, String insuranceType, BigDecimal amount);
    
    public static class PackageStatistics {
        private int totalPackages;
        private int deliveredPackages;
        private int inTransitPackages;
        private int pendingPackages;
        private int cancelledPackages;
        private BigDecimal totalSpent;
        private String averageDeliveryTime;
        
        // Getters and Setters
        public int getTotalPackages() {
            return totalPackages;
        }

        public void setTotalPackages(int totalPackages) {
            this.totalPackages = totalPackages;
        }

        public int getDeliveredPackages() {
            return deliveredPackages;
        }

        public void setDeliveredPackages(int deliveredPackages) {
            this.deliveredPackages = deliveredPackages;
        }

        public int getInTransitPackages() {
            return inTransitPackages;
        }

        public void setInTransitPackages(int inTransitPackages) {
            this.inTransitPackages = inTransitPackages;
        }

        public int getPendingPackages() {
            return pendingPackages;
        }

        public void setPendingPackages(int pendingPackages) {
            this.pendingPackages = pendingPackages;
        }

        public int getCancelledPackages() {
            return cancelledPackages;
        }

        public void setCancelledPackages(int cancelledPackages) {
            this.cancelledPackages = cancelledPackages;
        }

        public BigDecimal getTotalSpent() {
            return totalSpent;
        }

        public void setTotalSpent(BigDecimal totalSpent) {
            this.totalSpent = totalSpent;
        }

        public String getAverageDeliveryTime() {
            return averageDeliveryTime;
        }

        public void setAverageDeliveryTime(String averageDeliveryTime) {
            this.averageDeliveryTime = averageDeliveryTime;
        }
    }
    
    public static class InsuranceOption {
        private String type;
        private String description;
        private BigDecimal cost;
        private BigDecimal coverageAmount;
        
        public InsuranceOption(String type, String description, BigDecimal cost, BigDecimal coverageAmount) {
            this.type = type;
            this.description = description;
            this.cost = cost;
            this.coverageAmount = coverageAmount;
        }
        
        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public BigDecimal getCoverageAmount() {
            return coverageAmount;
        }

        public void setCoverageAmount(BigDecimal coverageAmount) {
            this.coverageAmount = coverageAmount;
        }
    }
    
    /**
     * Get saved quotes for a customer by email
     */
    List<Quote> getSavedQuotes(String email);
}
