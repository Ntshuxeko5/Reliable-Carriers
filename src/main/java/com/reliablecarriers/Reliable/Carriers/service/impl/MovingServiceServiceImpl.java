package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.MovingServiceRequest;
import com.reliablecarriers.Reliable.Carriers.dto.MovingServiceResponse;
import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.MovingServiceRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.MovingServiceService;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import com.reliablecarriers.Reliable.Carriers.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovingServiceServiceImpl implements MovingServiceService {

    @Autowired
    private MovingServiceRepository movingServiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PricingService pricingService;

    @Override
    public MovingServiceResponse createMovingService(MovingServiceRequest request, Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        MovingService movingService = new MovingService();
        movingService.setCustomer(customer);
        movingService.setServiceType(request.getServiceType());
        movingService.setPickupAddress(request.getPickupAddress());
        movingService.setDeliveryAddress(request.getDeliveryAddress());
        movingService.setDescription(request.getDescription());
        movingService.setWeightKg(request.getWeightKg());
        movingService.setNumberOfItems(request.getNumberOfItems());
        movingService.setRequestedDate(request.getRequestedDate());
        movingService.setScheduledDate(request.getScheduledDate());
        movingService.setSpecialInstructions(request.getSpecialInstructions());

        // Calculate distance and price using PricingService
        Double distance = request.calculateDistance();
        movingService.setDistanceKm(distance);
        
        // Get pricing details from PricingService
        BigDecimal basePrice = pricingService.calculateMovingServicePrice(request.getServiceType(), distance);
        movingService.setBasePrice(basePrice);
        movingService.setPricePerKm(new BigDecimal("25.00")); // R25 per km
        movingService.setMaxFreeDistanceKm(20); // 20km
        movingService.setMaxFreePrice(new BigDecimal("550.00")); // R550
        movingService.setTotalPrice(basePrice);

        MovingService savedService = movingServiceRepository.save(movingService);

        // Send notification
        notificationService.sendMovingServiceCreatedNotification(savedService);

        return new MovingServiceResponse(savedService);
    }

    @Override
    public MovingServiceResponse getMovingServiceById(Long id) {
        MovingService movingService = movingServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));
        return new MovingServiceResponse(movingService);
    }

    @Override
    public List<MovingServiceResponse> getAllMovingServices() {
        return movingServiceRepository.findAll().stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getMovingServicesByCustomer(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return movingServiceRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getMovingServicesByDriver(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        return movingServiceRepository.findByDriverOrderByCreatedAtDesc(driver).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getMovingServicesByStatus(ShipmentStatus status) {
        return movingServiceRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getMovingServicesByType(ServiceType serviceType) {
        return movingServiceRepository.findByServiceTypeOrderByCreatedAtDesc(serviceType).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getPendingServices() {
        return movingServiceRepository.findByDriverIsNullAndStatusOrderByCreatedAtAsc(ShipmentStatus.PENDING).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public MovingServiceResponse assignDriverToService(Long serviceId, Long driverId) {
        MovingService movingService = movingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));
        
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        movingService.setDriver(driver);
        movingService.setStatus(ShipmentStatus.ASSIGNED);
        MovingService savedService = movingServiceRepository.save(movingService);

        // Send notification
        notificationService.sendDriverAssignedToMovingServiceNotification(savedService, driver);

        return new MovingServiceResponse(savedService);
    }

    @Override
    public MovingServiceResponse updateServiceStatus(Long serviceId, ShipmentStatus status, String notes) {
        MovingService movingService = movingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));

        ShipmentStatus oldStatus = movingService.getStatus();
        movingService.setStatus(status);

        if (status == ShipmentStatus.DELIVERED) {
            movingService.setCompletedDate(new Date());
        }

        MovingService savedService = movingServiceRepository.save(movingService);

        // Send notification
        notificationService.sendMovingServiceStatusUpdateNotification(savedService, oldStatus, status, notes);

        return new MovingServiceResponse(savedService);
    }

    @Override
    public MovingServiceResponse updateMovingService(Long serviceId, MovingServiceRequest request) {
        MovingService movingService = movingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));

        movingService.setServiceType(request.getServiceType());
        movingService.setPickupAddress(request.getPickupAddress());
        movingService.setDeliveryAddress(request.getDeliveryAddress());
        movingService.setDescription(request.getDescription());
        movingService.setWeightKg(request.getWeightKg());
        movingService.setNumberOfItems(request.getNumberOfItems());
        movingService.setScheduledDate(request.getScheduledDate());
        movingService.setSpecialInstructions(request.getSpecialInstructions());

        // Recalculate distance and price
        Double distance = request.calculateDistance();
        movingService.setDistanceKm(distance);
        movingService.calculatePrice();

        MovingService savedService = movingServiceRepository.save(movingService);

        return new MovingServiceResponse(savedService);
    }

    @Override
    public void deleteMovingService(Long serviceId) {
        MovingService movingService = movingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));
        movingServiceRepository.delete(movingService);
    }

    @Override
    public BigDecimal calculatePrice(Double distanceKm) {
        return pricingService.calculateMovingServicePrice(ServiceType.FURNITURE, distanceKm);
    }

    @Override
    public Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    @Override
    public List<MovingServiceResponse> getServicesByDateRange(Date startDate, Date endDate) {
        return movingServiceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getServicesByLocation(String location) {
        return movingServiceRepository.findByLocationContaining(location).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getServicesByDistanceRange(Double minDistance, Double maxDistance) {
        return movingServiceRepository.findByDistanceKmBetweenOrderByDistanceKmAsc(minDistance, maxDistance).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getServicesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return movingServiceRepository.findByTotalPriceBetweenOrderByTotalPriceAsc(
                minPrice.doubleValue(), maxPrice.doubleValue()).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getTodayScheduledServices() {
        return movingServiceRepository.findByScheduledDateToday(new Date()).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getServicesForDate(Date date) {
        return movingServiceRepository.findByScheduledDateOn(date).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getCompletedServicesInRange(Date startDate, Date endDate) {
        return movingServiceRepository.findByStatusAndCompletedDateBetweenOrderByCompletedDateDesc(
                ShipmentStatus.DELIVERED, startDate, endDate).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getServicesByCustomerEmail(String email) {
        return movingServiceRepository.findByCustomerEmail(email).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getServicesByCustomerPhone(String phone) {
        return movingServiceRepository.findByCustomerPhone(phone).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalServices", movingServiceRepository.count());
        stats.put("pendingServices", movingServiceRepository.countByStatus(ShipmentStatus.PENDING));
        stats.put("assignedServices", movingServiceRepository.countByStatus(ShipmentStatus.ASSIGNED));
        stats.put("inProgressServices", movingServiceRepository.countByStatus(ShipmentStatus.IN_TRANSIT));
        stats.put("completedServices", movingServiceRepository.countByStatus(ShipmentStatus.DELIVERED));
        stats.put("cancelledServices", movingServiceRepository.countByStatus(ShipmentStatus.CANCELLED));
        
        // Service type breakdown
        for (ServiceType type : ServiceType.values()) {
            stats.put(type.name() + "Count", movingServiceRepository.countByServiceType(type));
        }
        
        // Average price
        Double avgPrice = movingServiceRepository.getAverageServicePrice();
        stats.put("averagePrice", avgPrice != null ? avgPrice : 0.0);
        
        return stats;
    }

    @Override
    public Map<String, Object> getRevenueStatistics(Date startDate, Date endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        Double totalRevenue = movingServiceRepository.getTotalRevenueForPeriod(startDate, endDate);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        
        return stats;
    }

    @Override
    public List<MovingServiceResponse> getTopServicesByPrice(int limit) {
        return movingServiceRepository.findTopByOrderByTotalPriceDesc().stream()
                .limit(limit)
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getTopServicesByDistance(int limit) {
        return movingServiceRepository.findTopByOrderByDistanceKmDesc().stream()
                .limit(limit)
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> searchServices(String query) {
        // This would need a more sophisticated search implementation
        // For now, search by location
        return getServicesByLocation(query);
    }

    @Override
    public List<MovingServiceResponse> getServicesByWeightRange(Double minWeight, Double maxWeight) {
        return movingServiceRepository.findByWeightKgBetweenOrderByWeightKgAsc(minWeight, maxWeight).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovingServiceResponse> getServicesByItemCount(Integer minItems, Integer maxItems) {
        return movingServiceRepository.findByNumberOfItemsBetweenOrderByNumberOfItemsAsc(minItems, maxItems).stream()
                .map(MovingServiceResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public MovingServiceResponse scheduleService(Long serviceId, Date scheduledDate) {
        MovingService movingService = movingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));
        
        movingService.setScheduledDate(scheduledDate);
        MovingService savedService = movingServiceRepository.save(movingService);

        // Send notification
        notificationService.sendMovingServiceScheduledNotification(savedService, scheduledDate);

        return new MovingServiceResponse(savedService);
    }

    @Override
    public MovingServiceResponse completeService(Long serviceId, String completionNotes) {
        MovingService movingService = movingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));
        
        movingService.setStatus(ShipmentStatus.DELIVERED);
        movingService.setCompletedDate(new Date());
        MovingService savedService = movingServiceRepository.save(movingService);

        // Send notification
        notificationService.sendMovingServiceCompletedNotification(savedService, completionNotes);

        return new MovingServiceResponse(savedService);
    }

    @Override
    public MovingServiceResponse cancelService(Long serviceId, String cancellationReason) {
        MovingService movingService = movingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Moving service not found"));
        
        movingService.setStatus(ShipmentStatus.CANCELLED);
        MovingService savedService = movingServiceRepository.save(movingService);

        // Send notification
        notificationService.sendMovingServiceCancelledNotification(savedService, cancellationReason);

        return new MovingServiceResponse(savedService);
    }

    @Override
    public Map<String, Object> getPricingBreakdown(Double distanceKm) {
        return pricingService.getMovingServicePricingBreakdown(distanceKm);
    }
}
