package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.AutomatedDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AutomatedDispatchRepository extends JpaRepository<AutomatedDispatch, Long> {
    
    /**
     * Find dispatches by driver ID
     */
    List<AutomatedDispatch> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    
    /**
     * Find dispatches by shipment ID
     */
    List<AutomatedDispatch> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
    
    /**
     * Find dispatches by status
     */
    List<AutomatedDispatch> findByDispatchStatusOrderByCreatedAtDesc(String dispatchStatus);
    
    /**
     * Find dispatches by priority level
     */
    List<AutomatedDispatch> findByPriorityLevelOrderByCreatedAtDesc(String priorityLevel);
    
    /**
     * Find dispatches by type
     */
    List<AutomatedDispatch> findByDispatchTypeOrderByCreatedAtDesc(String dispatchType);
    
    /**
     * Find pending dispatches
     */
    List<AutomatedDispatch> findByDispatchStatusOrderByPriorityLevelDescCreatedAtAsc(String status);
    
    /**
     * Find dispatches by driver and status
     */
    List<AutomatedDispatch> findByDriverIdAndDispatchStatusOrderByCreatedAtDesc(Long driverId, String status);
    
    /**
     * Find dispatches created after a specific date
     */
    List<AutomatedDispatch> findByCreatedAtAfterOrderByCreatedAtDesc(Date date);
    
    /**
     * Find dispatches by vehicle ID
     */
    List<AutomatedDispatch> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);
    
    /**
     * Find high priority dispatches
     */
    @Query("SELECT d FROM AutomatedDispatch d WHERE d.priorityLevel IN ('HIGH', 'URGENT') AND d.dispatchStatus = 'PENDING' ORDER BY d.priorityLevel DESC, d.createdAt ASC")
    List<AutomatedDispatch> findHighPriorityPendingDispatches();
    
    /**
     * Get dispatch statistics by status
     */
    @Query("SELECT d.dispatchStatus, COUNT(d) FROM AutomatedDispatch d GROUP BY d.dispatchStatus")
    List<Object[]> getDispatchStatisticsByStatus();
    
    /**
     * Get dispatch statistics by priority
     */
    @Query("SELECT d.priorityLevel, COUNT(d) FROM AutomatedDispatch d GROUP BY d.priorityLevel")
    List<Object[]> getDispatchStatisticsByPriority();
    
    /**
     * Find dispatches with optimization score above threshold
     */
    @Query("SELECT d FROM AutomatedDispatch d WHERE d.optimizationScore >= :threshold ORDER BY d.optimizationScore DESC")
    List<AutomatedDispatch> findDispatchesWithHighOptimizationScore(@Param("threshold") Double threshold);
    
    /**
     * Get average optimization score by driver
     */
    @Query("SELECT d.driverId, AVG(d.optimizationScore) FROM AutomatedDispatch d WHERE d.driverId IS NOT NULL GROUP BY d.driverId")
    List<Object[]> getAverageOptimizationScoreByDriver();
    
    /**
     * Find dispatches for specific time range
     */
    @Query("SELECT d FROM AutomatedDispatch d WHERE d.estimatedPickupTime BETWEEN :startTime AND :endTime ORDER BY d.estimatedPickupTime ASC")
    List<AutomatedDispatch> findDispatchesInTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * Find completed dispatches for a driver in date range
     */
    @Query("SELECT d FROM AutomatedDispatch d WHERE d.driverId = :driverId AND d.dispatchStatus = 'COMPLETED' AND d.actualDeliveryTime BETWEEN :startDate AND :endDate ORDER BY d.actualDeliveryTime DESC")
    List<AutomatedDispatch> findCompletedDispatchesByDriverInDateRange(@Param("driverId") Long driverId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
