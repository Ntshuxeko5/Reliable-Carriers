package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.PhotoVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {
    
    /**
     * Find photo verifications by shipment ID
     */
    List<PhotoVerification> findByShipmentId(Long shipmentId);
    
    /**
     * Find photo verifications by driver ID
     */
    List<PhotoVerification> findByDriverId(Long driverId);
    
    /**
     * Find photo verifications by verification type
     */
    List<PhotoVerification> findByVerificationType(String verificationType);
    
    /**
     * Find verified photo verifications
     */
    List<PhotoVerification> findByVerifiedTrue();
    
    /**
     * Find unverified photo verifications
     */
    List<PhotoVerification> findByVerifiedFalse();
    
    /**
     * Find photo verifications by date range
     */
    @Query("SELECT pv FROM PhotoVerification pv WHERE pv.createdAt BETWEEN :startDate AND :endDate")
    List<PhotoVerification> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find photo verifications by shipment and verification type
     */
    List<PhotoVerification> findByShipmentIdAndVerificationType(Long shipmentId, String verificationType);
    
    /**
     * Find photo verifications by driver and date range
     */
    @Query("SELECT pv FROM PhotoVerification pv WHERE pv.driverId = :driverId AND pv.createdAt BETWEEN :startDate AND :endDate")
    List<PhotoVerification> findByDriverIdAndCreatedAtBetween(@Param("driverId") Long driverId,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count photo verifications by shipment
     */
    long countByShipmentId(Long shipmentId);
    
    /**
     * Count verified photo verifications by shipment
     */
    long countByShipmentIdAndVerifiedTrue(Long shipmentId);
    
    /**
     * Find photo verifications requiring verification
     */
    @Query("SELECT pv FROM PhotoVerification pv WHERE pv.verified = false AND pv.createdAt < :cutoffDate")
    List<PhotoVerification> findPendingVerifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}

