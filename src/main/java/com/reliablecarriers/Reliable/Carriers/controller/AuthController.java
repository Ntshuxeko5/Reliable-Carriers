package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.config.JwtTokenUtil;
import com.reliablecarriers.Reliable.Carriers.dto.AuthRequest;
import com.reliablecarriers.Reliable.Carriers.dto.AuthResponse;
import com.reliablecarriers.Reliable.Carriers.dto.RegisterRequest;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
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
import com.reliablecarriers.Reliable.Carriers.dto.ForgotPasswordRequest;
import com.reliablecarriers.Reliable.Carriers.dto.ResetPasswordRequest;
import com.reliablecarriers.Reliable.Carriers.model.PasswordResetToken;
import com.reliablecarriers.Reliable.Carriers.repository.PasswordResetTokenRepository;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
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
    private final EmailService emailService;
    private final com.reliablecarriers.Reliable.Carriers.service.TotpService totpService;
    private final com.reliablecarriers.Reliable.Carriers.service.TwoFactorService twoFactorService;

    public AuthController(AuthService authService, JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService, UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService, com.reliablecarriers.Reliable.Carriers.service.TotpService totpService, com.reliablecarriers.Reliable.Carriers.service.TwoFactorService twoFactorService) {
        this.authService = authService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.totpService = totpService;
        this.twoFactorService = twoFactorService;
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

            return ResponseEntity.ok(Map.of("secret", secret));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error setting up 2FA: " + e.getMessage());
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
            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole(),
                    token
            );
            return ResponseEntity.ok(response);
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
            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole(),
                    tokenJwt
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying 2FA token: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Restrict driver and tracking manager role creation - only admins can create these accounts
            if (registerRequest.getRole() == UserRole.DRIVER || registerRequest.getRole() == UserRole.TRACKING_MANAGER) {
                String roleName = registerRequest.getRole() == UserRole.DRIVER ? "Driver" : "Tracking Manager";
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(roleName + " accounts can only be created by administrators. Please contact support.");
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

            // Register the user
            User registeredUser = authService.registerUser(user);

            // Generate JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(registeredUser.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Create response
            AuthResponse response = new AuthResponse(
                    registeredUser.getId(),
                    registeredUser.getFirstName(),
                    registeredUser.getLastName(),
                    registeredUser.getEmail(),
                    registeredUser.getRole(),
                    token
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during registration: " + e.getMessage());
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
            // Authenticate the user
            // The AuthRequest.identifier may be an email or a phone. We still authenticate via AuthService which expects email;
            // try to resolve identifier to an email if it's a phone.
            String identifier = authRequest.getIdentifier();
            String loginEmail = identifier;
            if (!identifier.contains("@")) {
                // assume phone -> try to find user by phone
                var uopt = userRepository.findByPhone(identifier);
                if (uopt.isPresent()) loginEmail = uopt.get().getEmail();
            }
            logger.debug("authenticateUser: identifier={}, resolvedEmail={}", identifier, loginEmail);
            User authenticatedUser = authService.authenticateUser(loginEmail, authRequest.getPassword());

            // Only allow customers to use this public/customer login endpoint.
            // Staff/admin/driver/tracking manager must use the staff portal (/staff-login).
            if (authenticatedUser.getRole() != com.reliablecarriers.Reliable.Carriers.model.UserRole.CUSTOMER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Use the staff portal to sign in for staff accounts.");
            }

            // For customers, always present a second-factor stage so the client can offer method choices
            // (email or SMS) or TOTP if they've enabled it. Frontend will call /api/auth/2fa/request
            // to actually send a method-based token, or /2fa/verify to verify TOTP.
            return ResponseEntity.ok(Map.of(
                    "requires2fa", true,
                    "email", authenticatedUser.getEmail(),
                    "phone", authenticatedUser.getPhone(),
                    "totpEnabled", Boolean.TRUE.equals(authenticatedUser.getTotpEnabled())
            ));

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during authentication: " + e.getMessage());
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
            User authenticatedUser = authService.authenticateUser(loginEmail, authRequest.getPassword());

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during authentication: " + e.getMessage());
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