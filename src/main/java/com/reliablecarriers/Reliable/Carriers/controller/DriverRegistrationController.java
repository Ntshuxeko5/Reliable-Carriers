package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.RegisterRequest;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.UserValidationService;
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
    
    @Autowired
    private UserValidationService userValidationService;
    
    @Autowired(required = false)
    private com.reliablecarriers.Reliable.Carriers.service.TwoFactorService twoFactorService;
    
    
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
            
            // Validate registration data
            try {
                Map<String, Object> validation = userValidationService.validateRegistration(
                    registerRequest.getFirstName(),
                    registerRequest.getLastName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getConfirmPassword(),
                    registerRequest.getPhone()
                );
                
                if (!(Boolean) validation.get("valid")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> errors = (Map<String, String>) validation.get("errors");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Validation failed", "errors", errors));
                }
            } catch (Exception validationError) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Validation error: " + validationError.getMessage()));
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
            
            // Set emailVerified to false - driver must verify email before login
            driver.setEmailVerified(false);
            driver.setIsActive(true); // Account is active but not verified
            
            // Register the driver
            User registeredDriver = authService.registerUser(driver);
            
            logger.info("Driver registered successfully: " + registeredDriver.getEmail());
            
            // Implement 2FA for driver registration (same as customer registration)
            try {
                logger.info("Generating 2FA token for driver registration...");
                
                // Generate and send 2FA code via email
                if (twoFactorService != null) {
                    try {
                        twoFactorService.generateAndSendToken(registeredDriver, "EMAIL");
                        logger.info("2FA code sent to driver email: " + registeredDriver.getEmail());
                    } catch (Exception e) {
                        logger.error("Failed to send 2FA code via email: " + e.getMessage(), e);
                        // Continue with demo code fallback
                    }
                }
                
                // Generate a demo code for development/testing
                String demoCode = String.format("%06d", new java.util.Random().nextInt(1000000));
                logger.info("Demo 2FA code for driver " + registeredDriver.getEmail() + ": " + demoCode);
                
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true,
                        "requires2fa", true,
                        "message", "Driver account created! Verification code sent to your email. Please check your inbox.",
                        "email", registeredDriver.getEmail(),
                        "role", registeredDriver.getRole().toString(),
                        "demoCode", demoCode  // Only for demo - remove in production
                    ));
            } catch (Exception e) {
                logger.error("2FA generation error: " + e.getMessage(), e);
                // If 2FA fails, still return success but indicate 2FA is required
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true,
                        "requires2fa", true,
                        "message", "Driver account created! Please verify your email.",
                        "email", registeredDriver.getEmail(),
                        "role", registeredDriver.getRole().toString()
                    ));
            }
            
        } catch (Exception e) {
            logger.error("Driver registration error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}

