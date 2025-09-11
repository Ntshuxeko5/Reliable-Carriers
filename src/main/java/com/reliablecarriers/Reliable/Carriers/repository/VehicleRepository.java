package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.model.VehicleStatus;
import com.reliablecarriers.Reliable.Carriers.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    
    List<Vehicle> findByStatus(VehicleStatus status);
    
    List<Vehicle> findByType(VehicleType type);
    
    List<Vehicle> findByAssignedDriver(User driver);
    
    List<Vehicle> findByMakeAndModel(String make, String model);
    
    List<Vehicle> findByYearBetween(Integer startYear, Integer endYear);
    
    List<Vehicle> findByNextMaintenanceDateBefore(Date date);
    
    List<Vehicle> findByCapacityGreaterThanEqual(Double capacity);
}