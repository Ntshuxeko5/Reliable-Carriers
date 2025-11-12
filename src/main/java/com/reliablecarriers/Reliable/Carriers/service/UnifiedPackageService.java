package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO;
import com.reliablecarriers.Reliable.Carriers.model.BookingStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;

import java.util.List;
import java.util.Map;

/**
 * Unified Package Service
 * Provides seamless package viewing for Customers, Drivers, and Admins
 * Merges data from both Booking and Shipment entities
 */
public interface UnifiedPackageService {
    
    /**
     * Get all packages (for admin view)
     */
    List<UnifiedPackageDTO> getAllPackages();
    
    /**
     * Get packages by tracking number (works for all user types)
     */
    UnifiedPackageDTO getPackageByTrackingNumber(String trackingNumber);
    
    /**
     * Get packages by customer email (for customer view)
     */
    List<UnifiedPackageDTO> getPackagesByCustomerEmail(String email);
    
    /**
     * Get packages by customer phone (for customer view)
     */
    List<UnifiedPackageDTO> getPackagesByCustomerPhone(String phone);
    
    /**
     * Get packages assigned to driver (for driver view)
     */
    List<UnifiedPackageDTO> getPackagesByDriverId(Long driverId);
    
    /**
     * Get packages by status (for admin/customer view)
     */
    List<UnifiedPackageDTO> getPackagesByStatus(String status);
    
    /**
     * Get packages by booking status (for admin view)
     */
    List<UnifiedPackageDTO> getPackagesByBookingStatus(BookingStatus status);
    
    /**
     * Get packages by shipment status (for driver/customer view)
     */
    List<UnifiedPackageDTO> getPackagesByShipmentStatus(ShipmentStatus status);
    
    /**
     * Get pending packages (not yet assigned)
     */
    List<UnifiedPackageDTO> getPendingPackages();
    
    /**
     * Get packages currently in transit
     */
    List<UnifiedPackageDTO> getInTransitPackages();
    
    /**
     * Get delivered packages
     */
    List<UnifiedPackageDTO> getDeliveredPackages();
    
    /**
     * Update package status (synchronizes Booking and Shipment)
     */
    UnifiedPackageDTO updatePackageStatus(String trackingNumber, String status);
    
    /**
     * Assign package to driver (synchronizes Booking and Shipment)
     */
    UnifiedPackageDTO assignPackageToDriver(String trackingNumber, Long driverId);
    
    /**
     * Unassign package from driver (synchronizes Booking and Shipment)
     */
    UnifiedPackageDTO unassignPackageFromDriver(String trackingNumber);
    
    /**
     * Get package statistics
     */
    PackageStatistics getPackageStatistics();
    
    /**
     * Search packages by various criteria
     */
    List<UnifiedPackageDTO> searchPackages(String searchTerm);
    
    /**
     * Update package details (addresses, weight, dimensions, description, recipient info)
     * Only works for PENDING packages
     */
    UnifiedPackageDTO updatePackageDetails(String trackingNumber, Map<String, Object> updates);
    
    class PackageStatistics {
        private long totalPackages;
        private long pendingPackages;
        private long assignedPackages;
        private long inTransitPackages;
        private long deliveredPackages;
        private long cancelledPackages;
        
        // Getters and Setters
        public long getTotalPackages() { return totalPackages; }
        public void setTotalPackages(long totalPackages) { this.totalPackages = totalPackages; }
        public long getPendingPackages() { return pendingPackages; }
        public void setPendingPackages(long pendingPackages) { this.pendingPackages = pendingPackages; }
        public long getAssignedPackages() { return assignedPackages; }
        public void setAssignedPackages(long assignedPackages) { this.assignedPackages = assignedPackages; }
        public long getInTransitPackages() { return inTransitPackages; }
        public void setInTransitPackages(long inTransitPackages) { this.inTransitPackages = inTransitPackages; }
        public long getDeliveredPackages() { return deliveredPackages; }
        public void setDeliveredPackages(long deliveredPackages) { this.deliveredPackages = deliveredPackages; }
        public long getCancelledPackages() { return cancelledPackages; }
        public void setCancelledPackages(long cancelledPackages) { this.cancelledPackages = cancelledPackages; }
    }
}

