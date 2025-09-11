package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.AutomatedDispatch;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.util.List;
import java.util.Map;

public interface AutomatedDispatchService {
    
    /**
     * Automatically assign driver to shipment based on dispatch rules
     */
    User assignDriverToShipment(Shipment shipment);
    
    /**
     * Create new dispatch rule
     */
    AutomatedDispatch createDispatchRule(AutomatedDispatch rule);
    
    /**
     * Update dispatch rule
     */
    AutomatedDispatch updateDispatchRule(Long ruleId, AutomatedDispatch rule);
    
    /**
     * Delete dispatch rule
     */
    void deleteDispatchRule(Long ruleId);
    
    /**
     * Get dispatch rule by ID
     */
    AutomatedDispatch getDispatchRuleById(Long ruleId);
    
    /**
     * Get all active dispatch rules
     */
    List<AutomatedDispatch> getAllActiveDispatchRules();
    
    /**
     * Get dispatch rules by service type
     */
    List<AutomatedDispatch> getDispatchRulesByServiceType(String serviceType);
    
    /**
     * Get dispatch rules by location
     */
    List<AutomatedDispatch> getDispatchRulesByLocation(String city, String state);
    
    /**
     * Enable/disable dispatch rule
     */
    AutomatedDispatch toggleDispatchRule(Long ruleId, Boolean isActive);
    
    /**
     * Get best matching dispatch rule for shipment
     */
    AutomatedDispatch getBestMatchingRule(Shipment shipment);
    
    /**
     * Get available drivers for shipment based on rules
     */
    List<User> getAvailableDrivers(Shipment shipment);
    
    /**
     * Get optimal driver for shipment
     */
    User getOptimalDriver(Shipment shipment);
    
    /**
     * Calculate driver workload
     */
    Map<String, Object> calculateDriverWorkload(Long driverId);
    
    /**
     * Get driver performance metrics
     */
    Map<String, Object> getDriverPerformanceMetrics(Long driverId);
    
    /**
     * Optimize driver assignments for multiple shipments
     */
    Map<Long, Long> optimizeAssignments(List<Shipment> shipments);
    
    /**
     * Get dispatch statistics
     */
    Map<String, Object> getDispatchStatistics();
    
    /**
     * Validate dispatch rule
     */
    Map<String, Object> validateDispatchRule(AutomatedDispatch rule);
    
    /**
     * Test dispatch rule with sample shipment
     */
    Map<String, Object> testDispatchRule(Long ruleId, Shipment sampleShipment);
    
    /**
     * Get dispatch history
     */
    List<Map<String, Object>> getDispatchHistory(Long driverId, String startDate, String endDate);
    
    /**
     * Get dispatch efficiency metrics
     */
    Map<String, Object> getDispatchEfficiencyMetrics();
    
    /**
     * Bulk assign drivers to shipments
     */
    List<Map<String, Object>> bulkAssignDrivers(List<Long> shipmentIds);
    
    /**
     * Get driver availability status
     */
    Map<String, Object> getDriverAvailabilityStatus(Long driverId);
    
    /**
     * Update driver availability
     */
    void updateDriverAvailability(Long driverId, Boolean isAvailable);
    
    /**
     * Get dispatch queue status
     */
    Map<String, Object> getDispatchQueueStatus();
    
    /**
     * Prioritize shipments in dispatch queue
     */
    void prioritizeShipments(List<Long> shipmentIds, Integer priority);
    
    /**
     * Get dispatch rule performance
     */
    Map<String, Object> getDispatchRulePerformance(Long ruleId);
}
