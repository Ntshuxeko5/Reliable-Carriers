package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Delivery Estimation Service
 * Calculates accurate delivery estimates based on multiple factors
 */
@Service
public class DeliveryEstimationService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEstimationService.class);

    /**
     * Calculate estimated delivery date and time
     */
    public Map<String, Object> calculateDeliveryEstimate(Booking booking, double distanceKm) {
        Map<String, Object> estimate = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime estimatedDelivery = now;
            
            // Base delivery time calculation
            int baseHours = calculateBaseDeliveryHours(booking.getServiceType(), distanceKm);
            
            // Add processing time
            int processingHours = calculateProcessingTime(booking.getServiceType());
            
            // Add distance-based travel time
            int travelHours = calculateTravelTime(distanceKm, booking.getServiceType());
            
            // Add service type specific delays
            int serviceDelayHours = getServiceTypeDelay(booking.getServiceType());
            
            // Total estimated hours
            int totalHours = baseHours + processingHours + travelHours + serviceDelayHours;
            
            // Apply business hours and weekend adjustments
            estimatedDelivery = adjustForBusinessHours(now.plusHours(totalHours), booking.getServiceType());
            
            // Format the results
            estimate.put("estimatedDeliveryDate", estimatedDelivery.toLocalDate().toString());
            estimate.put("estimatedDeliveryTime", estimatedDelivery.format(DateTimeFormatter.ofPattern("HH:mm")));
            estimate.put("estimatedDeliveryDateTime", estimatedDelivery.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            estimate.put("deliveryWindow", getDeliveryWindow(estimatedDelivery));
            estimate.put("totalEstimatedHours", totalHours);
            estimate.put("distanceKm", distanceKm);
            estimate.put("serviceType", booking.getServiceType().getDisplayName());
            
            // Confidence level based on factors
            int confidenceLevel = calculateConfidenceLevel(booking, distanceKm);
            estimate.put("confidenceLevel", confidenceLevel);
            estimate.put("confidenceDescription", getConfidenceDescription(confidenceLevel));
            
            logger.info("Delivery estimate calculated for booking {}: {} ({} hours)", 
                booking.getBookingNumber(), estimatedDelivery.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")), totalHours);
            
        } catch (Exception e) {
            logger.error("Error calculating delivery estimate for booking: {}", booking.getBookingNumber(), e);
            // Fallback estimate
            estimate.put("estimatedDeliveryDate", LocalDateTime.now().plusDays(2).toLocalDate().toString());
            estimate.put("estimatedDeliveryTime", "17:00");
            estimate.put("estimatedDeliveryDateTime", "Within 2 business days");
            estimate.put("confidenceLevel", 50);
        }
        
        return estimate;
    }

    /**
     * Calculate base delivery hours by service type and distance
     */
    private int calculateBaseDeliveryHours(ServiceType serviceType, double distanceKm) {
        switch (serviceType) {
            case SAME_DAY:
                return distanceKm < 50 ? 4 : 8;
            case OVERNIGHT:
                return 24;
            case URGENT:
                return distanceKm < 100 ? 12 : 18;
            case EXPRESS_DELIVERY:
                return distanceKm < 100 ? 48 : 72;
            case ECONOMY:
                return distanceKm < 200 ? 72 : 120;
            default:
                return 48;
        }
    }

    /**
     * Calculate processing time (pickup, sorting, loading)
     */
    private int calculateProcessingTime(ServiceType serviceType) {
        switch (serviceType) {
            case SAME_DAY:
                return 1; // 1 hour processing
            case OVERNIGHT:
            case URGENT:
                return 2; // 2 hours processing
            case EXPRESS_DELIVERY:
                return 4; // 4 hours processing
            case ECONOMY:
                return 6; // 6 hours processing
            default:
                return 4;
        }
    }

    /**
     * Calculate travel time based on distance and service type
     */
    private int calculateTravelTime(double distanceKm, ServiceType serviceType) {
        // Average speeds by service type (km/h including stops)
        double averageSpeed;
        switch (serviceType) {
            case SAME_DAY:
            case URGENT:
                averageSpeed = 60; // Direct routes, minimal stops
                break;
            case OVERNIGHT:
                averageSpeed = 80; // Highway speeds, overnight travel
                break;
            case EXPRESS_DELIVERY:
                averageSpeed = 50; // Regular routes with stops
                break;
            case ECONOMY:
                averageSpeed = 40; // Multiple stops, consolidated routes
                break;
            default:
                averageSpeed = 50;
        }
        
        return (int) Math.ceil(distanceKm / averageSpeed);
    }

    /**
     * Get service type specific delays
     */
    private int getServiceTypeDelay(ServiceType serviceType) {
        switch (serviceType) {
            case SAME_DAY:
                return 0; // No delays for same day
            case OVERNIGHT:
                return 2; // 2 hour buffer
            case URGENT:
                return 4; // 4 hour buffer
            case EXPRESS_DELIVERY:
                return 8; // 8 hour buffer
            case ECONOMY:
                return 12; // 12 hour buffer
            default:
                return 8;
        }
    }

    /**
     * Adjust delivery time for business hours and weekends
     */
    private LocalDateTime adjustForBusinessHours(LocalDateTime estimatedTime, ServiceType serviceType) {
        // Same day and overnight services operate 24/7
        if (serviceType == ServiceType.SAME_DAY || serviceType == ServiceType.OVERNIGHT) {
            return estimatedTime;
        }
        
        // Other services deliver during business hours (8 AM - 6 PM, Mon-Sat)
        LocalDateTime adjusted = estimatedTime;
        
        // If delivery falls outside business hours, move to next business day
        while (adjusted.getDayOfWeek() == DayOfWeek.SUNDAY || 
               adjusted.getHour() < 8 || adjusted.getHour() >= 18) {
            
            if (adjusted.getDayOfWeek() == DayOfWeek.SUNDAY) {
                // Move to Monday 8 AM
                adjusted = adjusted.plusDays(1).withHour(8).withMinute(0);
            } else if (adjusted.getHour() < 8) {
                // Move to 8 AM same day
                adjusted = adjusted.withHour(8).withMinute(0);
            } else if (adjusted.getHour() >= 18) {
                // Move to next business day 8 AM
                adjusted = adjusted.plusDays(1).withHour(8).withMinute(0);
                if (adjusted.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    adjusted = adjusted.plusDays(1); // Skip Sunday
                }
            }
        }
        
        return adjusted;
    }

    /**
     * Get delivery time window
     */
    private String getDeliveryWindow(LocalDateTime estimatedTime) {
        int hour = estimatedTime.getHour();
        
        if (hour >= 8 && hour < 12) {
            return "Morning (8 AM - 12 PM)";
        } else if (hour >= 12 && hour < 15) {
            return "Afternoon (12 PM - 3 PM)";
        } else if (hour >= 15 && hour < 18) {
            return "Late Afternoon (3 PM - 6 PM)";
        } else if (hour >= 18 && hour < 21) {
            return "Evening (6 PM - 9 PM)";
        } else {
            return "Extended Hours";
        }
    }

    /**
     * Calculate confidence level for the estimate
     */
    private int calculateConfidenceLevel(Booking booking, double distanceKm) {
        int confidence = 85; // Base confidence
        
        // Adjust based on distance
        if (distanceKm > 500) {
            confidence -= 15; // Long distance reduces confidence
        } else if (distanceKm > 200) {
            confidence -= 10;
        } else if (distanceKm < 50) {
            confidence += 10; // Short distance increases confidence
        }
        
        // Adjust based on service type
        switch (booking.getServiceType()) {
            case SAME_DAY:
                confidence += 5; // More predictable
                break;
            case ECONOMY:
                confidence -= 10; // More variables
                break;
            case EXPRESS_DELIVERY:
            case OVERNIGHT:
            case URGENT:
            case FURNITURE:
            case MOVING:
            case SAME_DAY_DELIVERY:
            case LOAD_TRANSPORT:
                // Default handling for other service types
                break;
        }
        
        // Adjust based on package characteristics
        if (booking.getWeight() > 50) {
            confidence -= 5; // Heavy packages may have delays
        }
        
        return Math.max(50, Math.min(95, confidence));
    }

    /**
     * Get confidence description
     */
    private String getConfidenceDescription(int confidenceLevel) {
        if (confidenceLevel >= 90) {
            return "Very High - Delivery time is highly predictable";
        } else if (confidenceLevel >= 80) {
            return "High - Delivery should arrive within estimated window";
        } else if (confidenceLevel >= 70) {
            return "Good - Minor delays possible due to distance/conditions";
        } else if (confidenceLevel >= 60) {
            return "Moderate - Some variability expected";
        } else {
            return "Variable - Multiple factors may affect delivery time";
        }
    }

    /**
     * Update delivery estimate based on current status
     */
    public Map<String, Object> updateDeliveryEstimate(Booking booking, ShipmentStatus currentStatus, 
                                                     double remainingDistanceKm) {
        Map<String, Object> updatedEstimate = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newEstimate = now;
            
            switch (currentStatus) {
                case ASSIGNED:
                    // Add time to pickup location + original estimate
                    newEstimate = now.plusHours(calculateTimeToPickup(booking) + 
                        calculateRemainingDeliveryTime(booking, remainingDistanceKm));
                    break;
                    
                case PICKED_UP:
                case IN_TRANSIT:
                    // Calculate remaining delivery time
                    newEstimate = now.plusHours(calculateRemainingDeliveryTime(booking, remainingDistanceKm));
                    break;
                    
                case OUT_FOR_DELIVERY:
                    // Should be delivered within 2-4 hours
                    newEstimate = now.plusHours(booking.getServiceType() == ServiceType.SAME_DAY ? 2 : 4);
                    break;
                    
                default:
                    return calculateDeliveryEstimate(booking, remainingDistanceKm);
            }
            
            // Adjust for business hours
            newEstimate = adjustForBusinessHours(newEstimate, booking.getServiceType());
            
            updatedEstimate.put("estimatedDeliveryDate", newEstimate.toLocalDate().toString());
            updatedEstimate.put("estimatedDeliveryTime", newEstimate.format(DateTimeFormatter.ofPattern("HH:mm")));
            updatedEstimate.put("estimatedDeliveryDateTime", newEstimate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            updatedEstimate.put("deliveryWindow", getDeliveryWindow(newEstimate));
            updatedEstimate.put("status", currentStatus.toString());
            updatedEstimate.put("remainingDistanceKm", remainingDistanceKm);
            
            logger.info("Updated delivery estimate for booking {}: {}", 
                booking.getBookingNumber(), newEstimate.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")));
            
        } catch (Exception e) {
            logger.error("Error updating delivery estimate for booking: {}", booking.getBookingNumber(), e);
        }
        
        return updatedEstimate;
    }

    /**
     * Calculate time to pickup location
     */
    private int calculateTimeToPickup(Booking booking) {
        // Simplified - in real system, would use Google Maps API
        // Assume 1-3 hours depending on service type
        switch (booking.getServiceType()) {
            case SAME_DAY:
                return 1;
            case OVERNIGHT:
            case URGENT:
                return 2;
            default:
                return 3;
        }
    }

    /**
     * Calculate remaining delivery time from current location
     */
    private int calculateRemainingDeliveryTime(Booking booking, double remainingDistanceKm) {
        int travelTime = calculateTravelTime(remainingDistanceKm, booking.getServiceType());
        int bufferTime = getServiceTypeDelay(booking.getServiceType()) / 2; // Half the original buffer
        
        return travelTime + bufferTime;
    }

    /**
     * Get delivery estimate for customer display
     */
    public String getCustomerFriendlyEstimate(Map<String, Object> estimate) {
        try {
            String dateTime = (String) estimate.get("estimatedDeliveryDateTime");
            String window = (String) estimate.get("deliveryWindow");
            Integer confidence = (Integer) estimate.get("confidenceLevel");
            
            StringBuilder friendly = new StringBuilder();
            friendly.append("Expected delivery: ").append(dateTime);
            
            if (window != null) {
                friendly.append(" (").append(window).append(")");
            }
            
            if (confidence != null && confidence < 80) {
                friendly.append(" - Times may vary due to distance and service conditions");
            }
            
            return friendly.toString();
            
        } catch (Exception e) {
            return "Delivery estimate will be provided once your package is processed";
        }
    }
}
