package com.reliablecarriers.Reliable.Carriers.service;

import java.util.List;
import java.util.Map;

/**
 * AI-Powered Chatbot Service
 * Provides intelligent responses using AI or rule-based fallback
 */
public interface ChatbotService {
    
    /**
     * Get AI-powered response to user message
     * @param message User's message
     * @param chatId Chat session ID
     * @param userEmail User's email for context
     * @param conversationHistory Previous messages for context
     * @return Response from chatbot
     */
    ChatbotResponse getResponse(String message, String chatId, String userEmail, List<Map<String, Object>> conversationHistory);
    
    /**
     * Analyze user intent from message
     * @param message User's message
     * @return Intent category
     */
    String analyzeIntent(String message);
    
    /**
     * Get quick response suggestions based on user message
     * @param message User's message
     * @return List of suggested responses
     */
    List<String> getQuickResponses(String message);
    
    /**
     * Check if message requires human agent
     * @param message User's message
     * @param sentiment Sentiment score
     * @return true if human agent needed
     */
    boolean requiresHumanAgent(String message, double sentiment);
    
    /**
     * Response object containing message and metadata
     */
    class ChatbotResponse {
        private String message;
        private String intent;
        private double confidence;
        private boolean requiresHuman;
        private List<String> quickResponses;
        private Map<String, Object> metadata;
        
        public ChatbotResponse(String message, String intent, double confidence) {
            this.message = message;
            this.intent = intent;
            this.confidence = confidence;
            this.requiresHuman = false;
        }
        
        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getIntent() { return intent; }
        public void setIntent(String intent) { this.intent = intent; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public boolean isRequiresHuman() { return requiresHuman; }
        public void setRequiresHuman(boolean requiresHuman) { this.requiresHuman = requiresHuman; }
        public List<String> getQuickResponses() { return quickResponses; }
        public void setQuickResponses(List<String> quickResponses) { this.quickResponses = quickResponses; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
