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
        
        // Get current date
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        
        // Get packages assigned to driver
        List<Shipment> assignedPackages = shipmentRepository.findByAssignedDriverId(driverId);
        
        // Calculate statistics
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
        
        // Calculate total distance (simplified)
        if (currentLat != null && currentLng != null) {
            double totalDistance = calculateTotalRouteDistance(assignedPackages, currentLat, currentLng);
            stats.setTotalDistance(totalDistance);
            
            // Estimate time remaining (simplified calculation)
            int estimatedTime = (int) (totalDistance * 2); // 2 minutes per km
            stats.setEstimatedTimeRemaining(estimatedTime);
        }
        
        // Get driver location
        DriverLocation driverLocation = driverLocationRepository.findByDriverIdOrderByTimestampDesc(driverId);
        if (driverLocation != null) {
            stats.setCurrentLat(driverLocation.getLatitude());
            stats.setCurrentLng(driverLocation.getLongitude());
            stats.setCurrentLocation(driverLocation.getAddress());
            stats.setLastLocationUpdate(driverLocation.getTimestamp());
        }
        
        // Calculate earnings (simplified)
        BigDecimal todayEarnings = calculateTodayEarnings(driverId, today);
        stats.setTodayEarnings(todayEarnings);
        
        // Get driver status
        User driver = userRepository.findById(driverId).orElse(null);
        if (driver != null) {
            stats.setDriverStatus("ONLINE"); // Simplified - you might want to track this separately
        }
        
        // Calculate performance metrics
        Map<String, Object> performance = getDriverPerformanceMetrics(driverId);
        stats.setOnTimeDeliveries((Integer) performance.get("onTimeDeliveries"));
        stats.setLateDeliveries((Integer) performance.get("lateDeliveries"));
        stats.setAverageDeliveryTime((Double) performance.get("averageDeliveryTime"));
        stats.setCustomerSatisfactionScore((Double) performance.get("customerSatisfactionScore"));
        
        return stats;
    }

    @Override
    public List<DriverPackageInfo> getAvailablePackagesForPickup(Long driverId, Double currentLat, Double currentLng, Double maxDistance, Integer page, Integer size) {
        // Get all pending packages that are not assigned to any driver
        List<Shipment> availablePackages = shipmentRepository.findByStatusAndAssignedDriverIsNull(ShipmentStatus.PENDING);
        
        // Filter by distance if location is provided
        if (currentLat != null && currentLng != null) {
            availablePackages = availablePackages.stream()
                    .filter(pkg -> {
                        double distance = calculateDistance(currentLat, currentLng, 
                                getPackageLatitude(pkg), getPackageLongitude(pkg));
                        return distance <= maxDistance;
                    })
                    .sorted((p1, p2) -> {
                        double dist1 = calculateDistance(currentLat, currentLng, 
                                getPackageLatitude(p1), getPackageLongitude(p1));
                        double dist2 = calculateDistance(currentLat, currentLng, 
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
                double distance = calculateDistance(currentLat, currentLng, 
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
                    double distance = calculateDistance(currentLat, currentLng, 
                            getPackageLatitude(pkg), getPackageLongitude(pkg));
                    return distance <= radius;
                })
                .sorted((p1, p2) -> {
                    double dist1 = calculateDistance(currentLat, currentLng, 
                            getPackageLatitude(p1), getPackageLongitude(p1));
                    double dist2 = calculateDistance(currentLat, currentLng, 
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
        location.setDriver(driver);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setAddress(address);
        location.setTimestamp(new Date());
        
        driverLocationRepository.save(location);
    }

    @Override
    public Map<String, Object> getDriverLocation(Long driverId) {
        DriverLocation location = driverLocationRepository.findByDriverIdOrderByTimestampDesc(driverId);
        
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
                double distance = calculateDistance(currentLatitude, currentLongitude, 
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
            totalDistance += calculateDistance(currentLatitude, currentLongitude, 
                    getPackageLatitude(pkg), getPackageLongitude(pkg));
            currentLatitude = getPackageLatitude(pkg);
            currentLongitude = getPackageLongitude(pkg);
        }
        
        return totalDistance;
    }

    private BigDecimal calculateTodayEarnings(Long driverId, Date today) {
        // Simplified earnings calculation
        // In a real implementation, you would calculate based on delivery fees, bonuses, etc.
        List<ProofOfDelivery> todayDeliveries = proofOfDeliveryRepository.findByDriverIdAndDeliveryDateAfter(driverId, today);
        return BigDecimal.valueOf(todayDeliveries.size() * 50); // $50 per delivery
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
}
