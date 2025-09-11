package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.model.VehicleStatus;
import com.reliablecarriers.Reliable.Carriers.model.VehicleType;
import com.reliablecarriers.Reliable.Carriers.repository.VehicleRepository;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import com.reliablecarriers.Reliable.Carriers.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    @Autowired
    public VehicleServiceImpl(VehicleRepository vehicleRepository, UserService userService) {
        this.vehicleRepository = vehicleRepository;
        this.userService = userService;
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        // Check if registration number already exists
        if (vehicleRepository.findByRegistrationNumber(vehicle.getRegistrationNumber()).isPresent()) {
            throw new RuntimeException("Vehicle with registration number " + vehicle.getRegistrationNumber() + " already exists");
        }
        
        // Set default status if not provided
        if (vehicle.getStatus() == null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }
        
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle updateVehicle(Long id, Vehicle vehicle) {
        Vehicle existingVehicle = getVehicleById(id);
        
        // Check if registration number is being changed and if it already exists
        if (!existingVehicle.getRegistrationNumber().equals(vehicle.getRegistrationNumber()) &&
            vehicleRepository.findByRegistrationNumber(vehicle.getRegistrationNumber()).isPresent()) {
            throw new RuntimeException("Vehicle with registration number " + vehicle.getRegistrationNumber() + " already exists");
        }
        
        // Update fields
        existingVehicle.setRegistrationNumber(vehicle.getRegistrationNumber());
        existingVehicle.setMake(vehicle.getMake());
        existingVehicle.setModel(vehicle.getModel());
        existingVehicle.setYear(vehicle.getYear());
        existingVehicle.setType(vehicle.getType());
        existingVehicle.setCapacity(vehicle.getCapacity());
        existingVehicle.setColor(vehicle.getColor());
        existingVehicle.setFuelType(vehicle.getFuelType());
        existingVehicle.setMileage(vehicle.getMileage());
        existingVehicle.setNotes(vehicle.getNotes());
        existingVehicle.setLastMaintenanceDate(vehicle.getLastMaintenanceDate());
        existingVehicle.setNextMaintenanceDate(vehicle.getNextMaintenanceDate());
        
        // Don't update status or assigned driver here
        // Those should be updated through specific methods
        
        return vehicleRepository.save(existingVehicle);
    }

    @Override
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }

    @Override
    public Vehicle getVehicleByRegistrationNumber(String registrationNumber) {
        return vehicleRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with registration number: " + registrationNumber));
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<Vehicle> getVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status);
    }

    @Override
    public List<Vehicle> getVehiclesByType(VehicleType type) {
        return vehicleRepository.findByType(type);
    }

    @Override
    public List<Vehicle> getVehiclesByDriver(User driver) {
        return vehicleRepository.findByAssignedDriver(driver);
    }

    @Override
    public List<Vehicle> getVehiclesByMakeAndModel(String make, String model) {
        return vehicleRepository.findByMakeAndModel(make, model);
    }

    @Override
    public List<Vehicle> getVehiclesByYearRange(int startYear, int endYear) {
        return vehicleRepository.findByYearBetween(startYear, endYear);
    }

    @Override
    public List<Vehicle> getVehiclesByNextMaintenanceDate(Date date) {
        return vehicleRepository.findByNextMaintenanceDateBefore(date);
    }

    @Override
    public List<Vehicle> getVehiclesByCapacityGreaterThan(double capacity) {
        return vehicleRepository.findByCapacityGreaterThanEqual(capacity);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleById(id);
        vehicleRepository.delete(vehicle);
    }

    @Override
    public Vehicle assignDriverToVehicle(Long vehicleId, Long driverId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        User driver = userService.getUserById(driverId);
        
        vehicle.setAssignedDriver(driver);
        vehicle.setStatus(VehicleStatus.IN_USE);
        
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle updateVehicleStatus(Long vehicleId, VehicleStatus status) {
        Vehicle vehicle = getVehicleById(vehicleId);
        
        vehicle.setStatus(status);
        
        // If vehicle is set to maintenance or out of service, remove driver assignment
        if (status == VehicleStatus.MAINTENANCE || status == VehicleStatus.OUT_OF_SERVICE) {
            vehicle.setAssignedDriver(null);
        }
        
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle scheduleVehicleMaintenance(Long vehicleId, Date maintenanceDate, String notes) {
        Vehicle vehicle = getVehicleById(vehicleId);
        
        vehicle.setNextMaintenanceDate(maintenanceDate);
        
        // Update notes if provided
        if (notes != null && !notes.isEmpty()) {
            String updatedNotes = vehicle.getNotes() != null ? 
                    vehicle.getNotes() + "\n" + notes : notes;
            vehicle.setNotes(updatedNotes);
        }
        
        return vehicleRepository.save(vehicle);
    }
}