package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.DriverLocationRequest;
import com.reliablecarriers.Reliable.Carriers.dto.DriverLocationResponse;
import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.DriverLocationService;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import com.reliablecarriers.Reliable.Carriers.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DriverLocationController {

    private final DriverLocationService driverLocationService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final AuthService authService;

    @Autowired
    public DriverLocationController(DriverLocationService driverLocationService,
                                   UserService userService,
                                   VehicleService vehicleService,
                                   AuthService authService) {
        this.driverLocationService = driverLocationService;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.authService = authService;
    }

    // Create a new driver location entry
    @PostMapping("/driver/location")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<DriverLocationResponse> createDriverLocation(@RequestBody DriverLocationRequest request) {
        // Get the current authenticated user if no driver ID is provided
        User driver;
        if (request.getDriverId() == null) {
            driver = authService.getCurrentUser();
        } else {
            driver = userService.getUserById(request.getDriverId());
        }

        // Create a new driver location entity
        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setDriver(driver);
        driverLocation.setLatitude(request.getLatitude());
        driverLocation.setLongitude(request.getLongitude());
        driverLocation.setAddress(request.getAddress());
        driverLocation.setCity(request.getCity());
        driverLocation.setState(request.getState());
        driverLocation.setZipCode(request.getZipCode());
        driverLocation.setCountry(request.getCountry());
        driverLocation.setNotes(request.getNotes());
        
        // Set vehicle if provided
        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleService.getVehicleById(request.getVehicleId());
            driverLocation.setVehicle(vehicle);
        }
        
        // Set timestamp if provided, otherwise it will be set in the service
        if (request.getTimestamp() != null) {
            driverLocation.setTimestamp(request.getTimestamp());
        }

        // Save the driver location
        DriverLocation savedLocation = driverLocationService.createDriverLocation(driverLocation);
        
        // Return the response
        return new ResponseEntity<>(new DriverLocationResponse(savedLocation), HttpStatus.CREATED);
    }

    // Get a specific driver location by ID
    @GetMapping("/driver/location/{id}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<DriverLocationResponse> getDriverLocationById(@PathVariable Long id) {
        DriverLocation driverLocation = driverLocationService.getDriverLocationById(id);
        return ResponseEntity.ok(new DriverLocationResponse(driverLocation));
    }

    // Get all driver locations (admin only)
    @GetMapping("/admin/driver/locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverLocationResponse>> getAllDriverLocations() {
        List<DriverLocation> locations = driverLocationService.getAllDriverLocations();
        List<DriverLocationResponse> responses = locations.stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Get the most recent location for a specific driver
    @GetMapping("/driver/location/recent/{driverId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN') or (hasRole('DRIVER') and @authService.isCurrentUser(#driverId))")
    public ResponseEntity<DriverLocationResponse> getMostRecentDriverLocation(@PathVariable Long driverId) {
        User driver = userService.getUserById(driverId);
        DriverLocation location = driverLocationService.getMostRecentDriverLocation(driver);
        
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new DriverLocationResponse(location));
    }

    // Get all locations for a specific driver
    @GetMapping("/driver/locations/{driverId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN') or (hasRole('DRIVER') and @authService.isCurrentUser(#driverId))")
    public ResponseEntity<List<DriverLocationResponse>> getDriverLocationsByDriver(@PathVariable Long driverId) {
        User driver = userService.getUserById(driverId);
        List<DriverLocation> locations = driverLocationService.getDriverLocationsByDriver(driver);
        List<DriverLocationResponse> responses = locations.stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Get locations for a specific driver within a time range
    @GetMapping("/driver/locations/{driverId}/timerange")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN') or (hasRole('DRIVER') and @authService.isCurrentUser(#driverId))")
    public ResponseEntity<List<DriverLocationResponse>> getDriverLocationsByDriverAndTimeRange(
            @PathVariable Long driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        
        User driver = userService.getUserById(driverId);
        List<DriverLocation> locations = driverLocationService.getDriverLocationsByDriverAndTimeRange(driver, startTime, endTime);
        List<DriverLocationResponse> responses = locations.stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Get locations for a specific vehicle
    @GetMapping("/vehicle/locations/{vehicleId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<List<DriverLocationResponse>> getDriverLocationsByVehicle(@PathVariable Long vehicleId) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        List<DriverLocation> locations = driverLocationService.getDriverLocationsByVehicle(vehicle);
        List<DriverLocationResponse> responses = locations.stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Get locations within a geographic area (by city and state)
    @GetMapping("/driver/locations/area")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<List<DriverLocationResponse>> getDriverLocationsByCityAndState(
            @RequestParam String city,
            @RequestParam String state) {
        
        List<DriverLocation> locations = driverLocationService.getDriverLocationsByCityAndState(city, state);
        List<DriverLocationResponse> responses = locations.stream()
                .map(DriverLocationResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Update a driver location
    @PutMapping("/driver/location/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverLocationResponse> updateDriverLocation(
            @PathVariable Long id,
            @RequestBody DriverLocationRequest request) {
        
        // Create a driver location object with the updated fields
        DriverLocation driverLocationDetails = new DriverLocation();
        
        if (request.getDriverId() != null) {
            User driver = userService.getUserById(request.getDriverId());
            driverLocationDetails.setDriver(driver);
        }
        
        driverLocationDetails.setLatitude(request.getLatitude());
        driverLocationDetails.setLongitude(request.getLongitude());
        driverLocationDetails.setAddress(request.getAddress());
        driverLocationDetails.setCity(request.getCity());
        driverLocationDetails.setState(request.getState());
        driverLocationDetails.setZipCode(request.getZipCode());
        driverLocationDetails.setCountry(request.getCountry());
        driverLocationDetails.setNotes(request.getNotes());
        
        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleService.getVehicleById(request.getVehicleId());
            driverLocationDetails.setVehicle(vehicle);
        }
        
        driverLocationDetails.setTimestamp(request.getTimestamp());
        
        // Update the driver location
        DriverLocation updatedLocation = driverLocationService.updateDriverLocation(id, driverLocationDetails);
        
        return ResponseEntity.ok(new DriverLocationResponse(updatedLocation));
    }

    // Delete a driver location
    @DeleteMapping("/driver/location/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDriverLocation(@PathVariable Long id) {
        driverLocationService.deleteDriverLocation(id);
        return ResponseEntity.noContent().build();
    }
}