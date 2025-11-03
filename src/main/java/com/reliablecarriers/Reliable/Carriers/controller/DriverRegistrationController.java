package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.RegisterRequest;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * Driver Registration Controller
 * Handles driver self-registration with document upload
 */
@RestController
@RequestMapping("/api/driver/register")
@CrossOrigin(origins = "*")
public class DriverRegistrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverRegistrationController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    
    /**
     * Driver self-registration endpoint
     * POST /api/driver/register
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerDriver(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Driver registration request received for: " + registerRequest.getEmail());
            
            // Ensure role is DRIVER
            registerRequest.setRole(UserRole.DRIVER);
            
            // Validate required driver fields
            if (registerRequest.getDriverLicenseNumber() == null || registerRequest.getDriverLicenseNumber().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Driver license number is required"));
            }
            
            if (registerRequest.getVehicleMake() == null || registerRequest.getVehicleMake().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Vehicle make is required"));
            }
            
            if (registerRequest.getVehicleRegistration() == null || registerRequest.getVehicleRegistration().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Vehicle registration number is required"));
            }
            
            // Check if email already exists
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Email already registered"));
            }
            
            // Create driver user
            User driver = new User();
            driver.setFirstName(registerRequest.getFirstName());
            driver.setLastName(registerRequest.getLastName());
            driver.setEmail(registerRequest.getEmail());
            driver.setPassword(registerRequest.getPassword());
            driver.setPhone(registerRequest.getPhone());
            driver.setAddress(registerRequest.getAddress());
            driver.setCity(registerRequest.getCity());
            driver.setState(registerRequest.getState());
            driver.setZipCode(registerRequest.getZipCode());
            driver.setCountry(registerRequest.getCountry() != null ? registerRequest.getCountry() : "South Africa");
            driver.setRole(UserRole.DRIVER);
            driver.setWaiverAccepted(Boolean.TRUE.equals(registerRequest.getWaiverAccepted()));
            if (Boolean.TRUE.equals(registerRequest.getWaiverAccepted())) {
                driver.setWaiverAcceptedAt(new java.util.Date());
            }
            
            // Set driver-specific fields
            driver.setDriverLicenseNumber(registerRequest.getDriverLicenseNumber());
            driver.setLicenseExpiryDate(registerRequest.getLicenseExpiryDate());
            driver.setVehicleMake(registerRequest.getVehicleMake());
            driver.setVehicleModel(registerRequest.getVehicleModel());
            driver.setVehicleYear(registerRequest.getVehicleYear());
            driver.setVehicleRegistration(registerRequest.getVehicleRegistration());
            driver.setVehicleColor(registerRequest.getVehicleColor());
            driver.setVehicleCapacityKg(registerRequest.getVehicleCapacityKg());
            
            // Set driver verification status to PENDING
            driver.setDriverVerificationStatus(
                com.reliablecarriers.Reliable.Carriers.model.DriverVerificationStatus.PENDING
            );
            
            // Initialize driver stats
            driver.setIsOnline(false);
            driver.setTotalDeliveries(0);
            driver.setTotalEarnings(java.math.BigDecimal.ZERO);
            driver.setDriverRating(null);
            
            // Register the driver
            User registeredDriver = authService.registerUser(driver);
            
            logger.info("Driver registered successfully: " + registeredDriver.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Driver account created successfully. Please upload required documents for verification.",
                "requires2fa", false,
                "userId", registeredDriver.getId()
            ));
            
        } catch (Exception e) {
            logger.error("Driver registration error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}

