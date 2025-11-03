package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
       Optional<User> findByPhone(String phone);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByRoleAndIsOnline(UserRole role, Boolean isOnline);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    List<User> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
    
    // Analytics methods
    long countByRole(UserRole role);
    
    long countByRoleAndCreatedAtBetween(UserRole role, Date startDate, Date endDate);
    
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN Shipment s ON s.sender = u " +
           "WHERE u.role = 'CUSTOMER' AND s.createdAt BETWEEN :startDate AND :endDate")
    long countActiveCustomersByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN DriverLocation dl ON dl.driverId = u.id " +
           "WHERE u.role = 'DRIVER' AND dl.timestamp BETWEEN :startDate AND :endDate")
    long countActiveDriversByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}