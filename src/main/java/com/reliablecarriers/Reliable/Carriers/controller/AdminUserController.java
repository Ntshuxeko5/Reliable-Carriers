package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyRole('ADMIN','TRACKING_MANAGER')")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get all users for admin dashboard
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("firstName", user.getFirstName());
                    userMap.put("lastName", user.getLastName());
                    userMap.put("email", user.getEmail());
                    userMap.put("phone", user.getPhone());
                    userMap.put("role", user.getRole());
                    userMap.put("isActive", true); // Default to active since User model doesn't have isActive field
                    userMap.put("createdAt", user.getCreatedAt());
                    userMap.put("lastLogin", user.getUpdatedAt()); // Using updatedAt as proxy for last login
                    return userMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user statistics for admin dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        try {
            List<User> allUsers = userRepository.findAll();
            
            // Count users by role
            long totalUsers = allUsers.size();
            long adminCount = allUsers.stream().filter(u -> u.getRole() == UserRole.ADMIN).count();
            long driverCount = allUsers.stream().filter(u -> u.getRole() == UserRole.DRIVER).count();
            long customerCount = allUsers.stream().filter(u -> u.getRole() == UserRole.CUSTOMER).count();
            long staffCount = allUsers.stream().filter(u -> u.getRole() == UserRole.STAFF).count();
            long trackingManagerCount = allUsers.stream().filter(u -> u.getRole() == UserRole.TRACKING_MANAGER).count();
            
            // Count active users (default all users as active since User model doesn't have isActive field)
            long activeUsers = allUsers.size(); // All users are considered active
            long inactiveUsers = 0; // No inactive users since no isActive field
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", totalUsers);
            statistics.put("activeUsers", activeUsers);
            statistics.put("inactiveUsers", inactiveUsers);
            statistics.put("adminCount", adminCount);
            statistics.put("driverCount", driverCount);
            statistics.put("customerCount", customerCount);
            statistics.put("staffCount", staffCount);
            statistics.put("trackingManagerCount", trackingManagerCount);
            
            // Recent registrations (last 30 days)
            long recentRegistrations = allUsers.stream()
                .filter(user -> user.getCreatedAt() != null)
                .filter(user -> {
                    long daysSinceCreation = (System.currentTimeMillis() - user.getCreatedAt().getTime()) / (1000 * 60 * 60 * 24);
                    return daysSinceCreation <= 30;
                })
                .count();
            
            statistics.put("recentRegistrations", recentRegistrations);
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Map<String, Object>>> getUsersByRole(@PathVariable UserRole role) {
        try {
            List<User> users = userRepository.findByRole(role);
            
            List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("firstName", user.getFirstName());
                    userMap.put("lastName", user.getLastName());
                    userMap.put("email", user.getEmail());
                    userMap.put("phone", user.getPhone());
                    userMap.put("role", user.getRole());
                    userMap.put("isActive", true); // Default to active since User model doesn't have isActive field
                    userMap.put("createdAt", user.getCreatedAt());
                    return userMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        try {
            // Validate required fields
            if (!userData.containsKey("email") || !userData.containsKey("password") || 
                !userData.containsKey("firstName") || !userData.containsKey("lastName")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required fields: email, password, firstName, lastName"
                ));
            }

            String email = (String) userData.get("email");
            if (userService.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User with this email already exists"
                ));
            }

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode((String) userData.get("password")));
            newUser.setFirstName((String) userData.get("firstName"));
            newUser.setLastName((String) userData.get("lastName"));
            newUser.setPhone((String) userData.get("phone"));
            newUser.setAddress((String) userData.get("address"));
            newUser.setCity((String) userData.get("city"));
            newUser.setState((String) userData.get("state"));
            newUser.setZipCode((String) userData.get("zipCode"));
            newUser.setCountry((String) userData.getOrDefault("country", "South Africa"));
            
            // Set role
            if (userData.containsKey("role")) {
                try {
                    newUser.setRole(UserRole.valueOf(((String) userData.get("role")).toUpperCase()));
                } catch (IllegalArgumentException e) {
                    newUser.setRole(UserRole.CUSTOMER); // Default to CUSTOMER
                }
            } else {
                newUser.setRole(UserRole.CUSTOMER);
            }

            User createdUser = userService.createUser(newUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("user", convertToDTO(createdUser));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to create user: " + e.getMessage()
            ));
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", convertToDTO(user)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to fetch user: " + e.getMessage()
            ));
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> userData) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Update allowed fields
            if (userData.containsKey("firstName")) {
                user.setFirstName((String) userData.get("firstName"));
            }
            if (userData.containsKey("lastName")) {
                user.setLastName((String) userData.get("lastName"));
            }
            if (userData.containsKey("phone")) {
                user.setPhone((String) userData.get("phone"));
            }
            if (userData.containsKey("address")) {
                user.setAddress((String) userData.get("address"));
            }
            if (userData.containsKey("city")) {
                user.setCity((String) userData.get("city"));
            }
            if (userData.containsKey("state")) {
                user.setState((String) userData.get("state"));
            }
            if (userData.containsKey("zipCode")) {
                user.setZipCode((String) userData.get("zipCode"));
            }
            if (userData.containsKey("country")) {
                user.setCountry((String) userData.get("country"));
            }
            if (userData.containsKey("role")) {
                try {
                    user.setRole(UserRole.valueOf(((String) userData.get("role")).toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Invalid role, ignore
                }
            }
            if (userData.containsKey("password") && userData.get("password") != null) {
                String newPassword = (String) userData.get("password");
                if (!newPassword.isEmpty()) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                }
            }

            User updatedUser = userService.updateUser(userId, user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User updated successfully",
                "user", convertToDTO(updatedUser)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to update user: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
            }

            // Prevent deleting admin users
            if (user.getRole() == UserRole.ADMIN) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cannot delete admin users"
                ));
            }

            userService.deleteUser(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to delete user: " + e.getMessage()
            ));
        }
    }

    /**
     * Update user status (activate/deactivate)
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Note: User model doesn't have isActive field, so we'll just return success
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User status update not supported (User model doesn't have isActive field)");
            response.put("isActive", true); // Always return true since no isActive field
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error updating user status: " + e.getMessage()
            ));
        }
    }

    /**
     * Convert User to DTO
     */
    private Map<String, Object> convertToDTO(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("email", user.getEmail());
        userMap.put("phone", user.getPhone());
        userMap.put("address", user.getAddress());
        userMap.put("city", user.getCity());
        userMap.put("state", user.getState());
        userMap.put("zipCode", user.getZipCode());
        userMap.put("country", user.getCountry());
        userMap.put("role", user.getRole() != null ? user.getRole().name() : null);
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("updatedAt", user.getUpdatedAt());
        return userMap;
    }
}
