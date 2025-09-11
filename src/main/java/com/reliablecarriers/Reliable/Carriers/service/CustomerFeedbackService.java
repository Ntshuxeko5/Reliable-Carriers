package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.CustomerFeedback;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;

import java.util.List;
import java.util.Map;

public interface CustomerFeedbackService {
    
    /**
     * Create customer feedback for a shipment
     */
    CustomerFeedback createFeedback(Long shipmentId, String customerEmail, String customerPhone,
                                   Integer overallRating, Integer deliverySpeedRating, 
                                   Integer driverCourtesyRating, Integer packageConditionRating,
                                   Integer communicationRating, String comments, String feedbackType);
    
    /**
     * Get feedback by ID
     */
    CustomerFeedback getFeedbackById(Long feedbackId);
    
    /**
     * Get feedback by shipment ID
     */
    CustomerFeedback getFeedbackByShipment(Long shipmentId);
    
    /**
     * Get all feedback for a customer
     */
    List<CustomerFeedback> getFeedbackByCustomer(String email);
    
    /**
     * Get feedback by rating range
     */
    List<CustomerFeedback> getFeedbackByRatingRange(Integer minRating, Integer maxRating);
    
    /**
     * Get feedback by sentiment
     */
    List<CustomerFeedback> getFeedbackBySentiment(String sentiment);
    
    /**
     * Get feedback by date range
     */
    List<CustomerFeedback> getFeedbackByDateRange(String startDate, String endDate);
    
    /**
     * Get feedback by feedback type
     */
    List<CustomerFeedback> getFeedbackByType(String feedbackType);
    
    /**
     * Get feedback statistics
     */
    Map<String, Object> getFeedbackStatistics();
    
    /**
     * Get average ratings by category
     */
    Map<String, Double> getAverageRatingsByCategory();
    
    /**
     * Get customer satisfaction score
     */
    Double getCustomerSatisfactionScore();
    
    /**
     * Get net promoter score (NPS)
     */
    Integer getNetPromoterScore();
    
    /**
     * Analyze feedback sentiment
     */
    String analyzeSentiment(String comments);
    
    /**
     * Respond to customer feedback
     */
    CustomerFeedback respondToFeedback(Long feedbackId, String adminResponse, String respondedBy);
    
    /**
     * Mark feedback as resolved
     */
    CustomerFeedback markFeedbackResolved(Long feedbackId);
    
    /**
     * Get feedback trends over time
     */
    Map<String, Object> getFeedbackTrends(String startDate, String endDate);
    
    /**
     * Get top feedback issues
     */
    List<Map<String, Object>> getTopFeedbackIssues();
    
    /**
     * Get driver performance based on feedback
     */
    Map<String, Object> getDriverFeedbackPerformance(Long driverId);
    
    /**
     * Get service type performance based on feedback
     */
    Map<String, Object> getServiceTypeFeedbackPerformance(String serviceType);
    
    /**
     * Generate feedback report
     */
    byte[] generateFeedbackReport(String startDate, String endDate);
    
    /**
     * Send feedback request to customer
     */
    void sendFeedbackRequest(Long shipmentId);
    
    /**
     * Get feedback response rate
     */
    Double getFeedbackResponseRate();
    
    /**
     * Get feedback by driver
     */
    List<CustomerFeedback> getFeedbackByDriver(Long driverId);
    
    /**
     * Get feedback summary for dashboard
     */
    Map<String, Object> getFeedbackSummary();
    
    /**
     * Export feedback data
     */
    byte[] exportFeedbackData(String startDate, String endDate, String format);
    
    /**
     * Get feedback improvement suggestions
     */
    List<String> getFeedbackImprovementSuggestions();
    
    /**
     * Get customer feedback history
     */
    List<CustomerFeedback> getCustomerFeedbackHistory(String email);
    
    /**
     * Update feedback
     */
    CustomerFeedback updateFeedback(Long feedbackId, CustomerFeedback feedback);
    
    /**
     * Delete feedback
     */
    void deleteFeedback(Long feedbackId);
    
    /**
     * Get feedback by status
     */
    List<CustomerFeedback> getFeedbackByStatus(String status);
    
    /**
     * Get feedback analytics
     */
    Map<String, Object> getFeedbackAnalytics();
}
