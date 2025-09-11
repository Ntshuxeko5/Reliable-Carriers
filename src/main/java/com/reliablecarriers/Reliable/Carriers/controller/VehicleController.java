package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.model.VehicleStatus;
import com.reliablecarriers.Reliable.Carriers.model.VehicleType;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import com.reliablecarriers.Reliable.Carriers.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserService userService;

    @Autowired
    public VehicleController(VehicleService vehicleService, UserService userService) {
        this.vehicleService = vehicleService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        return new ResponseEntity<>(createdVehicle, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicle);
        return ResponseEntity.ok(updatedVehicle);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/registration/{registrationNumber}")
    public ResponseEntity<Vehicle> getVehicleByRegistrationNumber(@PathVariable String registrationNumber) {
        Vehicle vehicle = vehicleService.getVehicleByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Vehicle>> getVehiclesByStatus(@PathVariable VehicleStatus status) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByStatus(status);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Vehicle>> getVehiclesByType(@PathVariable VehicleType type) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByType(type);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByDriver(@PathVariable Long driverId) {
        User driver = userService.getUserById(driverId);
        List<Vehicle> vehicles = vehicleService.getVehiclesByDriver(driver);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/make-model")
    public ResponseEntity<List<Vehicle>> getVehiclesByMakeAndModel(
            @RequestParam String make, @RequestParam String model) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByMakeAndModel(make, model);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/year-range")
    public ResponseEntity<List<Vehicle>> getVehiclesByYearRange(
            @RequestParam Integer startYear, @RequestParam Integer endYear) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByYearRange(startYear, endYear);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/maintenance-due")
    public ResponseEntity<List<Vehicle>> getVehiclesByNextMaintenanceDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByNextMaintenanceDate(date);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/capacity")
    public ResponseEntity<List<Vehicle>> getVehiclesByCapacityGreaterThan(
            @RequestParam Double capacity) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByCapacityGreaterThan(capacity);
        return ResponseEntity.ok(vehicles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{vehicleId}/assign-driver/{driverId}")
    public ResponseEntity<Vehicle> assignDriverToVehicle(
            @PathVariable Long vehicleId, @PathVariable Long driverId) {
        Vehicle vehicle = vehicleService.assignDriverToVehicle(vehicleId, driverId);
        return ResponseEntity.ok(vehicle);
    }

    @PutMapping("/{vehicleId}/update-status")
    public ResponseEntity<Vehicle> updateVehicleStatus(
            @PathVariable Long vehicleId, @RequestParam VehicleStatus status) {
        Vehicle vehicle = vehicleService.updateVehicleStatus(vehicleId, status);
        return ResponseEntity.ok(vehicle);
    }

    @PutMapping("/{vehicleId}/schedule-maintenance")
    public ResponseEntity<Vehicle> scheduleVehicleMaintenance(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date maintenanceDate,
            @RequestParam(required = false) String notes) {
        Vehicle vehicle = vehicleService.scheduleVehicleMaintenance(vehicleId, maintenanceDate, notes);
        return ResponseEntity.ok(vehicle);
    }
}