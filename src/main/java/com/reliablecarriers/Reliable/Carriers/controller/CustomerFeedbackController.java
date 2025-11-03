package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.CustomerFeedback;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.repository.CustomerFeedbackRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.service.CustomerFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for customer feedback and ratings
 */
@RestController
@RequestMapping("/api/customer/feedback")
@CrossOrigin(origins = "*")
public class CustomerFeedbackController {

    @Autowired
    private CustomerFeedbackService feedbackService;

    @Autowired
    private CustomerFeedbackRepository feedbackRepository;


    @Autowired
    private ShipmentRepository shipmentRepository;

    /**
     * Submit customer feedback/rating
     */
    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> feedbackData) {
        try {
            String trackingNumber = (String) feedbackData.get("trackingNumber");
            Integer rating = getIntegerValue(feedbackData.get("rating"));
            Integer driverServiceRating = getIntegerValue(feedbackData.get("driverServiceRating"));
            Integer deliverySpeedRating = getIntegerValue(feedbackData.get("deliverySpeedRating"));
            Integer packageConditionRating = getIntegerValue(feedbackData.get("packageConditionRating"));
            String comments = (String) feedbackData.get("comments");

            if (rating == null || rating < 1 || rating > 5) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Rating must be between 1 and 5"));
            }

            // Find shipment by tracking number (CustomerFeedback requires Shipment)
            Shipment shipment = null;
            
            if (trackingNumber != null) {
                Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
                if (shipmentOpt.isPresent()) {
                    shipment = shipmentOpt.get();
                }
            }

            if (shipment == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Shipment not found for tracking number: " + trackingNumber));
            }

            // Get customer email from sender or recipient
            String customerEmail = shipment.getSender() != null ? shipment.getSender().getEmail() : shipment.getRecipientEmail();
            String customerPhone = shipment.getSender() != null ? shipment.getSender().getPhone() : shipment.getRecipientPhone();

            // Use driverServiceRating if provided, otherwise use rating
            Integer driverCourtesyRating = driverServiceRating != null ? driverServiceRating : rating;
            
            // Use deliverySpeedRating if provided, otherwise use rating
            Integer speedRating = deliverySpeedRating != null ? deliverySpeedRating : rating;
            
            // Use packageConditionRating if provided, otherwise use rating
            Integer conditionRating = packageConditionRating != null ? packageConditionRating : rating;
            
            // Communication rating defaults to overall rating
            Integer communicationRating = rating;

            // Create feedback using the service
            CustomerFeedback feedback = feedbackService.createFeedback(
                shipment.getId(),
                customerEmail,
                customerPhone,
                rating,
                speedRating,
                driverCourtesyRating,
                conditionRating,
                communicationRating,
                comments,
                "DELIVERY"
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thank you for your feedback!",
                "feedbackId", feedback.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error submitting feedback: " + e.getMessage()));
        }
    }

    /**
     * Get feedback for a tracking number
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> getFeedbackByTracking(@PathVariable String trackingNumber) {
        try {
            Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);

            if (shipmentOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", true, "feedback", null));
            }

            Long shipmentId = shipmentOpt.get().getId();
            List<CustomerFeedback> feedbacks = feedbackRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedback", feedbacks.isEmpty() ? null : feedbacks.get(0)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error fetching feedback: " + e.getMessage()));
        }
    }

    /**
     * Get all feedback for current customer
     */
    @GetMapping("/my-feedback")
    public ResponseEntity<?> getMyFeedback(@RequestParam(required = false) String email) {
        try {
            // In production, get email from authentication context
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Email is required"));
            }

            List<CustomerFeedback> feedbacks = findByCustomerEmailOrderByCreatedAtDesc(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedbacks", feedbacks
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error fetching feedback: " + e.getMessage()));
        }
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    // Helper method to find feedback by customer email (since repository doesn't have this method)
    private List<CustomerFeedback> findByCustomerEmailOrderByCreatedAtDesc(String email) {
        return feedbackRepository.findAll().stream()
            .filter(f -> email != null && email.equalsIgnoreCase(f.getCustomerEmail()))
            .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
            .toList();
    }
}
