package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.DriverResponse;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.DriverLocationRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.repository.VehicleRepository;
import com.reliablecarriers.Reliable.Carriers.service.DriverService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl implements DriverService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverLocationRepository driverLocationRepository;
    private final ShipmentRepository shipmentRepository;

    public DriverServiceImpl(UserRepository userRepository,
                           VehicleRepository vehicleRepository,
                           DriverLocationRepository driverLocationRepository,
                           ShipmentRepository shipmentRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverLocationRepository = driverLocationRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public DriverResponse getDriverById(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        Vehicle vehicle = vehicleRepository.findByAssignedDriver(driver)
                .stream()
                .findFirst()
                .orElse(null);
        
        DriverResponse response = new DriverResponse(driver, vehicle);
        populateDriverStatus(response, driver);
        
        return response;
    }

    @Override
    public List<DriverResponse> getAllDrivers() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByRole(UserRole role) {
        List<User> drivers = userRepository.findByRole(role);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByVehicleMakeAndModel(String make, String model) {
        List<Vehicle> vehicles = vehicleRepository.findByMakeAndModel(make, model);
        return vehicles.stream()
                .map(vehicle -> {
                    if (vehicle.getAssignedDriver() != null) {
                        DriverResponse response = new DriverResponse(vehicle.getAssignedDriver(), vehicle);
                        populateDriverStatus(response, vehicle.getAssignedDriver());
                        return response;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByVehicleType(String vehicleType) {
        try {
            VehicleType type = VehicleType.valueOf(vehicleType.toUpperCase());
            List<Vehicle> vehicles = vehicleRepository.findByType(type);
            return vehicles.stream()
                    .map(vehicle -> {
                        if (vehicle.getAssignedDriver() != null) {
                            DriverResponse response = new DriverResponse(vehicle.getAssignedDriver(), vehicle);
                            populateDriverStatus(response, vehicle.getAssignedDriver());
                            return response;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<DriverResponse> getDriversByLocation(String city, String state) {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .filter(driver -> (city == null || city.equals(driver.getCity())) &&
                                (state == null || state.equals(driver.getState())))
                .map(this::createDriverResponseWithVehicle)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getOnlineDrivers() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(DriverResponse::getIsOnline)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getOfflineDrivers() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> !driver.getIsOnline())
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversWithActivePackages() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> driver.getActivePackages() != null && driver.getActivePackages() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversWithoutVehicles() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(driver -> new DriverResponse(driver, null))
                .filter(driver -> driver.getVehicleId() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> searchDrivers(String searchTerm) {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> 
                    driver.getFirstName().toLowerCase().contains(lowerSearchTerm) ||
                    driver.getLastName().toLowerCase().contains(lowerSearchTerm) ||
                    driver.getEmail().toLowerCase().contains(lowerSearchTerm) ||
                    (driver.getVehicleMake() != null && driver.getVehicleMake().toLowerCase().contains(lowerSearchTerm)) ||
                    (driver.getVehicleModel() != null && driver.getVehicleModel().toLowerCase().contains(lowerSearchTerm)) ||
                    (driver.getVehicleRegistrationNumber() != null && driver.getVehicleRegistrationNumber().toLowerCase().contains(lowerSearchTerm))
                )
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDriverStatistics() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        List<DriverResponse> driverResponses = drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .collect(Collectors.toList());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDrivers", (long) drivers.size());
        stats.put("onlineDrivers", driverResponses.stream().filter(DriverResponse::getIsOnline).count());
        stats.put("offlineDrivers", driverResponses.stream().filter(driver -> !driver.getIsOnline()).count());
        stats.put("driversWithVehicles", driverResponses.stream().filter(driver -> driver.getVehicleId() != null).count());
        stats.put("driversWithoutVehicles", driverResponses.stream().filter(driver -> driver.getVehicleId() == null).count());
        stats.put("driversWithActivePackages", driverResponses.stream().filter(driver -> driver.getActivePackages() != null && driver.getActivePackages() > 0).count());
        
        return stats;
    }

    @Override
    public Map<String, Object> getDriverPerformanceMetrics(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        List<Shipment> shipments = shipmentRepository.findByAssignedDriver(driver);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalShipments", (long) shipments.size());
        metrics.put("deliveredShipments", shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.DELIVERED).count());
        metrics.put("pendingShipments", shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.PENDING).count());
        metrics.put("inTransitShipments", shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT).count());
        
        // Calculate average delivery time for delivered shipments
        double avgDeliveryTime = shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED && s.getActualDeliveryDate() != null && s.getCreatedAt() != null)
                .mapToLong(s -> s.getActualDeliveryDate().getTime() - s.getCreatedAt().getTime())
                .average()
                .orElse(0.0);
        
        metrics.put("averageDeliveryTimeHours", avgDeliveryTime / (1000 * 60 * 60));
        
        return metrics;
    }

    @Override
    public DriverResponse getDriverByVehicleRegistration(String registrationNumber) {
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(registrationNumber)
                .orElse(null);
        
        if (vehicle != null && vehicle.getAssignedDriver() != null) {
            DriverResponse response = new DriverResponse(vehicle.getAssignedDriver(), vehicle);
            populateDriverStatus(response, vehicle.getAssignedDriver());
            return response;
        }
        
        return null;
    }

    @Override
    public List<DriverResponse> getDriversWithVehiclesDueForMaintenance() {
        Date today = new Date();
        List<Vehicle> vehicles = vehicleRepository.findByNextMaintenanceDateBefore(today);
        
        return vehicles.stream()
                .map(vehicle -> {
                    if (vehicle.getAssignedDriver() != null) {
                        DriverResponse response = new DriverResponse(vehicle.getAssignedDriver(), vehicle);
                        populateDriverStatus(response, vehicle.getAssignedDriver());
                        return response;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByVehicleCapacityRange(Double minCapacity, Double maxCapacity) {
        List<Vehicle> vehicles = vehicleRepository.findByCapacityGreaterThanEqual(minCapacity);
        return vehicles.stream()
                .filter(vehicle -> vehicle.getCapacity() <= maxCapacity)
                .map(vehicle -> {
                    if (vehicle.getAssignedDriver() != null) {
                        DriverResponse response = new DriverResponse(vehicle.getAssignedDriver(), vehicle);
                        populateDriverStatus(response, vehicle.getAssignedDriver());
                        return response;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByVehicleYearRange(Integer startYear, Integer endYear) {
        List<Vehicle> vehicles = vehicleRepository.findByYearBetween(startYear, endYear);
        return vehicles.stream()
                .map(vehicle -> {
                    if (vehicle.getAssignedDriver() != null) {
                        DriverResponse response = new DriverResponse(vehicle.getAssignedDriver(), vehicle);
                        populateDriverStatus(response, vehicle.getAssignedDriver());
                        return response;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByVehicleFuelType(String fuelType) {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> fuelType.equals(driver.getVehicleFuelType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByVehicleColor(String color) {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> color.equals(driver.getVehicleColor()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverResponse> getDriversByVehicleFeatures(String... features) {
        // This is a simplified implementation - in a real system, you might have a separate table for vehicle features
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> driver.getVehicleId() != null) // Has a vehicle assigned
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getDriverCountByVehicleMake() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> driver.getVehicleMake() != null)
                .collect(Collectors.groupingBy(
                    DriverResponse::getVehicleMake,
                    Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getDriverCountByVehicleType() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .filter(driver -> driver.getVehicleType() != null)
                .collect(Collectors.groupingBy(
                    DriverResponse::getVehicleType,
                    Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getDriverCountByLocation() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        return drivers.stream()
                .filter(driver -> driver.getCity() != null && driver.getState() != null)
                .collect(Collectors.groupingBy(
                    driver -> driver.getCity() + ", " + driver.getState(),
                    Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getDriverCountByStatus() {
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
        List<DriverResponse> driverResponses = drivers.stream()
                .map(this::createDriverResponseWithVehicle)
                .collect(Collectors.toList());
        
        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("ONLINE", driverResponses.stream().filter(DriverResponse::getIsOnline).count());
        statusCount.put("OFFLINE", driverResponses.stream().filter(driver -> !driver.getIsOnline()).count());
        statusCount.put("WITH_VEHICLE", driverResponses.stream().filter(driver -> driver.getVehicleId() != null).count());
        statusCount.put("WITHOUT_VEHICLE", driverResponses.stream().filter(driver -> driver.getVehicleId() == null).count());
        statusCount.put("WITH_ACTIVE_PACKAGES", driverResponses.stream().filter(driver -> driver.getActivePackages() != null && driver.getActivePackages() > 0).count());
        
        return statusCount;
    }

    // Helper methods
    private DriverResponse createDriverResponseWithVehicle(User driver) {
        Vehicle vehicle = vehicleRepository.findByAssignedDriver(driver)
                .stream()
                .findFirst()
                .orElse(null);
        
        DriverResponse response = new DriverResponse(driver, vehicle);
        populateDriverStatus(response, driver);
        
        return response;
    }

    private void populateDriverStatus(DriverResponse response, User driver) {
        // Check if driver is online (has recent location update)
        DriverLocation lastLocation = driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver.getId());
        boolean isOnline = lastLocation != null && isLocationRecent(lastLocation.getTimestamp());
        response.setIsOnline(isOnline);
        response.setCurrentStatus(isOnline ? "ACTIVE" : "OFFLINE");
        
        if (lastLocation != null) {
            response.setCurrentLocation(lastLocation.getAddress() + ", " + lastLocation.getCity() + ", " + lastLocation.getState());
            response.setLastLocationUpdate(java.sql.Timestamp.valueOf(lastLocation.getTimestamp()));
        }
        
        // Count active packages
        List<Shipment> activeShipments = shipmentRepository.findByAssignedDriver(driver);
        long activeCount = activeShipments.stream()
                .filter(shipment -> shipment.getStatus() != ShipmentStatus.DELIVERED && 
                                  shipment.getStatus() != ShipmentStatus.CANCELLED)
                .count();
        response.setActivePackages((int) activeCount);
        
        // Calculate total weight carrying
        double totalWeight = activeShipments.stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.IN_TRANSIT || 
                                  shipment.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                .mapToDouble(Shipment::getWeight)
                .sum();
        response.setTotalWeightCarrying(totalWeight);
    }

    private boolean isLocationRecent(LocalDateTime timestamp) {
        if (timestamp == null) return false;
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return timestamp.isAfter(fiveMinutesAgo);
    }
}
