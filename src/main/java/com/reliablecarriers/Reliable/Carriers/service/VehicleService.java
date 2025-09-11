package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.model.VehicleStatus;
import com.reliablecarriers.Reliable.Carriers.model.VehicleType;

import java.util.Date;
import java.util.List;

public interface VehicleService {
    
    Vehicle createVehicle(Vehicle vehicle);
    
    Vehicle updateVehicle(Long id, Vehicle vehicle);
    
    Vehicle getVehicleById(Long id);
    
    Vehicle getVehicleByRegistrationNumber(String registrationNumber);
    
    List<Vehicle> getAllVehicles();
    
    List<Vehicle> getVehiclesByStatus(VehicleStatus status);
    
    List<Vehicle> getVehiclesByType(VehicleType type);
    
    List<Vehicle> getVehiclesByDriver(User driver);
    
    List<Vehicle> getVehiclesByMakeAndModel(String make, String model);
    
    List<Vehicle> getVehiclesByYearRange(int startYear, int endYear);
    
    List<Vehicle> getVehiclesByNextMaintenanceDate(Date date);
    
    List<Vehicle> getVehiclesByCapacityGreaterThan(double capacity);
    
    void deleteVehicle(Long id);
    
    Vehicle assignDriverToVehicle(Long vehicleId, Long driverId);
    
    Vehicle updateVehicleStatus(Long vehicleId, VehicleStatus status);
    
    Vehicle scheduleVehicleMaintenance(Long vehicleId, Date maintenanceDate, String notes);
}