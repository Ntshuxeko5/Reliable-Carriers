package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.DriverLocationResponse;
import com.reliablecarriers.Reliable.Carriers.dto.ShipmentInfo;
import com.reliablecarriers.Reliable.Carriers.dto.TrackingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.TrackingResponse;
import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.repository.DriverLocationRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.repository.VehicleRepository;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentService;
import com.reliablecarriers.Reliable.Carriers.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackingServiceImpl implements TrackingService {

    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ShipmentService shipmentService;

    @Override
    public List<DriverLocationResponse> getAllActiveDriverLocations() {
        // Get all drivers
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        
        // Get the most recent location for each driver
        List<DriverLocationResponse> activeLocations = new ArrayList<>();
        for (User driver : drivers) {
            DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
            if (lastLocation != null && isLocationRecent(lastLocation.getTimestamp())) {
                DriverLocationResponse response = new DriverLocationResponse(lastLocation);
                populateShipmentInfo(response);
                activeLocations.add(response);
            }
        }
        
        return activeLocations;
    }

    @Override
    public List<DriverLocationResponse> getDriverLocationsByFilters(TrackingRequest request) {
        List<DriverLocationResponse> results = new ArrayList<>();
        
        // If specific driver IDs are provided
        if (request.getDriverIds() != null && !request.getDriverIds().isEmpty()) {
            for (Long driverId : request.getDriverIds()) {
                User driver = userRepository.findById(driverId).orElse(null);
                if (driver != null) {
                    DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
                    if (lastLocation != null) {
                        DriverLocationResponse response = new DriverLocationResponse(lastLocation);
                        populateShipmentInfo(response);
                        results.add(response);
                    }
                }
            }
        } else {
            // Get all drivers
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            for (User driver : drivers) {
                DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
                if (lastLocation != null) {
                    DriverLocationResponse response = new DriverLocationResponse(lastLocation);
                    populateShipmentInfo(response);
                    results.add(response);
                }
            }
        }
        
        // Apply filters
        results = applyFilters(results, request);
        
        // Apply limit
        if (request.getLimit() != null && results.size() > request.getLimit()) {
            results = results.subList(0, request.getLimit());
        }
        
        return results;
    }

    @Override
    public List<DriverLocationResponse> getRealTimeLocations(List<Long> driverIds) {
        List<DriverLocationResponse> realTimeLocations = new ArrayList<>();
        
        for (Long driverId : driverIds) {
            User driver = userRepository.findById(driverId).orElse(null);
            if (driver != null) {
                DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
                if (lastLocation != null && isLocationRecent(lastLocation.getTimestamp())) {
                    DriverLocationResponse response = new DriverLocationResponse(lastLocation);
                    populateShipmentInfo(response);
                    realTimeLocations.add(response);
                }
            }
        }
        
        return realTimeLocations;
    }

    @Override
    public List<DriverLocationResponse> getDriverLocationHistory(Long driverId, Date startTime, Date endTime) {
        User driver = userRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return new ArrayList<>();
        }
        
        // Convert Date to LocalDateTime
        LocalDateTime startDateTime = startTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDateTime = endTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        
        List<DriverLocation> locations = driverLocationRepository.findByDriverAndTimestampBetweenOrderByTimestampDesc(driver.getId(), startDateTime, endDateTime);
        return locations.stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverLocationResponse> getVehicleLocationHistory(Long vehicleId, Date startTime, Date endTime) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            return new ArrayList<>();
        }
        
        // Convert Date to LocalDateTime
        LocalDateTime startDateTime = startTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDateTime = endTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        
        List<DriverLocation> locations = driverLocationRepository.findByVehicleAndTimestampBetweenOrderByTimestampDesc(vehicle.getRegistrationNumber(), startDateTime, endDateTime);
        return locations.stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverLocationResponse> getDriversInBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        List<DriverLocation> locations = driverLocationRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);
        
        // Get the most recent location for each driver in the bounding box
        Map<Long, DriverLocation> latestLocations = new HashMap<>();
        for (DriverLocation location : locations) {
            Long driverId = location.getDriverId();
            if (!latestLocations.containsKey(driverId) || 
                location.getTimestamp().isAfter(latestLocations.get(driverId).getTimestamp())) {
                latestLocations.put(driverId, location);
            }
        }
        
        return latestLocations.values().stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverLocationResponse> getDriversByLocation(String city, String state) {
        List<DriverLocation> locations = driverLocationRepository.findByCityAndState(city, state);
        
        // Get the most recent location for each driver in the location
        Map<Long, DriverLocation> latestLocations = new HashMap<>();
        for (DriverLocation location : locations) {
            Long driverId = location.getDriverId();
            if (!latestLocations.containsKey(driverId) || 
                location.getTimestamp().isAfter(latestLocations.get(driverId).getTimestamp())) {
                latestLocations.put(driverId, location);
            }
        }
        
        return latestLocations.values().stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverLocationResponse> searchDrivers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchLower = searchTerm.toLowerCase();
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        List<DriverLocationResponse> results = new ArrayList<>();
        
        for (User driver : drivers) {
            boolean matches = driver.getFirstName().toLowerCase().contains(searchLower) ||
                            driver.getLastName().toLowerCase().contains(searchLower) ||
                            driver.getEmail().toLowerCase().contains(searchLower) ||
                            (driver.getPhone() != null && driver.getPhone().contains(searchTerm));
            
            if (matches) {
                DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
                if (lastLocation != null) {
                    results.add(new DriverLocationResponse(lastLocation));
                }
            }
        }
        
        return results;
    }

    @Override
    public Map<String, Object> getTrackingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        long totalDrivers = drivers.size();
        long onlineDrivers = 0;
        long offlineDrivers = 0;
        int totalPackages = 0;
        double totalWeight = 0.0;
        
        for (User driver : drivers) {
            DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
            if (lastLocation != null && isLocationRecent(lastLocation.getTimestamp())) {
                onlineDrivers++;
            } else {
                offlineDrivers++;
            }
            
            // Get package information for this driver
            try {
                List<Shipment> activeShipments = shipmentService.getShipmentsByDriver(driver);
                List<Shipment> currentShipments = activeShipments.stream()
                    .filter(shipment -> shipment.getStatus() != ShipmentStatus.DELIVERED 
                        && shipment.getStatus() != ShipmentStatus.CANCELLED)
                    .collect(Collectors.toList());
                
                totalPackages += currentShipments.size();
                totalWeight += currentShipments.stream()
                    .mapToDouble(shipment -> shipment.getWeight() != null ? shipment.getWeight() : 0.0)
                    .sum();
            } catch (Exception e) {
                // Log error but don't fail the entire statistics
                System.err.println("Error getting shipments for driver " + driver.getId() + ": " + e.getMessage());
            }
        }
        
        stats.put("totalDrivers", totalDrivers);
        stats.put("onlineDrivers", onlineDrivers);
        stats.put("offlineDrivers", offlineDrivers);
        stats.put("onlinePercentage", totalDrivers > 0 ? (double) onlineDrivers / totalDrivers * 100 : 0);
        stats.put("totalPackages", totalPackages);
        stats.put("totalWeight", totalWeight);
        
        return stats;
    }

    @Override
    public Map<String, Long> getDriverStatusSummary() {
        Map<String, Long> summary = new HashMap<>();
        
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        long online = 0;
        long offline = 0;
        
        for (User driver : drivers) {
            DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
            if (lastLocation != null && isLocationRecent(lastLocation.getTimestamp())) {
                online++;
            } else {
                offline++;
            }
        }
        
        summary.put("ONLINE", online);
        summary.put("OFFLINE", offline);
        
        return summary;
    }

    @Override
    public Map<String, Object> getVehicleTrackingSummary(Long vehicleId) {
        Map<String, Object> summary = new HashMap<>();
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            return summary;
        }
        
        DriverLocation lastLocation = driverLocationRepository.findTopByVehicleOrderByTimestampDesc(vehicle.getRegistrationNumber());
        if (lastLocation != null) {
            summary.put("lastLocation", new DriverLocationResponse(lastLocation));
            summary.put("isOnline", isLocationRecent(lastLocation.getTimestamp()));
        }
        
        return summary;
    }

    @Override
    public Map<String, Object> getDriverTrackingSummary(Long driverId) {
        Map<String, Object> summary = new HashMap<>();
        
        User driver = userRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return summary;
        }
        
        DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
        if (lastLocation != null) {
            summary.put("lastLocation", new DriverLocationResponse(lastLocation));
            summary.put("isOnline", isLocationRecent(lastLocation.getTimestamp()));
        }
        
        return summary;
    }

    @Override
    public boolean isDriverOnline(Long driverId) {
        User driver = userRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return false;
        }
        
        DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
        return lastLocation != null && isLocationRecent(lastLocation.getTimestamp());
    }

    @Override
    public DriverLocationResponse getLastKnownLocation(Long driverId) {
        User driver = userRepository.findById(driverId).orElse(null);
        if (driver == null) {
            return null;
        }
        
        DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
        if (lastLocation != null) {
            DriverLocationResponse response = new DriverLocationResponse(lastLocation);
            populateShipmentInfo(response);
            return response;
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getAllDriversWithStatus() {
        List<Map<String, Object>> driversWithStatus = new ArrayList<>();
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        
        for (User driver : drivers) {
            Map<String, Object> driverInfo = new HashMap<>();
            driverInfo.put("id", driver.getId());
            driverInfo.put("name", driver.getFirstName() + " " + driver.getLastName());
            driverInfo.put("email", driver.getEmail());
            driverInfo.put("phone", driver.getPhone());
            
            DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
            boolean isOnline = lastLocation != null && isLocationRecent(lastLocation.getTimestamp());
            driverInfo.put("isOnline", isOnline);
            driverInfo.put("status", isOnline ? "ONLINE" : "OFFLINE");
            
            if (lastLocation != null) {
                DriverLocationResponse response = new DriverLocationResponse(lastLocation);
                populateShipmentInfo(response);
                driverInfo.put("lastLocation", response);
            }
            
            driversWithStatus.add(driverInfo);
        }
        
        return driversWithStatus;
    }

    @Override
    public List<Map<String, Object>> getAllVehiclesWithStatus() {
        List<Map<String, Object>> vehiclesWithStatus = new ArrayList<>();
        List<Vehicle> vehicles = vehicleRepository.findAll();
        
        for (Vehicle vehicle : vehicles) {
            Map<String, Object> vehicleInfo = new HashMap<>();
            vehicleInfo.put("id", vehicle.getId());
            vehicleInfo.put("model", vehicle.getModel());
            vehicleInfo.put("licensePlate", vehicle.getRegistrationNumber());
            vehicleInfo.put("type", vehicle.getType());
            
            DriverLocation lastLocation = driverLocationRepository.findTopByVehicleOrderByTimestampDesc(vehicle.getRegistrationNumber());
            boolean isOnline = lastLocation != null && isLocationRecent(lastLocation.getTimestamp());
            vehicleInfo.put("isOnline", isOnline);
            vehicleInfo.put("status", isOnline ? "ONLINE" : "OFFLINE");
            
            if (lastLocation != null) {
                DriverLocationResponse response = new DriverLocationResponse(lastLocation);
                populateShipmentInfo(response);
                vehicleInfo.put("lastLocation", response);
            }
            
            vehiclesWithStatus.add(vehicleInfo);
        }
        
        return vehiclesWithStatus;
    }

    private List<DriverLocationResponse> applyFilters(List<DriverLocationResponse> locations, TrackingRequest request) {
        return locations.stream()
                .filter(location -> {
                    // Status filter
                    if (request.getStatus() != null && !request.getStatus().equals("ALL")) {
                        if (!location.getStatus().equals(request.getStatus())) {
                            return false;
                        }
                    }
                    
                    // City filter
                    if (request.getCity() != null && !request.getCity().isEmpty()) {
                        if (location.getCity() == null || !location.getCity().equalsIgnoreCase(request.getCity())) {
                            return false;
                        }
                    }
                    
                    // State filter
                    if (request.getState() != null && !request.getState().isEmpty()) {
                        if (location.getState() == null || !location.getState().equalsIgnoreCase(request.getState())) {
                            return false;
                        }
                    }
                    
                    // Geographic bounding box filter
                    if (request.getMinLatitude() != null && request.getMaxLatitude() != null &&
                        request.getMinLongitude() != null && request.getMaxLongitude() != null) {
                        if (location.getLatitude() < request.getMinLatitude() || 
                            location.getLatitude() > request.getMaxLatitude() ||
                            location.getLongitude() < request.getMinLongitude() || 
                            location.getLongitude() > request.getMaxLongitude()) {
                            return false;
                        }
                    }
                    
                    // Include offline drivers filter
                    if (!request.getIncludeOfflineDrivers() && !location.getIsOnline()) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    private boolean isLocationRecent(LocalDateTime timestamp) {
        if (timestamp == null) return false;
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return timestamp.isAfter(fiveMinutesAgo);
    }

    /**
     * Populates shipment information for a driver location response
     */
    private void populateShipmentInfo(DriverLocationResponse response) {
        if (response.getDriverId() == null) return;
        
        try {
            // Get active shipments for the driver (not delivered or cancelled)
            List<Shipment> activeShipments = shipmentService.getShipmentsByDriver(
                userRepository.findById(response.getDriverId()).orElse(null)
            );
            
            // Filter out delivered and cancelled shipments
            List<Shipment> currentShipments = activeShipments.stream()
                .filter(shipment -> shipment.getStatus() != ShipmentStatus.DELIVERED 
                    && shipment.getStatus() != ShipmentStatus.CANCELLED)
                .collect(Collectors.toList());
            
            // Convert to ShipmentInfo DTOs
            List<ShipmentInfo> shipmentInfos = currentShipments.stream()
                .map(ShipmentInfo::new)
                .collect(Collectors.toList());
            
            response.setActiveShipments(new ArrayList<Object>(shipmentInfos));
            response.setTotalPackages(shipmentInfos.size());
            
            // Calculate total weight
            double totalWeight = shipmentInfos.stream()
                .mapToDouble(shipment -> shipment.getWeight() != null ? shipment.getWeight() : 0.0)
                .sum();
            response.setTotalWeight(totalWeight);
            
            // Find next delivery location and time
            if (!shipmentInfos.isEmpty()) {
                // Sort by estimated delivery date to find the next one
                shipmentInfos.sort((s1, s2) -> {
                    if (s1.getEstimatedDeliveryDate() == null && s2.getEstimatedDeliveryDate() == null) return 0;
                    if (s1.getEstimatedDeliveryDate() == null) return 1;
                    if (s2.getEstimatedDeliveryDate() == null) return -1;
                    return s1.getEstimatedDeliveryDate().compareTo(s2.getEstimatedDeliveryDate());
                });
                
                ShipmentInfo nextShipment = shipmentInfos.get(0);
                response.setNextDeliveryLocation(nextShipment.getDeliveryCity() + ", " + nextShipment.getDeliveryState());
                response.setNextDeliveryTime(nextShipment.getFormattedEstimatedDelivery());
            }
            
        } catch (Exception e) {
            // Log error but don't fail the entire response
            System.err.println("Error populating shipment info for driver " + response.getDriverId() + ": " + e.getMessage());
        }
    }

    @Override
    public TrackingResponse getTrackingInfo(String trackingNumber) {
        try {
            // In a real implementation, this would query the database
            // For now, return sample data for demonstration
            if ("RC123456789ZA".equals(trackingNumber)) {
                TrackingResponse response = new TrackingResponse();
                response.setTrackingNumber(trackingNumber);
                response.setStatus("out-for-delivery");
                response.setStatusText("Out for Delivery");
                response.setProgress(85);
                response.setPickupLocation("Cape Town, Western Cape");
                response.setDeliveryLocation("Johannesburg, Gauteng");
                response.setEstimatedDelivery("Today by 6:00 PM");
                response.setServiceType("Express Delivery");
                response.setLastUpdated(LocalDateTime.now());
                
                // Create timeline
                List<TrackingResponse.TrackingEvent> timeline = new ArrayList<>();
                
                TrackingResponse.TrackingEvent event1 = new TrackingResponse.TrackingEvent();
                event1.setStatus("pending");
                event1.setTitle("Package Received");
                event1.setDescription("Package received at Cape Town facility");
                event1.setTimestamp(LocalDateTime.now().minusDays(1).withHour(8).withMinute(30));
                event1.setCompleted(true);
                timeline.add(event1);
                
                TrackingResponse.TrackingEvent event2 = new TrackingResponse.TrackingEvent();
                event2.setStatus("processing");
                event2.setTitle("Processing");
                event2.setDescription("Package is being processed and prepared for shipment");
                event2.setTimestamp(LocalDateTime.now().minusDays(1).withHour(9).withMinute(15));
                event2.setCompleted(true);
                timeline.add(event2);
                
                TrackingResponse.TrackingEvent event3 = new TrackingResponse.TrackingEvent();
                event3.setStatus("in-transit");
                event3.setTitle("In Transit");
                event3.setDescription("Package is on its way to Johannesburg");
                event3.setTimestamp(LocalDateTime.now().minusDays(1).withHour(14).withMinute(20));
                event3.setCompleted(true);
                timeline.add(event3);
                
                TrackingResponse.TrackingEvent event4 = new TrackingResponse.TrackingEvent();
                event4.setStatus("out-for-delivery");
                event4.setTitle("Out for Delivery");
                event4.setDescription("Package is out for delivery in your area");
                event4.setTimestamp(LocalDateTime.now().withHour(10).withMinute(45));
                event4.setCompleted(false);
                event4.setCurrent(true);
                timeline.add(event4);
                
                TrackingResponse.TrackingEvent event5 = new TrackingResponse.TrackingEvent();
                event5.setStatus("delivered");
                event5.setTitle("Delivered");
                event5.setDescription("Package has been delivered successfully");
                event5.setTimestamp(null);
                event5.setCompleted(false);
                timeline.add(event5);
                
                response.setTimeline(timeline);
                
                // Create driver info
                TrackingResponse.DriverInfo driver = new TrackingResponse.DriverInfo();
                driver.setName("John Smith");
                driver.setPhone("+27 82 123 4567");
                driver.setVehicle("Toyota Hilux");
                driver.setVehiclePlate("RC123GP");
                driver.setCurrentLocation("Johannesburg, Gauteng");
                driver.setLastLocationUpdate(LocalDateTime.now().minusMinutes(5));
                response.setDriver(driver);
                
                return response;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting tracking info: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getTrackingStatus(String trackingNumber) {
        try {
            TrackingResponse trackingInfo = getTrackingInfo(trackingNumber);
            if (trackingInfo != null) {
                Map<String, Object> status = new HashMap<>();
                status.put("trackingNumber", trackingInfo.getTrackingNumber());
                status.put("status", trackingInfo.getStatus());
                status.put("statusText", trackingInfo.getStatusText());
                status.put("progress", trackingInfo.getProgress());
                status.put("lastUpdated", trackingInfo.getLastUpdated());
                return status;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting tracking status: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getTrackingTimeline(String trackingNumber) {
        try {
            TrackingResponse trackingInfo = getTrackingInfo(trackingNumber);
            if (trackingInfo != null) {
                Map<String, Object> timeline = new HashMap<>();
                timeline.put("trackingNumber", trackingInfo.getTrackingNumber());
                timeline.put("timeline", trackingInfo.getTimeline());
                return timeline;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting tracking timeline: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getDriverInfo(String trackingNumber) {
        try {
            TrackingResponse trackingInfo = getTrackingInfo(trackingNumber);
            if (trackingInfo != null && trackingInfo.getDriver() != null) {
                Map<String, Object> driverInfo = new HashMap<>();
                driverInfo.put("trackingNumber", trackingInfo.getTrackingNumber());
                driverInfo.put("driver", trackingInfo.getDriver());
                return driverInfo;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting driver info: " + e.getMessage());
            return null;
        }
    }
}
