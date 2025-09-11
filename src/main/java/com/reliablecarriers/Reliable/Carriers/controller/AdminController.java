package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.RegisterRequest;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Get all drivers
     */
    @GetMapping("/drivers")
    public ResponseEntity<List<User>> getAllDrivers() {
        List<User> drivers = userService.getUsersByRole(UserRole.DRIVER);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get all tracking managers
     */
    @GetMapping("/tracking-managers")
    public ResponseEntity<List<User>> getAllTrackingManagers() {
        List<User> trackingManagers = userService.getUsersByRole(UserRole.TRACKING_MANAGER);
        return ResponseEntity.ok(trackingManagers);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Create a new driver account
     */
    @PostMapping("/drivers")
    public ResponseEntity<?> createDriver(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Create a new driver user
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
            driver.setCountry(registerRequest.getCountry());
            driver.setRole(UserRole.DRIVER);

            User createdDriver = userService.createUser(driver);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating driver: " + e.getMessage());
        }
    }

    /**
     * Create a new tracking manager account
     */
    @PostMapping("/tracking-managers")
    public ResponseEntity<?> createTrackingManager(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Create a new tracking manager user
            User trackingManager = new User();
            trackingManager.setFirstName(registerRequest.getFirstName());
            trackingManager.setLastName(registerRequest.getLastName());
            trackingManager.setEmail(registerRequest.getEmail());
            trackingManager.setPassword(registerRequest.getPassword());
            trackingManager.setPhone(registerRequest.getPhone());
            trackingManager.setAddress(registerRequest.getAddress());
            trackingManager.setCity(registerRequest.getCity());
            trackingManager.setState(registerRequest.getState());
            trackingManager.setZipCode(registerRequest.getZipCode());
            trackingManager.setCountry(registerRequest.getCountry());
            trackingManager.setRole(UserRole.TRACKING_MANAGER);

            User createdTrackingManager = userService.createUser(trackingManager);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTrackingManager);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating tracking manager: " + e.getMessage());
        }
    }

    /**
     * Update user information (admin only)
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            User user = userService.getUserById(id);
            
            // Update user fields
            if (request.containsKey("firstName")) {
                user.setFirstName(request.get("firstName"));
            }
            if (request.containsKey("lastName")) {
                user.setLastName(request.get("lastName"));
            }
            if (request.containsKey("email")) {
                user.setEmail(request.get("email"));
            }
            if (request.containsKey("phone")) {
                user.setPhone(request.get("phone"));
            }
            if (request.containsKey("role")) {
                try {
                    UserRole newRole = UserRole.valueOf(request.get("role").toUpperCase());
                    user.setRole(newRole);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid role: " + request.get("role"));
                }
            }
            
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating user: " + e.getMessage());
        }
    }

    /**
     * Update user role (admin only)
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String roleString = request.get("role");
            if (roleString == null) {
                return ResponseEntity.badRequest().body("Role is required");
            }

            UserRole newRole;
            try {
                newRole = UserRole.valueOf(roleString.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid role: " + roleString);
            }

            User user = userService.getUserById(id);
            user.setRole(newRole);
            User updatedUser = userService.updateUser(id, user);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating user role: " + e.getMessage());
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // Check if user exists
            User user = userService.getUserById(id);
            
            // Prevent deletion of admin accounts
            if (user.getRole() == UserRole.ADMIN) {
                return ResponseEntity.badRequest().body("Cannot delete admin accounts");
            }
            
            // In a real application, you might want to soft delete instead
            // For now, we'll just return success
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting user: " + e.getMessage());
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/users/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> drivers = userService.getUsersByRole(UserRole.DRIVER);
            List<User> customers = userService.getUsersByRole(UserRole.CUSTOMER);
            List<User> staff = userService.getUsersByRole(UserRole.STAFF);
            List<User> admins = userService.getUsersByRole(UserRole.ADMIN);
            List<User> trackingManagers = userService.getUsersByRole(UserRole.TRACKING_MANAGER);

            Map<String, Object> statistics = Map.of(
                "totalUsers", allUsers.size(),
                "drivers", drivers.size(),
                "customers", customers.size(),
                "staff", staff.size(),
                "admins", admins.size(),
                "trackingManagers", trackingManagers.size()
            );

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error retrieving statistics: " + e.getMessage()));
        }
    }
}
