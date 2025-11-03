package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageInfo;
import com.reliablecarriers.Reliable.Carriers.dto.PackagePickupRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PackageDeliveryRequest;
import com.reliablecarriers.Reliable.Carriers.dto.WorkboardStats;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import com.reliablecarriers.Reliable.Carriers.service.DriverWorkboardService;
// Notification service is used via constructor injection
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// removed unused File import
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DriverWorkboardServiceImpl implements DriverWorkboardService {

    private final ShipmentRepository shipmentRepository;

    private final ProofOfDeliveryRepository proofOfDeliveryRepository;

    private final DriverLocationRepository driverLocationRepository;

    private final UserRepository userRepository;

    // NotificationService used for sending notifications
    private final NotificationService notificationService;

    public DriverWorkboardServiceImpl(ShipmentRepository shipmentRepository,
                                      ProofOfDeliveryRepository proofOfDeliveryRepository,
                                      DriverLocationRepository driverLocationRepository,
                                      UserRepository userRepository,
                                      NotificationService notificationService) {
    this.shipmentRepository = shipmentRepository;
    this.proofOfDeliveryRepository = proofOfDeliveryRepository;
        this.driverLocationRepository = driverLocationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private static final String UPLOAD_DIR = "uploads/driver/";
    private static final double EARTH_RADIUS = 6371; // Earth's radius in kilometers

    @Override
    public WorkboardStats getWorkboardStats(Long driverId, Double currentLat, Double currentLng) {
        WorkboardStats stats = new WorkboardStats();
        
        try {
            // Get current date for today's calculations
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date today = cal.getTime();
            
            // Get packages assigned to driver
            List<Shipment> assignedPackages = shipmentRepository.findByAssignedDriverId(driverId);
            
            // Calculate comprehensive statistics
            stats.setTotalPackages((long) assignedPackages.size());
            stats.setPackagesToPickup(assignedPackages.stream()
                    .filter(p -> p.getStatus() == ShipmentStatus.PENDING || p.getStatus() == ShipmentStatus.ASSIGNED)
                    .count());
            stats.setPackagesInVehicle(assignedPackages.stream()
                    .filter(p -> p.getStatus() == ShipmentStatus.IN_TRANSIT)
                    .count());
            
            // Get today's deliveries
            List<ProofOfDelivery> todayDeliveries = proofOfDeliveryRepository.findByDriverIdAndDeliveryDateAfter(driverId, today);
            stats.setPackagesDeliveredToday((long) todayDeliveries.size());
            
            // Calculate total weight
            double totalWeight = assignedPackages.stream()
                    .mapToDouble(Shipment::getWeight)
                    .sum();
            stats.setTotalWeight(totalWeight);
            
            // Calculate total distance and estimated time
            if (currentLat != null && currentLng != null) {
                double totalDistance = calculateTotalRouteDistance(assignedPackages, currentLat, currentLng);
                stats.setTotalDistance(totalDistance);
                
                // Estimate time remaining (2 minutes per km + 10 minutes per stop)
                int estimatedTime = (int) (totalDistance * 2 + assignedPackages.size() * 10);
                stats.setEstimatedTimeRemaining(estimatedTime);
            }
            
            // Get driver location
            DriverLocation driverLocation = driverLocationRepository.findTopByDriverIdOrderByTimestampDesc(driverId);
            if (driverLocation != null) {
                stats.setCurrentLat(driverLocation.getLatitude());
                stats.setCurrentLng(driverLocation.getLongitude());
                stats.setCurrentLocation(driverLocation.getAddress());
                stats.setLastLocationUpdate(java.sql.Timestamp.valueOf(driverLocation.getTimestamp()));
            } else if (currentLat != null && currentLng != null) {
                stats.setCurrentLat(currentLat);
                stats.setCurrentLng(currentLng);
                stats.setCurrentLocation("Current Location");
            }
            
            // Calculate today's earnings
            BigDecimal todayEarnings = calculateTodayEarnings(driverId, today);
            stats.setTodayEarnings(todayEarnings);
            stats.setDriverStatus("ONLINE");
            
        } catch (Exception e) {
            System.err.println("Error in getWorkboardStats: " + e.getMessage());
            e.printStackTrace();
            
            // Return default stats on error
            stats.setTotalPackages(0L);
            stats.setPackagesToPickup(0L);
            stats.setPackagesInVehicle(0L);
            stats.setPackagesDeliveredToday(0L);
            stats.setTotalWeight(0.0);
            stats.setTotalDistance(0.0);
            stats.setEstimatedTimeRemaining(0);
            stats.setTodayEarnings(BigDecimal.valueOf(0.0));
            stats.setDriverStatus("ONLINE");
            
            if (currentLat != null && currentLng != null) {
                stats.setCurrentLat(currentLat);
                stats.setCurrentLng(currentLng);
                stats.setCurrentLocation("Current Location");
            }
        }
        
        return stats;
    }

    @Override
    public List<DriverPackageInfo> getAvailablePackagesForPickup(Long driverId, Double currentLat, Double currentLng, Double maxDistance, Integer page, Integer size) {
        try {
            // Get all packages that are available for pickup (PENDING and not assigned, or ASSIGNED but not to this driver)
            List<Shipment> availablePackages;
            try {
                // First try to get PENDING packages not assigned to any driver
                availablePackages = shipmentRepository.findByStatusAndAssignedDriverIsNull(ShipmentStatus.PENDING);
                System.out.println("Found " + availablePackages.size() + " PENDING packages not assigned to any driver");
                
                // If no PENDING packages, also check for ASSIGNED packages not assigned to this driver
                if (availablePackages.isEmpty()) {
                    List<Shipment> assignedPackages = shipmentRepository.findByStatus(ShipmentStatus.ASSIGNED);
                    System.out.println("Found " + assignedPackages.size() + " ASSIGNED packages total");
                    
                    // Filter out packages assigned to this driver
                    availablePackages = assignedPackages.stream()
                            .filter(pkg -> pkg.getAssignedDriver() == null || !pkg.getAssignedDriver().getId().equals(driverId))
                            .collect(Collectors.toList());
                    System.out.println("Found " + availablePackages.size() + " ASSIGNED packages not assigned to this driver");
                }
                
                // If still no packages, get all packages regardless of status for debugging
                if (availablePackages.isEmpty()) {
                    List<Shipment> allPackages = shipmentRepository.findAll();
                    System.out.println("Total packages in database: " + allPackages.size());
                    for (Shipment pkg : allPackages) {
                        System.out.println("Package " + pkg.getId() + ": Status=" + pkg.getStatus() + 
                                        ", AssignedDriver=" + (pkg.getAssignedDriver() != null ? pkg.getAssignedDriver().getId() : "null"));
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Error fetching available packages: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        
        // Filter by distance if location is provided
        if (currentLat != null && currentLng != null) {
            availablePackages = availablePackages.stream()
                    .filter(pkg -> {
                        double distance = calculateDistanceInternal(currentLat, currentLng, 
                                getPackageLatitude(pkg), getPackageLongitude(pkg));
                        return distance <= maxDistance;
                    })
                    .sorted((p1, p2) -> {
                        double dist1 = calculateDistanceInternal(currentLat, currentLng, 
                                getPackageLatitude(p1), getPackageLongitude(p1));
                        double dist2 = calculateDistanceInternal(currentLat, currentLng, 
                                getPackageLatitude(p2), getPackageLongitude(p2));
                        return Double.compare(dist1, dist2);
                    })
                    .collect(Collectors.toList());
        }
        
        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, availablePackages.size());
        if (start < availablePackages.size()) {
            availablePackages = availablePackages.subList(start, end);
        } else {
            availablePackages = new ArrayList<>();
        }
        
        return availablePackages.stream()
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
        
        } catch (Exception e) {
            System.out.println("Error in getAvailablePackagesForPickup: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> debugAllPackages() {
        Map<String, Object> debugInfo = new HashMap<>();
        try {
            List<Shipment> allPackages = shipmentRepository.findAll();
            debugInfo.put("totalPackages", allPackages.size());
            
            Map<String, Long> statusCounts = new HashMap<>();
            Map<String, Long> assignedDriverCounts = new HashMap<>();
            
            for (Shipment pkg : allPackages) {
                String status = pkg.getStatus() != null ? pkg.getStatus().toString() : "NULL";
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
                
                String assignedDriver = pkg.getAssignedDriver() != null ? 
                    pkg.getAssignedDriver().getId().toString() : "NULL";
                assignedDriverCounts.put(assignedDriver, assignedDriverCounts.getOrDefault(assignedDriver, 0L) + 1);
            }
            
            debugInfo.put("statusCounts", statusCounts);
            debugInfo.put("assignedDriverCounts", assignedDriverCounts);
            
            // Get packages by status
            List<Shipment> pendingPackages = shipmentRepository.findByStatus(ShipmentStatus.PENDING);
            List<Shipment> assignedPackages = shipmentRepository.findByStatus(ShipmentStatus.ASSIGNED);
            List<Shipment> pendingNotAssigned = shipmentRepository.findByStatusAndAssignedDriverIsNull(ShipmentStatus.PENDING);
            
            debugInfo.put("pendingPackages", pendingPackages.size());
            debugInfo.put("assignedPackages", assignedPackages.size());
            debugInfo.put("pendingNotAssigned", pendingNotAssigned.size());
            
            // Sample package details
            List<Map<String, Object>> samplePackages = new ArrayList<>();
            for (int i = 0; i < Math.min(5, allPackages.size()); i++) {
                Shipment pkg = allPackages.get(i);
                Map<String, Object> pkgInfo = new HashMap<>();
                pkgInfo.put("id", pkg.getId());
                pkgInfo.put("trackingNumber", pkg.getTrackingNumber());
                pkgInfo.put("status", pkg.getStatus());
                pkgInfo.put("assignedDriver", pkg.getAssignedDriver() != null ? pkg.getAssignedDriver().getId() : null);
                pkgInfo.put("createdAt", pkg.getCreatedAt());
                samplePackages.add(pkgInfo);
            }
            debugInfo.put("samplePackages", samplePackages);
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return debugInfo;
    }

    @Override
    public List<DriverPackageInfo> getAssignedPackages(Long driverId, Double currentLat, Double currentLng) {
        List<Shipment> assignedPackages = shipmentRepository.findByAssignedDriverId(driverId);
        
        List<DriverPackageInfo> packageInfos = assignedPackages.stream()
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
        
        // Add distance information if current location is provided
        if (currentLat != null && currentLng != null) {
            for (DriverPackageInfo pkg : packageInfos) {
                double distance = calculateDistanceInternal(currentLat, currentLng, 
                        getPackageLatitude(pkg), getPackageLongitude(pkg));
                pkg.setDistanceFromCurrentLocation(distance);
            }
        }
        
        return packageInfos;
    }

    @Override
    public List<DriverPackageInfo> getOptimizedRoute(Long driverId, Double currentLat, Double currentLng) {
        List<Shipment> assignedPackages = shipmentRepository.findByAssignedDriverId(driverId);
        
        if (assignedPackages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Simple route optimization using nearest neighbor algorithm
        List<Shipment> optimizedRoute = optimizeRoute(assignedPackages, currentLat, currentLng);
        
        return optimizedRoute.stream()
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
    }

    @Override
    public DriverPackageInfo pickupPackage(PackagePickupRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        // Verify driver is assigned to this package
        if (!shipment.getAssignedDriver().getId().equals(request.getDriverId())) {
            throw new RuntimeException("Driver not authorized to pick up this package");
        }
        
        // Verify collection code if provided
        if (request.getCollectionCode() != null && !request.getCollectionCode().trim().isEmpty()) {
            if (shipment.getCollectionCode() == null || !shipment.getCollectionCode().equals(request.getCollectionCode())) {
                throw new RuntimeException("Invalid collection code. Please verify the code with the customer.");
            }
            System.out.println("Collection code verified for pickup: " + request.getCollectionCode());
        } else {
            System.out.println("Warning: No collection code provided for pickup");
        }
        
        // Update package status
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setUpdatedAt(new Date());
        shipmentRepository.save(shipment);
        
        // Save pickup proof if provided
        if (request.getSignature() != null || request.getSignaturePhoto() != null || request.getPackagePhoto() != null) {
            savePickupProof(request);
        }
        
        // Update driver location if provided
        if (request.getPickupLat() != null && request.getPickupLng() != null) {
            updateDriverLocation(request.getDriverId(), request.getPickupLat(), request.getPickupLng(), request.getPickupAddress());
        }
        
        // Send notifications
        sendPickupNotifications(shipment);
        
        return convertToDriverPackageInfo(shipment);
    }

    @Override
    public DriverPackageInfo deliverPackage(PackageDeliveryRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        // Verify driver is assigned to this package
        if (!shipment.getAssignedDriver().getId().equals(request.getDriverId())) {
            throw new RuntimeException("Driver not authorized to deliver this package");
        }
        
        // Verify drop-off code if provided
        if (request.getDropOffCode() != null && !request.getDropOffCode().trim().isEmpty()) {
            if (shipment.getDropOffCode() == null || !shipment.getDropOffCode().equals(request.getDropOffCode())) {
                throw new RuntimeException("Invalid drop-off code. Please verify the code with the customer.");
            }
            System.out.println("Drop-off code verified for delivery: " + request.getDropOffCode());
        } else {
            System.out.println("Warning: No drop-off code provided for delivery");
        }
        
        // Update package status
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDeliveryDate(new Date());
        shipment.setUpdatedAt(new Date());
        shipmentRepository.save(shipment);
        
        // Create proof of delivery
        createProofOfDelivery(request, shipment);
        
        // Update driver location if provided
        if (request.getDeliveryLat() != null && request.getDeliveryLng() != null) {
            updateDriverLocation(request.getDriverId(), request.getDeliveryLat(), request.getDeliveryLng(), request.getDeliveryAddress());
        }
        
        // Send notifications
        sendDeliveryNotifications(shipment);
        
        return convertToDriverPackageInfo(shipment);
    }

    @Override
    public void requestPackagePickup(Long driverId, Long packageId) {
        Shipment shipment = shipmentRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        // Check if package is available for pickup
        if (shipment.getStatus() != ShipmentStatus.PENDING || shipment.getAssignedDriver() != null) {
            throw new RuntimeException("Package is not available for pickup");
        }
        
        // Assign package to driver
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        shipment.setAssignedDriver(driver);
        shipment.setStatus(ShipmentStatus.ASSIGNED);
        shipment.setUpdatedAt(new Date());
        shipmentRepository.save(shipment);
        
        // Send notification to admin/staff
        sendPickupRequestNotification(shipment, driver);
    }

    @Override
    public List<DriverPackageInfo> getNearbyPackages(Long driverId, Double currentLat, Double currentLng, Double radius) {
        List<Shipment> nearbyPackages = shipmentRepository.findByStatus(ShipmentStatus.PENDING);
        
        return nearbyPackages.stream()
                .filter(pkg -> {
                    double distance = calculateDistanceInternal(currentLat, currentLng, 
                            getPackageLatitude(pkg), getPackageLongitude(pkg));
                    return distance <= radius;
                })
                .sorted((p1, p2) -> {
                    double dist1 = calculateDistanceInternal(currentLat, currentLng, 
                            getPackageLatitude(p1), getPackageLongitude(p1));
                    double dist2 = calculateDistanceInternal(currentLat, currentLng, 
                            getPackageLatitude(p2), getPackageLongitude(p2));
                    return Double.compare(dist1, dist2);
                })
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPackageDetails(Long driverId, Long packageId) {
        Shipment shipment = shipmentRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        Map<String, Object> details = new HashMap<>();
        details.put("package", convertToDriverPackageInfo(shipment));
        
        // Get proof of delivery if exists
        Optional<ProofOfDelivery> proof = proofOfDeliveryRepository.findByShipmentId(packageId);
        proof.ifPresent(pod -> details.put("proofOfDelivery", pod));
        
        // Get tracking history
        List<ShipmentTracking> trackingHistory = getTrackingHistory(packageId);
        details.put("trackingHistory", trackingHistory);
        
        return details;
    }

    @Override
    public void updateDriverLocation(Long driverId, Double lat, Double lng, String address) {
        DriverLocation location = new DriverLocation();
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        location.setDriverId(driver.getId());
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setAddress(address);
        location.setTimestamp(java.time.LocalDateTime.now());
        
        driverLocationRepository.save(location);
    }

    @Override
    public Map<String, Object> getDriverLocation(Long driverId) {
        DriverLocation location = driverLocationRepository.findTopByDriverIdOrderByTimestampDesc(driverId);
        
        Map<String, Object> locationData = new HashMap<>();
        if (location != null) {
            locationData.put("lat", location.getLatitude());
            locationData.put("lng", location.getLongitude());
            locationData.put("address", location.getAddress());
            locationData.put("timestamp", location.getTimestamp());
        }
        
        return locationData;
    }

    @Override
    public Map<String, Object> getTodaySummary(Long driverId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get today's deliveries
        List<ProofOfDelivery> todayDeliveries = proofOfDeliveryRepository.findByDriverIdAndDeliveryDateAfter(driverId, today);
        summary.put("deliveriesCompleted", todayDeliveries.size());
        
        // Get today's pickups
        List<Shipment> todayPickups = shipmentRepository.findByAssignedDriverIdAndStatusAndUpdatedAtAfter(driverId, ShipmentStatus.IN_TRANSIT, today);
        summary.put("pickupsCompleted", todayPickups.size());
        
        // Calculate total distance
        double totalDistance = calculateTodayDistance(driverId, today);
        summary.put("totalDistance", totalDistance);
        
        // Calculate earnings
        BigDecimal todayEarnings = calculateTodayEarnings(driverId, today);
        summary.put("todayEarnings", todayEarnings);
        
        return summary;
    }

    @Override
    public DriverPackageInfo markFailedDelivery(Long driverId, Long packageId, String reason, String notes, MultipartFile photo) {
        Shipment shipment = shipmentRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        // Update package status
        shipment.setStatus(ShipmentStatus.FAILED_DELIVERY);
        shipment.setUpdatedAt(new Date());
        shipmentRepository.save(shipment);
        
        // Create proof of delivery with failure reason
        ProofOfDelivery proof = new ProofOfDelivery();
        proof.setShipment(shipment);
        proof.setDriver(userRepository.findById(driverId).orElse(null));
        proof.setDeliveryDate(new Date());
        proof.setDeliveryStatus("FAILED");
        proof.setFailureReason(reason);
        proof.setDeliveryNotes(notes);
        
        // Save photo if provided
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = savePhoto(photo, "failed_delivery");
            proof.setDeliveryPhotoUrl(photoUrl);
        }
        
        proofOfDeliveryRepository.save(proof);
        
        // Send failure notification
        sendFailedDeliveryNotification(shipment, reason);
        
        return convertToDriverPackageInfo(shipment);
    }

    @Override
    public List<Map<String, Object>> getDeliveryHistory(Long driverId, String date) {
        // Implementation for getting delivery history
        // This would query the proof of delivery repository
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getEarningsSummary(Long driverId, String period) {
        // Implementation for earnings calculation
        // This would calculate earnings based on completed deliveries
        Map<String, Object> earnings = new HashMap<>();
        earnings.put("today", BigDecimal.ZERO);
        earnings.put("week", BigDecimal.ZERO);
        earnings.put("month", BigDecimal.ZERO);
        return earnings;
    }

    @Override
    public Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    @Override
    public Integer estimateTravelTime(Double lat1, Double lng1, Double lat2, Double lng2) {
        double distance = calculateDistance(lat1, lng1, lat2, lng2);
        // Assume average speed of 30 km/h in urban areas
        return (int) (distance * 2); // 2 minutes per km
    }

    @Override
    public List<Map<String, Object>> getPickupHistory(Long driverId, String date) {
        // Implementation for pickup history
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getDriverPerformanceMetrics(Long driverId) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Calculate performance metrics based on delivery history
        metrics.put("onTimeDeliveries", 0);
        metrics.put("lateDeliveries", 0);
        metrics.put("averageDeliveryTime", 0.0);
        metrics.put("customerSatisfactionScore", 0.0);
        
        return metrics;
    }

    @Override
    public void updateDriverStatus(Long driverId, String status) {
        // Implementation for updating driver status
        // This would update a driver status field in the user table
    }

    @Override
    public List<Map<String, Object>> getRealTimeUpdates(Long driverId) {
        // Implementation for real-time updates
        return new ArrayList<>();
    }

    // Helper methods
    private double calculateDistanceInternal(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    private DriverPackageInfo convertToDriverPackageInfo(Shipment shipment) {
        DriverPackageInfo info = new DriverPackageInfo();
        info.setId(shipment.getId());
        info.setTrackingNumber(shipment.getTrackingNumber());
        info.setRecipientName(shipment.getRecipientName());
        info.setRecipientPhone(shipment.getRecipientPhone());
        info.setPickupAddress(shipment.getPickupAddress());
        info.setPickupCity(shipment.getPickupCity());
        info.setPickupState(shipment.getPickupState());
        info.setDeliveryAddress(shipment.getDeliveryAddress());
        info.setDeliveryCity(shipment.getDeliveryCity());
        info.setDeliveryState(shipment.getDeliveryState());
        info.setWeight(shipment.getWeight());
        info.setDescription(shipment.getDescription());
        info.setStatus(shipment.getStatus());
        info.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        info.setActualDeliveryDate(shipment.getActualDeliveryDate());
        
        return info;
    }

    private double getPackageLatitude(Shipment shipment) {
        // In a real implementation, you would geocode the address
        // For now, return a default latitude
        return -26.2041; // Johannesburg latitude
    }

    private double getPackageLongitude(Shipment shipment) {
        // In a real implementation, you would geocode the address
        // For now, return a default longitude
        return 28.0473; // Johannesburg longitude
    }

    private double getPackageLatitude(DriverPackageInfo pkg) {
        // In a real implementation, you would geocode the address
        return -26.2041;
    }

    private double getPackageLongitude(DriverPackageInfo pkg) {
        // In a real implementation, you would geocode the address
        return 28.0473;
    }

    private List<Shipment> optimizeRoute(List<Shipment> packages, Double currentLat, Double currentLng) {
        // Simple nearest neighbor algorithm
        List<Shipment> optimized = new ArrayList<>();
        List<Shipment> remaining = new ArrayList<>(packages);
        
        double currentLatitude = currentLat != null ? currentLat : -26.2041;
        double currentLongitude = currentLng != null ? currentLng : 28.0473;
        
        while (!remaining.isEmpty()) {
            Shipment nearest = null;
            double minDistance = Double.MAX_VALUE;
            
            for (Shipment pkg : remaining) {
                double distance = calculateDistanceInternal(currentLatitude, currentLongitude, 
                        getPackageLatitude(pkg), getPackageLongitude(pkg));
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = pkg;
                }
            }
            
            if (nearest != null) {
                optimized.add(nearest);
                remaining.remove(nearest);
                currentLatitude = getPackageLatitude(nearest);
                currentLongitude = getPackageLongitude(nearest);
            }
        }
        
        return optimized;
    }

    private double calculateTotalRouteDistance(List<Shipment> packages, Double currentLat, Double currentLng) {
        if (packages.isEmpty()) return 0.0;
        
        List<Shipment> optimized = optimizeRoute(packages, currentLat, currentLng);
        double totalDistance = 0.0;
        
        double currentLatitude = currentLat != null ? currentLat : -26.2041;
        double currentLongitude = currentLng != null ? currentLng : 28.0473;
        
        for (Shipment pkg : optimized) {
            totalDistance += calculateDistanceInternal(currentLatitude, currentLongitude, 
                    getPackageLatitude(pkg), getPackageLongitude(pkg));
            currentLatitude = getPackageLatitude(pkg);
            currentLongitude = getPackageLongitude(pkg);
        }
        
        return totalDistance;
    }


    private double calculateTodayDistance(Long driverId, Date today) {
        // Simplified distance calculation
        // In a real implementation, you would calculate based on actual route data
        List<ProofOfDelivery> todayDeliveries = proofOfDeliveryRepository.findByDriverIdAndDeliveryDateAfter(driverId, today);
        return todayDeliveries.size() * 5.0; // Assume 5km per delivery
    }

    private void savePickupProof(PackagePickupRequest request) {
        // Implementation for saving pickup proof
        // This would save signature and photos to storage
    }

    private void createProofOfDelivery(PackageDeliveryRequest request, Shipment shipment) {
        ProofOfDelivery proof = new ProofOfDelivery();
        proof.setShipment(shipment);
        proof.setDriver(userRepository.findById(request.getDriverId()).orElse(null));
        proof.setDeliveryDate(new Date());
        proof.setRecipientName(request.getRecipientName());
        proof.setRecipientPhone(request.getRecipientPhone());
        proof.setRecipientIdNumber(request.getRecipientIdNumber());
        proof.setDeliveryNotes(request.getDeliveryNotes());
        proof.setDeliveryMethod(request.getDeliveryMethod());
        proof.setSignatureRequired(true);
        proof.setPhotoRequired(true);
        proof.setIdVerificationRequired(false);
        proof.setDeliveryStatus("COMPLETED");
        
        // Save signature if provided
        if (request.getSignature() != null) {
            proof.setRecipientSignature(request.getSignature());
        }
        
        // Save photos if provided
        if (request.getSignaturePhoto() != null && !request.getSignaturePhoto().isEmpty()) {
            savePhoto(request.getSignaturePhoto(), "signature");
        }
        
        if (request.getDeliveryPhoto() != null && !request.getDeliveryPhoto().isEmpty()) {
            String deliveryPhotoUrl = savePhoto(request.getDeliveryPhoto(), "delivery");
            proof.setDeliveryPhotoUrl(deliveryPhotoUrl);
        }
        
        proofOfDeliveryRepository.save(proof);
    }

    private String savePhoto(MultipartFile photo, String type) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = type + "_" + timestamp + "_" + photo.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            
            // Save file
            Files.copy(photo.getInputStream(), filePath);
            
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save photo", e);
        }
    }

    private void sendPickupNotifications(Shipment shipment) {
        // Send email notification to sender
        if (shipment.getSender() != null && shipment.getSender().getEmail() != null) {
            notificationService.sendShipmentPickedUpNotification(shipment, 
                shipment.getAssignedDriver() != null ? 
                shipment.getAssignedDriver().getFirstName() + " " + shipment.getAssignedDriver().getLastName() : 
                "Assigned Driver");
        }
        
        // Send SMS notification to recipient
        if (shipment.getRecipientPhone() != null) {
            notificationService.sendCustomSmsNotification(shipment.getRecipientPhone(), 
                    "Your package " + shipment.getTrackingNumber() + " has been picked up and is on its way!");
        }
    }

    private void sendDeliveryNotifications(Shipment shipment) {
        // Send email notification to sender
        if (shipment.getSender() != null && shipment.getSender().getEmail() != null) {
            notificationService.sendShipmentDeliveredNotification(shipment, 
                shipment.getAssignedDriver() != null ? 
                shipment.getAssignedDriver().getFirstName() + " " + shipment.getAssignedDriver().getLastName() : 
                "Assigned Driver",
                new Date().toString());
        }
        
        // Send SMS notification to recipient
        if (shipment.getRecipientPhone() != null) {
            notificationService.sendCustomSmsNotification(shipment.getRecipientPhone(), 
                    "Your package " + shipment.getTrackingNumber() + " has been delivered successfully!");
        }
    }

    private void sendPickupRequestNotification(Shipment shipment, User driver) {
        // Send notification to admin/staff about pickup request
        notificationService.sendCustomEmailNotification("admin@reliablecarriers.com", 
            "Driver Pickup Request", 
            "Driver " + driver.getFirstName() + " " + driver.getLastName() + 
                " has requested to pick up package " + shipment.getTrackingNumber());
    }

    private void sendFailedDeliveryNotification(Shipment shipment, String reason) {
        // Send notification about failed delivery
        if (shipment.getSender() != null && shipment.getSender().getEmail() != null) {
            notificationService.sendShipmentFailedDeliveryNotification(shipment, reason, "Next business day");
        }
    }

    private List<ShipmentTracking> getTrackingHistory(Long packageId) {
        // Implementation for getting tracking history
        // This would query the shipment tracking repository
        return new ArrayList<>();
    }

    @Override
    public boolean acceptPackage(Long driverId, Long packageId) {
        try {
            Optional<Shipment> packageOpt = shipmentRepository.findById(packageId);
            if (!packageOpt.isPresent()) {
                return false;
            }
            
            Shipment shipment = packageOpt.get();
            
            // Check if package is assigned to this driver
            if (shipment.getAssignedDriver() == null || !shipment.getAssignedDriver().getId().equals(driverId)) {
                return false;
            }
            
            // Check if package is in ASSIGNED status
            if (shipment.getStatus() != ShipmentStatus.ASSIGNED) {
                return false;
            }
            
            // Update package status to accepted (you might want to add a new status like ACCEPTED)
            shipment.setStatus(ShipmentStatus.ASSIGNED); // Keep as ASSIGNED for now
            shipment.setUpdatedAt(new Date());
            shipmentRepository.save(shipment);
            
            // Send notification to admin about acceptance
            sendPackageAcceptanceNotification(shipment);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean rejectPackage(Long driverId, Long packageId, String reason) {
        try {
            Optional<Shipment> packageOpt = shipmentRepository.findById(packageId);
            if (!packageOpt.isPresent()) {
                return false;
            }
            
            Shipment shipment = packageOpt.get();
            
            // Check if package is assigned to this driver
            if (shipment.getAssignedDriver() == null || !shipment.getAssignedDriver().getId().equals(driverId)) {
                return false;
            }
            
            // Unassign the package
            shipment.setAssignedDriver(null);
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setUpdatedAt(new Date());
            shipmentRepository.save(shipment);
            
            // Send notification to admin about rejection
            sendPackageRejectionNotification(shipment, reason);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendPackageAcceptanceNotification(Shipment shipment) {
        // Implementation for sending acceptance notification
        // This could send email/SMS to admin
    }

    private void sendPackageRejectionNotification(Shipment shipment, String reason) {
        // Implementation for sending rejection notification
        // This could send email/SMS to admin with reason
    }

    private BigDecimal calculateTodayEarnings(Long driverId, Date today) {
        try {
            // Calculate earnings based on today's deliveries
            List<ProofOfDelivery> todayDeliveries = proofOfDeliveryRepository.findByDriverIdAndDeliveryDateAfter(driverId, today);
            
            // Base rate per delivery
            BigDecimal baseRate = BigDecimal.valueOf(25.0); // R25 per delivery
            BigDecimal totalEarnings = BigDecimal.ZERO;
            
            for (ProofOfDelivery delivery : todayDeliveries) {
                // Add base rate
                totalEarnings = totalEarnings.add(baseRate);
                
                // Add distance bonus (R2 per km) - using default distance if not available
                BigDecimal distance = BigDecimal.valueOf(5.0); // Default 5km
                BigDecimal distanceBonus = distance.multiply(BigDecimal.valueOf(2.0));
                totalEarnings = totalEarnings.add(distanceBonus);
                
                // Add weight bonus (R1 per kg) - using default weight if not available
                BigDecimal weight = BigDecimal.valueOf(2.0); // Default 2kg
                BigDecimal weightBonus = weight.multiply(BigDecimal.valueOf(1.0));
                totalEarnings = totalEarnings.add(weightBonus);
                
                // Log delivery for tracking
                System.out.println("Processed delivery: " + delivery.getId());
            }
            
            return totalEarnings;
        } catch (Exception e) {
            System.err.println("Error calculating today's earnings: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public Map<String, Object> verifyCollectionCode(Long packageId, String collectionCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            Shipment shipment = shipmentRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Package not found"));
            
            if (shipment.getCollectionCode() == null) {
                result.put("valid", false);
                result.put("message", "No collection code found for this package");
                return result;
            }
            
            if (shipment.getCollectionCode().equals(collectionCode)) {
                result.put("valid", true);
                result.put("message", "Collection code verified successfully");
                result.put("packageId", packageId);
                result.put("trackingNumber", shipment.getTrackingNumber());
                System.out.println("Collection code verified for package " + packageId + ": " + collectionCode);
            } else {
                result.put("valid", false);
                result.put("message", "Invalid collection code. Please check the code provided by the customer.");
                System.out.println("Invalid collection code for package " + packageId + ": " + collectionCode + " (expected: " + shipment.getCollectionCode() + ")");
            }
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Error verifying collection code: " + e.getMessage());
            System.err.println("Error verifying collection code: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> verifyDropOffCode(Long packageId, String dropOffCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            Shipment shipment = shipmentRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Package not found"));
            
            if (shipment.getDropOffCode() == null) {
                result.put("valid", false);
                result.put("message", "No drop-off code found for this package");
                return result;
            }
            
            if (shipment.getDropOffCode().equals(dropOffCode)) {
                result.put("valid", true);
                result.put("message", "Drop-off code verified successfully");
                result.put("packageId", packageId);
                result.put("trackingNumber", shipment.getTrackingNumber());
                System.out.println("Drop-off code verified for package " + packageId + ": " + dropOffCode);
            } else {
                result.put("valid", false);
                result.put("message", "Invalid drop-off code. Please check the code provided by the customer.");
                System.out.println("Invalid drop-off code for package " + packageId + ": " + dropOffCode + " (expected: " + shipment.getDropOffCode() + ")");
            }
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Error verifying drop-off code: " + e.getMessage());
            System.err.println("Error verifying drop-off code: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean assignPackageToDriver(Long driverId, Long packageId) {
        try {
            // Get the driver
            User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
            
            // Get the package
            Shipment packageToAssign = shipmentRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));
            
            // Check if package is available for assignment
            if (packageToAssign.getStatus() != ShipmentStatus.PENDING && 
                packageToAssign.getStatus() != ShipmentStatus.ASSIGNED) {
                throw new RuntimeException("Package is not available for assignment");
            }
            
            // Check if package is already assigned to another driver
            if (packageToAssign.getAssignedDriver() != null && 
                !packageToAssign.getAssignedDriver().getId().equals(driverId)) {
                throw new RuntimeException("Package is already assigned to another driver");
            }
            
            // Assign package to driver
            packageToAssign.setAssignedDriver(driver);
            packageToAssign.setStatus(ShipmentStatus.ASSIGNED);
            
            shipmentRepository.save(packageToAssign);
            
            System.out.println("Package " + packageId + " assigned to driver " + driverId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error assigning package " + packageId + " to driver " + driverId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<DriverPackageInfo> batchPickupPackages(Long driverId, List<Long> packageIds, Double currentLat, Double currentLng) {
        List<DriverPackageInfo> pickedUpPackages = new ArrayList<>();
        
        for (Long packageId : packageIds) {
            try {
                PackagePickupRequest request = new PackagePickupRequest();
                request.setDriverId(driverId);
                request.setPackageId(packageId);
                request.setPickupLat(currentLat);
                request.setPickupLng(currentLng);
                
                DriverPackageInfo pickedUp = pickupPackage(request);
                pickedUpPackages.add(pickedUp);
            } catch (Exception e) {
                System.err.println("Error picking up package " + packageId + ": " + e.getMessage());
            }
        }
        
        return pickedUpPackages;
    }

    @Override
    public List<DriverPackageInfo> batchDeliverPackages(Long driverId, List<Long> packageIds, Double currentLat, Double currentLng) {
        List<DriverPackageInfo> deliveredPackages = new ArrayList<>();
        
        for (Long packageId : packageIds) {
            try {
                PackageDeliveryRequest request = new PackageDeliveryRequest();
                request.setDriverId(driverId);
                request.setPackageId(packageId);
                request.setDeliveryLat(currentLat);
                request.setDeliveryLng(currentLng);
                
                DriverPackageInfo delivered = deliverPackage(request);
                deliveredPackages.add(delivered);
            } catch (Exception e) {
                System.err.println("Error delivering package " + packageId + ": " + e.getMessage());
            }
        }
        
        return deliveredPackages;
    }

    @Override
    public Map<String, Object> getOptimizedRouteWithWaypoints(Long driverId, Double currentLat, Double currentLng, String routeType) {
        Map<String, Object> routeData = new HashMap<>();
        
        // Get packages based on route type
        List<Shipment> packages = shipmentRepository.findByAssignedDriverId(driverId);
        
        // Filter by status if route type specified
        if ("pickup".equalsIgnoreCase(routeType)) {
            packages = packages.stream()
                .filter(pkg -> pkg.getStatus() == ShipmentStatus.ASSIGNED)
                .collect(Collectors.toList());
        } else if ("delivery".equalsIgnoreCase(routeType)) {
            packages = packages.stream()
                .filter(pkg -> pkg.getStatus() == ShipmentStatus.IN_TRANSIT)
                .collect(Collectors.toList());
        }
        
        if (packages.isEmpty()) {
            routeData.put("packages", new ArrayList<>());
            routeData.put("waypoints", new ArrayList<>());
            routeData.put("totalDistance", 0.0);
            routeData.put("estimatedTime", 0);
            return routeData;
        }
        
        // Build waypoints list
        List<Map<String, Object>> waypoints = new ArrayList<>();
        List<DriverPackageInfo> packageInfos = new ArrayList<>();
        
        // Optimize route using nearest neighbor
        List<Shipment> optimizedPackages = optimizeRoute(packages, currentLat, currentLng);
        
        for (Shipment pkg : optimizedPackages) {
            DriverPackageInfo info = convertToDriverPackageInfo(pkg);
            packageInfos.add(info);
            
            Map<String, Object> waypoint = new HashMap<>();
            if ("pickup".equalsIgnoreCase(routeType)) {
                waypoint.put("lat", getPackageLatitude(pkg));
                waypoint.put("lng", getPackageLongitude(pkg));
                waypoint.put("address", pkg.getPickupAddress());
                waypoint.put("type", "pickup");
            } else {
                waypoint.put("lat", getDeliveryLatitude(pkg));
                waypoint.put("lng", getDeliveryLongitude(pkg));
                waypoint.put("address", pkg.getDeliveryAddress());
                waypoint.put("type", "delivery");
            }
            waypoint.put("packageId", pkg.getId());
            waypoint.put("trackingNumber", pkg.getTrackingNumber());
            waypoints.add(waypoint);
        }
        
        // Calculate total distance
        double totalDistance = calculateTotalRouteDistance(optimizedPackages, currentLat, currentLng);
        
        routeData.put("packages", packageInfos);
        routeData.put("waypoints", waypoints);
        routeData.put("totalDistance", totalDistance);
        routeData.put("estimatedTime", (int)(totalDistance * 2)); // Rough estimate: 2 minutes per km
        routeData.put("routeType", routeType != null ? routeType : "mixed");
        
        return routeData;
    }

    @Override
    public Map<String, Object> getPackagesOnMap(Long driverId, Double currentLat, Double currentLng, Double radius) {
        Map<String, Object> mapData = new HashMap<>();
        
        // Get assigned packages
        List<DriverPackageInfo> assignedPackages = getAssignedPackages(driverId, currentLat, currentLng);
        
        // Get nearby packages
        List<DriverPackageInfo> nearbyPackages = getNearbyPackages(driverId, currentLat, currentLng, radius);
        
        // Combine and mark duplicates
        Map<Long, DriverPackageInfo> allPackagesMap = new HashMap<>();
        assignedPackages.forEach(pkg -> allPackagesMap.put(pkg.getId(), pkg));
        nearbyPackages.forEach(pkg -> {
            if (!allPackagesMap.containsKey(pkg.getId())) {
                allPackagesMap.put(pkg.getId(), pkg);
            }
        });
        
        List<DriverPackageInfo> allPackages = new ArrayList<>(allPackagesMap.values());
        
        // Build map markers
        List<Map<String, Object>> markers = new ArrayList<>();
        for (DriverPackageInfo pkg : allPackages) {
            Map<String, Object> marker = new HashMap<>();
            marker.put("id", pkg.getId());
            marker.put("trackingNumber", pkg.getTrackingNumber());
            marker.put("status", pkg.getStatus());
            marker.put("isAssigned", assignedPackages.stream().anyMatch(p -> p.getId().equals(pkg.getId())));
            // Use helper methods to get coordinates
            Shipment shipment = shipmentRepository.findById(pkg.getId()).orElse(null);
            if (shipment != null) {
                marker.put("pickupLat", getPackageLatitude(shipment));
                marker.put("pickupLng", getPackageLongitude(shipment));
                marker.put("deliveryLat", getDeliveryLatitude(shipment));
                marker.put("deliveryLng", getDeliveryLongitude(shipment));
            }
            marker.put("pickupAddress", pkg.getPickupAddress());
            marker.put("deliveryAddress", pkg.getDeliveryAddress());
            marker.put("distance", pkg.getDistanceFromCurrentLocation());
            markers.add(marker);
        }
        
        mapData.put("packages", allPackages);
        mapData.put("markers", markers);
        mapData.put("currentLocation", Map.of("lat", currentLat, "lng", currentLng));
        mapData.put("assignedCount", assignedPackages.size());
        mapData.put("nearbyCount", nearbyPackages.size());
        
        return mapData;
    }
    
    private Double getDeliveryLatitude(Shipment shipment) {
        // Try to get from address geocoding or use default
        // Note: You may need to geocode the delivery address if coordinates aren't stored
        return -26.2041; // Default to Johannesburg - update when geocoding is implemented
    }
    
    private Double getDeliveryLongitude(Shipment shipment) {
        return 28.0473; // Default to Johannesburg - update when geocoding is implemented
    }

}
