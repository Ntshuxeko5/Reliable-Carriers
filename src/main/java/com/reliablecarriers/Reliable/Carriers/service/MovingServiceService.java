package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.MovingServiceRequest;
import com.reliablecarriers.Reliable.Carriers.dto.MovingServiceResponse;
import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MovingServiceService {
    
    // Create new moving service
    MovingServiceResponse createMovingService(MovingServiceRequest request, Long customerId);
    
    // Get moving service by ID
    MovingServiceResponse getMovingServiceById(Long id);
    
    // Get all moving services
    List<MovingServiceResponse> getAllMovingServices();
    
    // Get moving services by customer
    List<MovingServiceResponse> getMovingServicesByCustomer(Long customerId);
    
    // Get moving services by driver
    List<MovingServiceResponse> getMovingServicesByDriver(Long driverId);
    
    // Get moving services by status
    List<MovingServiceResponse> getMovingServicesByStatus(ShipmentStatus status);
    
    // Get moving services by service type
    List<MovingServiceResponse> getMovingServicesByType(ServiceType serviceType);
    
    // Get pending services (no driver assigned)
    List<MovingServiceResponse> getPendingServices();
    
    // Assign driver to moving service
    MovingServiceResponse assignDriverToService(Long serviceId, Long driverId);
    
    // Update service status
    MovingServiceResponse updateServiceStatus(Long serviceId, ShipmentStatus status, String notes);
    
    // Update service details
    MovingServiceResponse updateMovingService(Long serviceId, MovingServiceRequest request);
    
    // Delete moving service
    void deleteMovingService(Long serviceId);
    
    // Calculate price for a service
    BigDecimal calculatePrice(Double distanceKm);
    
    // Calculate distance between two points
    Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2);
    
    // Get services by date range
    List<MovingServiceResponse> getServicesByDateRange(Date startDate, Date endDate);
    
    // Get services by location
    List<MovingServiceResponse> getServicesByLocation(String location);
    
    // Get services by distance range
    List<MovingServiceResponse> getServicesByDistanceRange(Double minDistance, Double maxDistance);
    
    // Get services by price range
    List<MovingServiceResponse> getServicesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Get today's scheduled services
    List<MovingServiceResponse> getTodayScheduledServices();
    
    // Get services for a specific date
    List<MovingServiceResponse> getServicesForDate(Date date);
    
    // Get completed services in date range
    List<MovingServiceResponse> getCompletedServicesInRange(Date startDate, Date endDate);
    
    // Get services by customer email
    List<MovingServiceResponse> getServicesByCustomerEmail(String email);
    
    // Get services by customer phone
    List<MovingServiceResponse> getServicesByCustomerPhone(String phone);
    
    // Get service statistics
    Map<String, Object> getServiceStatistics();
    
    // Get revenue statistics
    Map<String, Object> getRevenueStatistics(Date startDate, Date endDate);
    
    // Get top services by price
    List<MovingServiceResponse> getTopServicesByPrice(int limit);
    
    // Get top services by distance
    List<MovingServiceResponse> getTopServicesByDistance(int limit);
    
    // Search services
    List<MovingServiceResponse> searchServices(String query);
    
    // Get services by weight range
    List<MovingServiceResponse> getServicesByWeightRange(Double minWeight, Double maxWeight);
    
    // Get services by number of items
    List<MovingServiceResponse> getServicesByItemCount(Integer minItems, Integer maxItems);
    
    // Schedule service
    MovingServiceResponse scheduleService(Long serviceId, Date scheduledDate);
    
    // Complete service
    MovingServiceResponse completeService(Long serviceId, String completionNotes);
    
    // Cancel service
    MovingServiceResponse cancelService(Long serviceId, String cancellationReason);
    
    // Get service pricing breakdown
    Map<String, Object> getPricingBreakdown(Double distanceKm);
}
