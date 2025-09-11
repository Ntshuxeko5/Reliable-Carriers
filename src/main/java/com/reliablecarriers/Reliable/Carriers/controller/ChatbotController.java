package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final Map<String, String> responses = new HashMap<>();
    private final Map<String, String> patterns = new HashMap<>();

    public ChatbotController() {
        initializeResponses();
        initializePatterns();
    }

    private void initializeResponses() {
        responses.put("greeting", "Hello! Welcome to Reliable Carriers. How can I help you today?");
        responses.put("tracking", "To track your package, please visit our tracking page or provide your tracking number. You can also call our customer service at 1-800-RELIABLE.");
        responses.put("pricing", "Our pricing varies based on package size, weight, and delivery distance. Please visit our booking page for a quote or contact us for bulk shipping rates.");
        responses.put("delivery_time", "Standard delivery takes 3-5 business days. Express delivery is available for 1-2 business days. National shipping takes 3-10 business days.");
        responses.put("pickup", "We offer pickup services from your location. Please schedule a pickup through our booking system or call us at least 24 hours in advance.");
        responses.put("damage", "If your package is damaged, please contact us immediately with photos and your tracking number. We have insurance coverage for all shipments.");
        responses.put("refund", "For refund inquiries, please contact our customer service with your tracking number and reason for refund. We'll process your request within 3-5 business days.");
        responses.put("contact", "You can reach us at:\nPhone: +27 78 478 9875\nEmail: support@reliablecarriers.com\nLive Chat: Available 24/7 on our website");
        responses.put("hours", "Our customer service is available 24/7. Our pickup and delivery services operate Monday-Friday 8 AM to 6 PM, and Saturday 9 AM to 3 PM.");
        responses.put("locations", "We have locations nationwide. Please visit our website to find the nearest pickup/delivery location or call us for assistance.");
        responses.put("default", "I'm sorry, I didn't understand your question. Please try rephrasing or contact our customer service for assistance.");
    }

    private void initializePatterns() {
        patterns.put("greeting", "\\b(hi|hello|hey|good morning|good afternoon|good evening)\\b");
        patterns.put("tracking", "\\b(track|tracking|where is|status|package|delivery status)\\b");
        patterns.put("pricing", "\\b(price|cost|rate|how much|fee|charge)\\b");
        patterns.put("delivery_time", "\\b(how long|delivery time|when|arrive|estimated|duration)\\b");
        patterns.put("pickup", "\\b(pickup|pick up|collect|schedule pickup)\\b");
        patterns.put("damage", "\\b(damage|broken|damaged|problem|issue|defective)\\b");
        patterns.put("refund", "\\b(refund|return|money back|cancel|reimbursement)\\b");
        patterns.put("contact", "\\b(contact|phone|email|call|speak|talk to)\\b");
        patterns.put("hours", "\\b(hours|time|open|close|available|business hours)\\b");
        patterns.put("locations", "\\b(location|where|address|office|branch|facility)\\b");
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> handleMessage(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("response", "Please provide a message."));
        }

        String response = getResponse(userMessage.toLowerCase());
        return ResponseEntity.ok(Map.of("response", response));
    }

    private String getResponse(String message) {
        for (Map.Entry<String, String> pattern : patterns.entrySet()) {
            if (Pattern.compile(pattern.getValue(), Pattern.CASE_INSENSITIVE).matcher(message).find()) {
                return responses.get(pattern.getKey());
            }
        }
        return responses.get("default");
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Chatbot is running"));
    }
}
