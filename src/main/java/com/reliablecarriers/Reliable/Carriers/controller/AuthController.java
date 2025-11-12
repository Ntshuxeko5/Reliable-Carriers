package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.config.JwtTokenUtil;
import com.reliablecarriers.Reliable.Carriers.dto.AuthRequest;
import com.reliablecarriers.Reliable.Carriers.dto.AuthResponse;
import com.reliablecarriers.Reliable.Carriers.dto.RegisterRequest;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.UserValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.reliablecarriers.Reliable.Carriers.dto.ForgotPasswordRequest;
import com.reliablecarriers.Reliable.Carriers.dto.ResetPasswordRequest;
import com.reliablecarriers.Reliable.Carriers.model.PasswordResetToken;
import com.reliablecarriers.Reliable.Carriers.repository.PasswordResetTokenRepository;
import com.reliablecarriers.Reliable.Carriers.repository.TwoFactorTokenRepository;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.AccountLockoutService;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TwoFactorTokenRepository twoFactorTokenRepository;
    private final EmailService emailService;
    private final com.reliablecarriers.Reliable.Carriers.service.TotpService totpService;
    private final com.reliablecarriers.Reliable.Carriers.service.TwoFactorService twoFactorService;
    private final UserValidationService userValidationService;
    private final AccountLockoutService accountLockoutService;

    public AuthController(AuthService authService, JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService, UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, TwoFactorTokenRepository twoFactorTokenRepository, EmailService emailService, com.reliablecarriers.Reliable.Carriers.service.TotpService totpService, com.reliablecarriers.Reliable.Carriers.service.TwoFactorService twoFactorService, UserValidationService userValidationService, AccountLockoutService accountLockoutService) {
        this.authService = authService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.twoFactorTokenRepository = twoFactorTokenRepository;
        this.emailService = emailService;
        this.totpService = totpService;
        this.twoFactorService = twoFactorService;
        this.userValidationService = userValidationService;
        this.accountLockoutService = accountLockoutService;
    }

    // Endpoint to enable TOTP for a user (after login or from profile)
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2fa(@RequestParam String identifier) {
        try {
            User user = userRepository.findByEmail(identifier).orElse(null);
            if (user == null) user = userRepository.findByPhone(identifier).orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

            // generate secret
            String secret = totpService.createSecret();
            user.setTotpSecret(secret);
            user.setTotpEnabled(true);
            userRepository.save(user);

            // Generate QR code URL (using Google Charts API for simplicity)
            String appName = "Reliable Carriers";
            String qrCodeUrl = String.format("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=otpauth://totp/%s:%s?secret=%s&issuer=%s",
                appName.replace(" ", "%20"),
                user.getEmail().replace("@", "%40"),
                secret,
                appName.replace(" ", "%20"));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "secret", secret,
                "qrCodeUrl", qrCodeUrl,
                "manualEntryKey", secret
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Error setting up 2FA: " + e.getMessage()));
        }
    }

    // Endpoint to disable 2FA
    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2fa(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "User not authenticated"));
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            user.setTotpEnabled(false);
            user.setTotpSecret(null);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("success", true, "message", "2FA has been disabled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Error disabling 2FA: " + e.getMessage()));
        }
    }

    // Endpoint to get 2FA status
    @GetMapping("/2fa/status")
    public ResponseEntity<?> get2faStatus(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "User not authenticated"));
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            boolean enabled = Boolean.TRUE.equals(user.getTotpEnabled());
            boolean hasPhone = user.getPhone() != null && !user.getPhone().isEmpty();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "totpEnabled", enabled,
                "hasPhone", hasPhone,
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Error getting 2FA status: " + e.getMessage()));
        }
    }

    // Verify TOTP code during login second step
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2fa(@RequestParam String identifier, @RequestParam int code) {
        try {
            User user = userRepository.findByEmail(identifier).orElse(null);
            if (user == null) user = userRepository.findByPhone(identifier).orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            if (user.getTotpSecret() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("2FA not enabled for user");

            boolean ok = totpService.authorize(user.getTotpSecret(), code);
            if (!ok) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA code");

            // success — generate JWT and return user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Set authentication in SecurityContext
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Also store user info in session for server-rendered pages
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userFirstName", user.getFirstName());
            session.setAttribute("userLastName", user.getLastName());
            session.setAttribute("userPhone", user.getPhone());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole().toString());
            session.setAttribute("isAuthenticated", true);
            
            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole(),
                    token
            );
            
            // Check if user needs approval
            boolean needsApproval = false;
            if (user.getRole() == UserRole.DRIVER) {
                needsApproval = user.getDriverVerificationStatus() == null || 
                    user.getDriverVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.DriverVerificationStatus.PENDING;
            } else if (Boolean.TRUE.equals(user.getIsBusiness())) {
                needsApproval = user.getBusinessVerificationStatus() == null || 
                    user.getBusinessVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.BusinessVerificationStatus.PENDING;
            }
            
            return ResponseEntity.ok(Map.of(
                "user", response,
                "needsApproval", needsApproval
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying 2FA: " + e.getMessage());
        }
    }

    // Request a one-time token sent via EMAIL or SMS (for customers who prefer method-based 2FA)
    @PostMapping("/2fa/request")
    public ResponseEntity<?> request2fa(@RequestParam String identifier, @RequestParam(defaultValue = "EMAIL") String method) {
        try {
            User user = userRepository.findByEmail(identifier).orElse(null);
            if (user == null) user = userRepository.findByPhone(identifier).orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

            // Only enforce for customers (not staff/admin/oauth logins) — leaving role checks flexible for now
            twoFactorService.generateAndSendToken(user, method);

            return ResponseEntity.ok(Map.of("sent", true, "method", method));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error requesting 2FA token: " + e.getMessage());
        }
    }

    // Verify a method-based 2FA token (EMAIL/SMS)
    @PostMapping("/2fa/verify-method")
    public ResponseEntity<?> verify2faMethod(@RequestParam String identifier, @RequestParam String token) {
        try {
            User user = userRepository.findByEmail(identifier).orElse(null);
            if (user == null) user = userRepository.findByPhone(identifier).orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

            boolean ok = twoFactorService.verifyToken(user, token);
            if (!ok) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");

            // success — generate JWT and return user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            final String tokenJwt = jwtTokenUtil.generateToken(userDetails);
            
            // Set authentication in SecurityContext
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Also store user info in session for server-rendered pages
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userFirstName", user.getFirstName());
            session.setAttribute("userLastName", user.getLastName());
            session.setAttribute("userPhone", user.getPhone());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole().toString());
            session.setAttribute("isAuthenticated", true);
            
            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole(),
                    tokenJwt
            );
            
            // Check if user needs approval
            boolean needsApproval = false;
            if (user.getRole() == UserRole.DRIVER) {
                needsApproval = user.getDriverVerificationStatus() == null || 
                    user.getDriverVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.DriverVerificationStatus.PENDING;
            } else if (Boolean.TRUE.equals(user.getIsBusiness())) {
                needsApproval = user.getBusinessVerificationStatus() == null || 
                    user.getBusinessVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.BusinessVerificationStatus.PENDING;
            }
            
            return ResponseEntity.ok(Map.of(
                "user", response,
                "needsApproval", needsApproval
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying 2FA token: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            if (registerRequest == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Registration request is required"));
            }
            
            logger.debug("Registration request received for email: {}", registerRequest.getEmail());
            logger.debug("Registration request - Email: {}, Role: {}", registerRequest.getEmail(), registerRequest.getRole());
            
            // Restrict tracking manager role creation - only admins can create these accounts
            // Drivers can self-register via /api/driver/register
            if (registerRequest.getRole() == UserRole.TRACKING_MANAGER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Tracking Manager accounts can only be created by administrators. Please contact support."));
            }
            
            // Set default role to CUSTOMER if not specified
            if (registerRequest.getRole() == null) {
                registerRequest.setRole(UserRole.CUSTOMER);
            }

            // Validate registration data
            try {
                Map<String, Object> validation = userValidationService.validateRegistration(
                    registerRequest.getFirstName(),
                    registerRequest.getLastName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getConfirmPassword(), // Use actual confirm password
                    registerRequest.getPhone()
                );

                if (!(Boolean) validation.get("valid")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> errors = (Map<String, String>) validation.get("errors");
                    logger.info("Validation failed: " + errors);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Validation failed", "errors", errors));
                }
            } catch (Exception validationError) {
                logger.error("Validation service error: " + validationError.getMessage(), validationError);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Validation error: " + validationError.getMessage()));
            }

            // Create a new user from the request
            User user = new User();
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            user.setPhone(registerRequest.getPhone());
            user.setAddress(registerRequest.getAddress());
            user.setCity(registerRequest.getCity());
            user.setState(registerRequest.getState());
            user.setZipCode(registerRequest.getZipCode());
            user.setCountry(registerRequest.getCountry());
            user.setWaiverAccepted(Boolean.TRUE.equals(registerRequest.getWaiverAccepted()));
            if (Boolean.TRUE.equals(registerRequest.getWaiverAccepted())) {
                user.setWaiverAcceptedAt(new java.util.Date());
            }
            user.setInsurancePreference(registerRequest.getInsurancePreference() != null ? registerRequest.getInsurancePreference() : "BUDGET");
            user.setRole(registerRequest.getRole());

            // Handle business registration
            if (Boolean.TRUE.equals(registerRequest.getIsBusiness())) {
                // Validate business name if provided
                if (registerRequest.getBusinessName() != null && !registerRequest.getBusinessName().trim().isEmpty()) {
                    Map<String, Object> businessNameValidation = userValidationService.validateBusinessName(
                        registerRequest.getBusinessName()
                    );
                    if (!(Boolean) businessNameValidation.get("valid")) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> errors = (Map<String, String>) businessNameValidation.get("errors");
                        logger.info("Business name validation failed: " + errors);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "message", "Validation failed", "errors", errors));
                    }
                }
                
                user.setIsBusiness(true);
                user.setCustomerTier(com.reliablecarriers.Reliable.Carriers.model.CustomerTier.BUSINESS);
                user.setBusinessName(registerRequest.getBusinessName());
                user.setTaxId(registerRequest.getTaxId());
                user.setRegistrationNumber(registerRequest.getRegistrationNumber());
                
                // Set business verification status to PENDING
                user.setBusinessVerificationStatus(
                    com.reliablecarriers.Reliable.Carriers.model.BusinessVerificationStatus.PENDING
                );
                
                // Set default credit terms for new businesses (can be adjusted after verification)
                user.setCreditLimit(java.math.BigDecimal.ZERO); // Will be set after verification
                user.setPaymentTerms(0); // Immediate payment until verified, then Net 30
                user.setCurrentBalance(java.math.BigDecimal.ZERO);
            } else {
                user.setIsBusiness(false);
                user.setCustomerTier(com.reliablecarriers.Reliable.Carriers.model.CustomerTier.INDIVIDUAL);
            }
            
            // Handle driver registration (if role is DRIVER - allowed for self-registration)
            if (registerRequest.getRole() == UserRole.DRIVER) {
                user.setDriverLicenseNumber(registerRequest.getDriverLicenseNumber());
                user.setLicenseExpiryDate(registerRequest.getLicenseExpiryDate());
                user.setVehicleMake(registerRequest.getVehicleMake());
                user.setVehicleModel(registerRequest.getVehicleModel());
                user.setVehicleYear(registerRequest.getVehicleYear());
                user.setVehicleRegistration(registerRequest.getVehicleRegistration());
                user.setVehicleColor(registerRequest.getVehicleColor());
                user.setVehicleCapacityKg(registerRequest.getVehicleCapacityKg());
                
                // Set driver verification status to PENDING
                user.setDriverVerificationStatus(
                    com.reliablecarriers.Reliable.Carriers.model.DriverVerificationStatus.PENDING
                );
                
                // Initialize driver stats
                user.setIsOnline(false);
                user.setTotalDeliveries(0);
                user.setTotalEarnings(java.math.BigDecimal.ZERO);
                user.setDriverRating(null);
            }

            // Register the user and implement 2FA
            User registeredUser = authService.registerUser(user);
            logger.info("User registered successfully: " + registeredUser.getEmail());
            
            // Send role-specific welcome/registration notification email
            try {
                if (emailService != null) {
                    String recipientName = registeredUser.getFirstName() + " " + registeredUser.getLastName();
                    if (registeredUser.getRole() == UserRole.DRIVER) {
                        // Send driver-specific registration email
                        emailService.sendSimpleEmail(
                            registeredUser.getEmail(),
                            "Welcome to Reliable Carriers - Driver Registration",
                            "Hello " + recipientName + ",\n\n" +
                            "Thank you for registering as a driver with Reliable Carriers!\n\n" +
                            "Your account has been created and is pending verification. " +
                            "Please upload the required documents (driver's license, vehicle registration, etc.) " +
                            "to complete your driver profile.\n\n" +
                            "Once your documents are verified, you'll be able to start accepting delivery requests.\n\n" +
                            "If you have any questions, please contact our support team.\n\n" +
                            "Best regards,\n" +
                            "Reliable Carriers Team"
                        );
                    } else if (Boolean.TRUE.equals(registeredUser.getIsBusiness())) {
                        // Send business-specific registration email
                        emailService.sendSimpleEmail(
                            registeredUser.getEmail(),
                            "Welcome to Reliable Carriers - Business Account Created",
                            "Hello " + recipientName + ",\n\n" +
                            "Thank you for registering your business with Reliable Carriers!\n\n" +
                            "Your business account has been created and is pending verification. " +
                            "Please upload the required business documents (business registration, tax ID, etc.) " +
                            "to complete your business profile.\n\n" +
                            "Once verified, you'll have access to:\n" +
                            "- Business shipping rates\n" +
                            "- Credit terms (Net 30)\n" +
                            "- Bulk shipping discounts\n" +
                            "- Business analytics dashboard\n\n" +
                            "If you have any questions, please contact our business support team.\n\n" +
                            "Best regards,\n" +
                            "Reliable Carriers Business Team"
                        );
                    } else {
                        // Send customer-specific registration email
                        emailService.sendWelcomeEmail(registeredUser.getEmail(), recipientName);
                    }
                    logger.info("Registration notification email sent to: " + registeredUser.getEmail());
                }
            } catch (Exception e) {
                logger.error("Failed to send registration notification email: " + e.getMessage(), e);
                // Don't fail registration if email fails
            }

            // Implement 2FA for registration (same for customers, businesses, and drivers)
            boolean codeSent = false;
            try {
                logger.info("Generating 2FA token for registration...");
                
                // Generate and send 2FA code via email - ensure it's sent before returning
                if (twoFactorService != null) {
                    try {
                        // Clear any existing tokens first and generate new one
                        twoFactorService.generateAndSendToken(registeredUser, "EMAIL");
                        codeSent = true;
                        logger.info("2FA code successfully sent to email: " + registeredUser.getEmail());
                    } catch (Exception e) {
                        logger.error("Failed to send 2FA code via email: " + e.getMessage(), e);
                        // Try to resend once more
                        try {
                            Thread.sleep(500); // Small delay before retry
                            twoFactorService.generateAndSendToken(registeredUser, "EMAIL");
                            codeSent = true;
                            logger.info("2FA code sent on retry to email: " + registeredUser.getEmail());
                        } catch (Exception retryException) {
                            logger.error("Failed to send 2FA code on retry: " + retryException.getMessage(), retryException);
                        }
                    }
                } else {
                    logger.warn("TwoFactorService is not available - 2FA codes will not be sent");
                }
                
                // Get the actual token from database (in case email failed, user can still verify)
                String actualToken = null;
                try {
                    // Find the most recent unused token for this user
                    var userTokens = twoFactorTokenRepository.findByUserAndUsedFalseOrderByExpiresAtDesc(registeredUser);
                    if (!userTokens.isEmpty()) {
                        actualToken = userTokens.get(0).getToken();
                        logger.info("Retrieved actual 2FA token from database for " + registeredUser.getEmail() + ": " + actualToken);
                    }
                } catch (Exception e) {
                    logger.warn("Could not retrieve token from database: " + e.getMessage());
                }
                
                // Use actual token if available, otherwise generate demo code
                String demoCode = actualToken != null ? actualToken : String.format("%06d", new java.util.Random().nextInt(1000000));
                logger.info("2FA code for " + registeredUser.getEmail() + ": " + demoCode + " (codeSent: " + codeSent + ")");
                
                String message = codeSent 
                    ? "Account created! Verification code sent to your email. Please check your inbox."
                    : "Account created! Please use the resend button to receive your verification code. Check application logs if email fails.";
                
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true, 
                        "requires2fa", true,
                        "codeSent", codeSent,
                        "message", message,
                        "email", registeredUser.getEmail(),
                        "role", registeredUser.getRole().toString(),
                        "demoCode", demoCode  // Actual token from database (for development/testing)
                    ));
            } catch (Exception e) {
                logger.error("2FA generation error: " + e.getMessage(), e);
                // If 2FA fails, still return success but indicate 2FA is required
                // User can use resend functionality
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true, 
                        "requires2fa", true,
                        "message", "Account created! Please verify your email. If you didn't receive a code, use the resend button.",
                        "email", registeredUser.getEmail(),
                        "role", registeredUser.getRole().toString()
                    ));
            }
        } catch (IllegalArgumentException e) {
            logger.error("Registration validation error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Registration error: " + e.getMessage(), e);
            e.printStackTrace(); // Print full stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Registration failed: " + e.getMessage() + ". Please check the server logs for details."));
        }
    }

    /**
     * Test endpoint to check if controller is working
     */
    @GetMapping("/register/test")
    public ResponseEntity<?> testRegistration() {
        return ResponseEntity.ok(Map.of("message", "Registration controller is working"));
    }

    /**
     * Verify 2FA code for registration completion
     */
    @PostMapping("/register/verify")
    public ResponseEntity<?> verifyRegistration(@RequestParam String email, @RequestParam String code) {
        try {
            logger.debug("Registration verification attempt for email: " + email);
            
            // Find the user by email
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
            }

            // Verify the 2FA token
            boolean verified = twoFactorService.verifyToken(user, code);
            if (!verified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid or expired verification code"));
            }

            logger.debug("Registration verification successful for: " + email);

            // Generate JWT token for the verified user
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Set authentication in SecurityContext
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Store user info in session for server-rendered pages
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userFirstName", user.getFirstName());
            session.setAttribute("userLastName", user.getLastName());
            session.setAttribute("userPhone", user.getPhone());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole().toString());
            session.setAttribute("isAuthenticated", true);
                
                // Create response with token
                AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole(),
                        token
                );

            // Check if user needs approval (driver or business)
            boolean needsApproval = false;
            String approvalMessage = "";
            
            if (user.getRole() == UserRole.DRIVER) {
                if (user.getDriverVerificationStatus() == null || 
                    user.getDriverVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.DriverVerificationStatus.PENDING) {
                    needsApproval = true;
                    approvalMessage = "Your driver account is pending admin approval. You'll receive an email once approved.";
                }
            } else if (Boolean.TRUE.equals(user.getIsBusiness())) {
                if (user.getBusinessVerificationStatus() == null || 
                    user.getBusinessVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.BusinessVerificationStatus.PENDING) {
                    needsApproval = true;
                    approvalMessage = "Your business account is pending admin approval. You'll receive an email once approved.";
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", needsApproval ? approvalMessage : "Registration completed successfully! Welcome to Reliable Carriers.",
                "user", response,
                "needsApproval", needsApproval,
                "verificationStatus", user.getRole() == UserRole.DRIVER ? 
                    (user.getDriverVerificationStatus() != null ? user.getDriverVerificationStatus().toString() : "PENDING") :
                    (user.getBusinessVerificationStatus() != null ? user.getBusinessVerificationStatus().toString() : "PENDING")
            ));
            
        } catch (Exception e) {
            logger.error("Registration verification error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Verification failed. Please try again."));
        }
    }

    /**
     * Check verification status for current user
     */
    @GetMapping("/verification-status")
    public ResponseEntity<?> checkVerificationStatus(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Not authenticated"));
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            boolean approved = false;
            String status = "PENDING";

            if (user.getRole() == UserRole.DRIVER) {
                status = user.getDriverVerificationStatus() != null ? 
                    user.getDriverVerificationStatus().toString() : "PENDING";
                approved = user.getDriverVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.DriverVerificationStatus.APPROVED;
            } else if (Boolean.TRUE.equals(user.getIsBusiness())) {
                status = user.getBusinessVerificationStatus() != null ? 
                    user.getBusinessVerificationStatus().toString() : "PENDING";
                approved = user.getBusinessVerificationStatus() == com.reliablecarriers.Reliable.Carriers.model.BusinessVerificationStatus.APPROVED;
            } else {
                // Regular customers don't need approval
                approved = true;
                status = "APPROVED";
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "approved", approved,
                "status", status,
                "role", user.getRole().toString()
            ));
        } catch (Exception e) {
            logger.error("Error checking verification status: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error checking status"));
        }
    }

    /**
     * Resend 2FA code for registration
     */
    @PostMapping("/register/resend")
    public ResponseEntity<?> resendRegistrationCode(@RequestParam String email, @RequestParam(defaultValue = "EMAIL") String method) {
        try {
            logger.info("Resending registration code for: " + email + " via " + method);
            
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                logger.warn("Resend code requested for non-existent user: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found. Please register first."));
            }

            // Ensure twoFactorService is available
            if (twoFactorService == null) {
                logger.error("TwoFactorService is not available");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Verification service is temporarily unavailable. Please try again later."));
            }

            // Send new 2FA token - this will clear old tokens and generate a new one
            try {
                twoFactorService.generateAndSendToken(user, method);
                logger.info("2FA code successfully sent to " + email + " via " + method);
        } catch (Exception e) {
                logger.error("Failed to send 2FA code: " + e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to send verification code. Please try again."));
            }
            
            String methodText = "EMAIL".equalsIgnoreCase(method) ? "email" : "SMS";
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Verification code sent to your " + methodText + ". Please check your " + methodText.toLowerCase() + "."
            ));
            
        } catch (Exception e) {
            logger.error("Failed to resend registration code: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to send verification code. Please try again."));
        }
    }

    @PostMapping("/admin/create-driver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDriverAccount(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Ensure the role is set to DRIVER for admin-created driver accounts
            if (registerRequest.getRole() != UserRole.DRIVER) {
                registerRequest.setRole(UserRole.DRIVER);
            }

            // Create a new driver user from the request
            User user = new User();
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            user.setPhone(registerRequest.getPhone());
            user.setAddress(registerRequest.getAddress());
            user.setCity(registerRequest.getCity());
            user.setState(registerRequest.getState());
            user.setZipCode(registerRequest.getZipCode());
            user.setCountry(registerRequest.getCountry());
            user.setWaiverAccepted(Boolean.TRUE.equals(registerRequest.getWaiverAccepted()));
            if (Boolean.TRUE.equals(registerRequest.getWaiverAccepted())) {
                user.setWaiverAcceptedAt(new java.util.Date());
            }
            user.setInsurancePreference(registerRequest.getInsurancePreference() != null ? registerRequest.getInsurancePreference() : "BUDGET");
            user.setRole(UserRole.DRIVER);

            // Register the driver user
            User registeredDriver = authService.registerUser(user);

            // Create response (without JWT token since admin is creating the account)
            AuthResponse response = new AuthResponse(
                    registeredDriver.getId(),
                    registeredDriver.getFirstName(),
                    registeredDriver.getLastName(),
                    registeredDriver.getEmail(),
                    registeredDriver.getRole()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating driver account: " + e.getMessage());
        }
    }

    @PostMapping("/admin/create-tracking-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTrackingManagerAccount(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Ensure the role is set to TRACKING_MANAGER for admin-created tracking manager accounts
            if (registerRequest.getRole() != UserRole.TRACKING_MANAGER) {
                registerRequest.setRole(UserRole.TRACKING_MANAGER);
            }

            // Create a new tracking manager user from the request
            User user = new User();
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            user.setPhone(registerRequest.getPhone());
            user.setAddress(registerRequest.getAddress());
            user.setCity(registerRequest.getCity());
            user.setState(registerRequest.getState());
            user.setZipCode(registerRequest.getZipCode());
            user.setCountry(registerRequest.getCountry());
            user.setWaiverAccepted(Boolean.TRUE.equals(registerRequest.getWaiverAccepted()));
            if (Boolean.TRUE.equals(registerRequest.getWaiverAccepted())) {
                user.setWaiverAcceptedAt(new java.util.Date());
            }
            user.setInsurancePreference(registerRequest.getInsurancePreference() != null ? registerRequest.getInsurancePreference() : "BUDGET");
            user.setRole(UserRole.TRACKING_MANAGER);

            // Register the tracking manager user
            User registeredTrackingManager = authService.registerUser(user);

            // Create response (without JWT token since admin is creating the account)
            AuthResponse response = new AuthResponse(
                    registeredTrackingManager.getId(),
                    registeredTrackingManager.getFirstName(),
                    registeredTrackingManager.getLastName(),
                    registeredTrackingManager.getEmail(),
                    registeredTrackingManager.getRole()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating tracking manager account: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Validate identifier is not null or empty (additional check beyond @Valid)
            if (authRequest == null || authRequest.getIdentifier() == null || authRequest.getIdentifier().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Email or phone number is required"));
            }
            
            // Authenticate the user
            // The AuthRequest.identifier may be an email or a phone. We still authenticate via AuthService which expects email;
            // try to resolve identifier to an email if it's a phone.
            String identifier = authRequest.getIdentifier().trim();
            String loginEmail = identifier;
            if (!identifier.contains("@")) {
                // assume phone -> try to find user by phone
                var uopt = userRepository.findByPhone(identifier);
                if (uopt.isPresent()) loginEmail = uopt.get().getEmail();
            }
            logger.debug("authenticateUser: identifier={}, resolvedEmail={}", identifier, loginEmail);
            
            // Check if account is locked
            if (accountLockoutService.isAccountLocked(loginEmail)) {
                long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(loginEmail);
                return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of(
                        "success", false,
                        "message", "Account is temporarily locked due to multiple failed login attempts.",
                        "remainingMinutes", remainingMinutes,
                        "locked", true
                    ));
            }
            
            User authenticatedUser;
            try {
                authenticatedUser = authService.authenticateUser(loginEmail, authRequest.getPassword());
                
                // Clear failed login attempts on successful authentication
                accountLockoutService.clearFailedLoginAttempts(loginEmail);
            } catch (BadCredentialsException e) {
                // Record failed login attempt
                accountLockoutService.recordFailedLoginAttempt(loginEmail);
                
                // Check if account is now locked
                if (accountLockoutService.isAccountLocked(loginEmail)) {
                    long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(loginEmail);
                    return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(Map.of(
                            "success", false,
                            "message", "Account is temporarily locked due to multiple failed login attempts.",
                            "remainingMinutes", remainingMinutes,
                            "locked", true
                        ));
                }
                
                // Re-throw to be handled by exception handler
                throw e;
            }

            // Allow customers and businesses (businesses have CUSTOMER role) to use this login endpoint.
            // Staff/admin/driver/tracking manager must use the staff portal (/staff-login).
            if (authenticatedUser.getRole() != com.reliablecarriers.Reliable.Carriers.model.UserRole.CUSTOMER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Access denied. Use the staff portal to sign in for staff accounts."));
            }

            // For customers, check if 2FA is enabled
            boolean has2faEnabled = Boolean.TRUE.equals(authenticatedUser.getTotpEnabled()) || 
                                   (authenticatedUser.getPhone() != null && !authenticatedUser.getPhone().isEmpty());
            
            if (has2faEnabled) {
                // Present 2FA stage
                return ResponseEntity.ok(Map.of(
                        "requires2fa", true,
                        "email", authenticatedUser.getEmail(),
                        "phone", authenticatedUser.getPhone(),
                        "totpEnabled", Boolean.TRUE.equals(authenticatedUser.getTotpEnabled())
                ));
            } else {
                // No 2FA required, generate JWT token directly
                final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatedUser.getEmail());
                final String token = jwtTokenUtil.generateToken(userDetails);
                
                // Set authentication in SecurityContext
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                AuthResponse response = new AuthResponse(
                        authenticatedUser.getId(),
                        authenticatedUser.getFirstName(),
                        authenticatedUser.getLastName(),
                        authenticatedUser.getEmail(),
                        authenticatedUser.getRole(),
                        token
                );
                return ResponseEntity.ok(response);
            }

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            // Use validation service to provide specific error messages
            String errorMessage = userValidationService.getAuthenticationErrorMessage(authRequest.getIdentifier(), authRequest.getPassword());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", errorMessage));
        } catch (Exception e) {
            logger.error("Login error: " + e.getMessage(), e);
            e.printStackTrace(); // Print full stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error during authentication: " + e.getMessage() + ". Please check the server logs for details."));
        }
    }

    @PostMapping("/staff-login-test")
    public ResponseEntity<?> authenticateStaffTest(@RequestBody Map<String, Object> request) {
        try {
            logger.debug("authenticateStaffTest: Received request={}", request);
            return ResponseEntity.ok(Map.of("received", request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/staff-login-raw")
    public ResponseEntity<?> authenticateStaffRaw(@RequestBody String rawRequest) {
        try {
            logger.debug("authenticateStaffRaw: Received raw request={}", rawRequest);
            return ResponseEntity.ok(Map.of("rawRequest", rawRequest));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/staff-login")
    public ResponseEntity<?> authenticateStaff(@Valid @RequestBody AuthRequest authRequest, HttpServletRequest request) {
        try {
            // Debug logging
            logger.debug("authenticateStaff: Received authRequest={}", authRequest);
            logger.debug("authenticateStaff: identifier={}, password={}", 
                authRequest.getIdentifier(), 
                authRequest.getPassword() != null ? "[PROVIDED]" : "[NULL]");
            
            // Authenticate the user
            String identifier = authRequest.getIdentifier();
            String loginEmail = identifier;
            if (!identifier.contains("@")) {
                var uopt = userRepository.findByPhone(identifier);
                if (uopt.isPresent()) loginEmail = uopt.get().getEmail();
            }
            logger.debug("authenticateStaff: identifier={}, resolvedEmail={}", identifier, loginEmail);
            
            // Check if account is locked
            if (accountLockoutService.isAccountLocked(loginEmail)) {
                long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(loginEmail);
                return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of(
                        "success", false,
                        "message", "Account is temporarily locked due to multiple failed login attempts.",
                        "remainingMinutes", remainingMinutes,
                        "locked", true
                    ));
            }
            
            User authenticatedUser = authService.authenticateUser(loginEmail, authRequest.getPassword());
            
            // Clear failed login attempts on successful authentication
            accountLockoutService.clearFailedLoginAttempts(loginEmail);

            // Check if user has staff role (ADMIN, STAFF, DRIVER, TRACKING_MANAGER)
            if (authenticatedUser.getRole() != UserRole.ADMIN && 
                authenticatedUser.getRole() != UserRole.STAFF && 
                authenticatedUser.getRole() != UserRole.DRIVER && 
                authenticatedUser.getRole() != UserRole.TRACKING_MANAGER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Staff portal is for authorized personnel only.");
            }

            // For staff logins, require two-factor authentication as well.
            // Return a requires2fa payload so the client can initiate method-based token sending
            // or prompt for TOTP if enabled. After verifying via /api/auth/2fa/verify-method or /2fa/verify,
            // the server will issue the JWT/session.
            return ResponseEntity.ok(Map.of(
                    "requires2fa", true,
                    "email", authenticatedUser.getEmail(),
                    "phone", authenticatedUser.getPhone(),
                    "totpEnabled", Boolean.TRUE.equals(authenticatedUser.getTotpEnabled()),
                    "role", authenticatedUser.getRole().name()
            ));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            // Record failed login attempt
            String identifier = authRequest.getIdentifier();
            String loginEmail = identifier;
            if (!identifier.contains("@")) {
                var uopt = userRepository.findByPhone(identifier);
                if (uopt.isPresent()) loginEmail = uopt.get().getEmail();
            }
            
            // Only record if user exists (to prevent email enumeration)
            if (userRepository.findByEmail(loginEmail).isPresent()) {
                accountLockoutService.recordFailedLoginAttempt(loginEmail);
                
                // Check if account is now locked
                if (accountLockoutService.isAccountLocked(loginEmail)) {
                    long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(loginEmail);
                    return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(Map.of(
                            "success", false,
                            "message", "Too many failed login attempts. Account locked for " + remainingMinutes + " minutes.",
                            "remainingMinutes", remainingMinutes,
                            "locked", true
                        ));
                }
            }
            
            // Use validation service to provide specific error messages
            String errorMessage = userValidationService.getAuthenticationErrorMessage(authRequest.getIdentifier(), authRequest.getPassword());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", errorMessage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error during authentication: " + e.getMessage()));
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authenticated user found");
            }

            // Create response
            AuthResponse response = new AuthResponse(
                    currentUser.getId(),
                    currentUser.getFirstName(),
                    currentUser.getLastName(),
                    currentUser.getEmail(),
                    currentUser.getRole()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving current user: " + e.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a stateless JWT implementation, the client would simply discard the token
        // For session-based auth, Spring Security handles logout via /logout endpoint
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/test-password")
    public ResponseEntity<?> testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hashedPassword = encoder.encode(password);
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
        
        return ResponseEntity.ok(Map.of(
            "originalPassword", password,
            "newHash", hashedPassword,
            "existingHash", existingHash,
            "newHashMatches", encoder.matches(password, hashedPassword),
            "existingHashMatches", encoder.matches(password, existingHash)
        ));
    }

    // ... oauth2 success handled by Oauth2SuccessController

    @GetMapping("/fix-passwords")
    public ResponseEntity<?> fixPasswords() {
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String correctHash = encoder.encode("admin123");
            
            // Update all user passwords to the correct hash
            List<String> emails = Arrays.asList(
                "admin@reliablecarriers.com",
                "driver@reliablecarriers.com", 
                "sarah.wilson@reliablecarriers.com",
                "michael.brown@reliablecarriers.com",
                "lisa.garcia@reliablecarriers.com",
                "david.martinez@reliablecarriers.com",
                "emma.johnson@reliablecarriers.com",
                "customer@example.com"
            );
            
            int updatedCount = 0;
            for (String email : emails) {
                try {
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        user.setPassword(correctHash);
                        userRepository.save(user);
                        updatedCount++;
                    }
                } catch (Exception e) {
                    // Continue with other users
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Passwords updated successfully",
                "updatedCount", updatedCount,
                "correctHash", correctHash,
                "testResult", encoder.matches("admin123", correctHash)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update passwords: " + e.getMessage()));
        }
    }

    @GetMapping("/create-test-users")
    public ResponseEntity<?> createTestUsers() {
        try {
            // Create staff user if it doesn't exist
            if (!userRepository.findByEmail("staff@reliablecarriers.com").isPresent()) {
                User staffUser = new User();
                staffUser.setFirstName("Staff");
                staffUser.setLastName("User");
                staffUser.setEmail("staff@reliablecarriers.com");
                staffUser.setPhone("+27123456781");
                staffUser.setPassword(passwordEncoder.encode("admin123"));
                staffUser.setRole(UserRole.STAFF);
                userRepository.save(staffUser);
            }

            // Create driver user if it doesn't exist
            if (!userRepository.findByEmail("driver@reliablecarriers.com").isPresent()) {
                User driverUser = new User();
                driverUser.setFirstName("John");
                driverUser.setLastName("Driver");
                driverUser.setEmail("driver@reliablecarriers.com");
                driverUser.setPhone("+27123456788");
                driverUser.setPassword(passwordEncoder.encode("admin123"));
                driverUser.setRole(UserRole.DRIVER);
                userRepository.save(driverUser);
            }

            // Create tracking manager if it doesn't exist
            if (!userRepository.findByEmail("tracking@reliablecarriers.com").isPresent()) {
                User trackingUser = new User();
                trackingUser.setFirstName("Alex");
                trackingUser.setLastName("Tracker");
                trackingUser.setEmail("tracking@reliablecarriers.com");
                trackingUser.setPhone("+27123456782");
                trackingUser.setPassword(passwordEncoder.encode("admin123"));
                trackingUser.setRole(UserRole.TRACKING_MANAGER);
                userRepository.save(trackingUser);
            }

            // Create a test customer without 2FA
            if (!userRepository.findByEmail("customer@test.com").isPresent()) {
                User customerUser = new User();
                customerUser.setFirstName("Test");
                customerUser.setLastName("Customer");
                customerUser.setEmail("customer@test.com");
                customerUser.setPhone(null); // No phone = no 2FA
                customerUser.setPassword(passwordEncoder.encode("test123"));
                customerUser.setRole(UserRole.CUSTOMER);
                customerUser.setTotpEnabled(false); // Explicitly disable 2FA
                userRepository.save(customerUser);
            }

            return ResponseEntity.ok("Test users created successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating test users: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            // Always return success to prevent email enumeration attacks
            if (user == null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "If the email exists, a reset link has been sent."));
            }
            
            // Generate secure token
            String token = generateSecureToken();
            
            // Mark all existing tokens for this user as used
            passwordResetTokenRepository.markAllTokensAsUsedForUser(user);
            
            // Create new password reset token
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            passwordResetTokenRepository.save(resetToken);
            
            // Send email
            emailService.sendPasswordReset(user.getEmail(), token);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Password reset link sent to your email."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error processing password reset request."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // Find valid token
            PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(request.getToken(), new Date()).orElse(null);
            
            if (resetToken == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Invalid or expired reset token."));
            }
            
            // Update user password
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            
            // Mark token as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Password reset successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error resetting password."));
        }
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
