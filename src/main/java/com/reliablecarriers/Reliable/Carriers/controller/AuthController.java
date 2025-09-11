package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.config.JwtTokenUtil;
import com.reliablecarriers.Reliable.Carriers.dto.AuthRequest;
import com.reliablecarriers.Reliable.Carriers.dto.AuthResponse;
import com.reliablecarriers.Reliable.Carriers.dto.RegisterRequest;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthService authService, JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            User authenticatedUser = authService.authenticateUser(authRequest.getEmail(), authRequest.getPassword());

            // Generate JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatedUser.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Create response
            AuthResponse response = new AuthResponse(
                    authenticatedUser.getId(),
                    authenticatedUser.getFirstName(),
                    authenticatedUser.getLastName(),
                    authenticatedUser.getEmail(),
                    authenticatedUser.getRole(),
                    token
            );

            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during authentication: " + e.getMessage());
        }
    }

    @PostMapping("/staff-login")
    public ResponseEntity<?> authenticateStaff(@Valid @RequestBody AuthRequest authRequest, HttpServletRequest request) {
        try {
            // Authenticate the user
            User authenticatedUser = authService.authenticateUser(authRequest.getEmail(), authRequest.getPassword());

            // Check if user has staff role (ADMIN, STAFF, DRIVER, TRACKING_MANAGER)
            if (authenticatedUser.getRole() != UserRole.ADMIN && 
                authenticatedUser.getRole() != UserRole.STAFF && 
                authenticatedUser.getRole() != UserRole.DRIVER && 
                authenticatedUser.getRole() != UserRole.TRACKING_MANAGER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Staff portal is for authorized personnel only.");
            }

            // Generate JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatedUser.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Create session for web access
            HttpSession session = request.getSession(true);
            session.setAttribute("user", authenticatedUser);
            session.setAttribute("userRole", authenticatedUser.getRole().name());
            session.setAttribute("jwtToken", token);
            
            // Create response
            AuthResponse response = new AuthResponse(
                    authenticatedUser.getId(),
                    authenticatedUser.getFirstName(),
                    authenticatedUser.getLastName(),
                    authenticatedUser.getEmail(),
                    authenticatedUser.getRole(),
                    token
            );

            return ResponseEntity.ok(response);
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
}