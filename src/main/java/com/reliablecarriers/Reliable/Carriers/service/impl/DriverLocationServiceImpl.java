package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.repository.DriverLocationRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.repository.VehicleRepository;
import com.reliablecarriers.Reliable.Carriers.service.DriverLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DriverLocationServiceImpl implements DriverLocationService {

    private final DriverLocationRepository driverLocationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public DriverLocationServiceImpl(DriverLocationRepository driverLocationRepository, 
                                     UserRepository userRepository,
                                     VehicleRepository vehicleRepository) {
        this.driverLocationRepository = driverLocationRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public DriverLocation createDriverLocation(DriverLocation driverLocation) {
        // Validate driver exists
        User driver = userRepository.findById(driverLocation.getDriver().getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        
        // Validate vehicle exists if provided
        if (driverLocation.getVehicle() != null && driverLocation.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(driverLocation.getVehicle().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
            driverLocation.setVehicle(vehicle);
        }
        
        // Set the driver
        driverLocation.setDriver(driver);
        
        // Set timestamp if not provided
        if (driverLocation.getTimestamp() == null) {
            driverLocation.setTimestamp(new Date());
        }
        
        return driverLocationRepository.save(driverLocation);
    }

    @Override
    public DriverLocation getDriverLocationById(Long id) {
        return driverLocationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver location not found with id: " + id));
    }

    @Override
    public List<DriverLocation> getAllDriverLocations() {
        return driverLocationRepository.findAll();
    }

    @Override
    public DriverLocation getMostRecentDriverLocation(User driver) {
        // Validate driver exists
        userRepository.findById(driver.getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
                
        return driverLocationRepository.findTopByDriverOrderByTimestampDesc(driver);
    }

    @Override
    public List<DriverLocation> getDriverLocationsByDriver(User driver) {
        // Validate driver exists
        userRepository.findById(driver.getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
                
        return driverLocationRepository.findByDriverOrderByTimestampDesc(driver);
    }

    @Override
    public List<DriverLocation> getDriverLocationsByDriverAndTimeRange(User driver, Date startTime, Date endTime) {
        // Validate driver exists
        userRepository.findById(driver.getId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
                
        return driverLocationRepository.findByDriverAndTimestampBetweenOrderByTimestampDesc(driver, startTime, endTime);
    }

    @Override
    public List<DriverLocation> getDriverLocationsByVehicle(Vehicle vehicle) {
        // Validate vehicle exists
        vehicleRepository.findById(vehicle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
                
        return driverLocationRepository.findByVehicleOrderByTimestampDesc(vehicle);
    }

    @Override
    public List<DriverLocation> getDriverLocationsByCityAndState(String city, String state) {
        return driverLocationRepository.findByCityAndStateOrderByTimestampDesc(city, state);
    }

    @Override
    public DriverLocation updateDriverLocation(Long id, DriverLocation driverLocationDetails) {
        DriverLocation driverLocation = getDriverLocationById(id);
        
        // Update fields
        if (driverLocationDetails.getLatitude() != null) {
            driverLocation.setLatitude(driverLocationDetails.getLatitude());
        }
        
        if (driverLocationDetails.getLongitude() != null) {
            driverLocation.setLongitude(driverLocationDetails.getLongitude());
        }
        
        if (driverLocationDetails.getAddress() != null) {
            driverLocation.setAddress(driverLocationDetails.getAddress());
        }
        
        if (driverLocationDetails.getCity() != null) {
            driverLocation.setCity(driverLocationDetails.getCity());
        }
        
        if (driverLocationDetails.getState() != null) {
            driverLocation.setState(driverLocationDetails.getState());
        }
        
        if (driverLocationDetails.getZipCode() != null) {
            driverLocation.setZipCode(driverLocationDetails.getZipCode());
        }
        
        if (driverLocationDetails.getCountry() != null) {
            driverLocation.setCountry(driverLocationDetails.getCountry());
        }
        
        if (driverLocationDetails.getVehicle() != null) {
            Vehicle vehicle = vehicleRepository.findById(driverLocationDetails.getVehicle().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
            driverLocation.setVehicle(vehicle);
        }
        
        if (driverLocationDetails.getNotes() != null) {
            driverLocation.setNotes(driverLocationDetails.getNotes());
        }
        
        if (driverLocationDetails.getTimestamp() != null) {
            driverLocation.setTimestamp(driverLocationDetails.getTimestamp());
        }
        
        return driverLocationRepository.save(driverLocation);
    }

    @Override
    public void deleteDriverLocation(Long id) {
        DriverLocation driverLocation = getDriverLocationById(id);
        driverLocationRepository.delete(driverLocation);
    }
}