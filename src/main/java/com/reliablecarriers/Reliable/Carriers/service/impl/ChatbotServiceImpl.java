package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Pattern;

/**
 * AI-Powered Chatbot Service Implementation
 * Uses OpenAI API with rule-based fallback
 */
@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.enabled:false}")
    private boolean aiEnabled;

    @Value("${openai.api.model:gpt-3.5-turbo}")
    private String aiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    // FAQ Knowledge Base - Comprehensive free responses
    private static final Map<String, String> FAQ_RESPONSES = new HashMap<>();
    static {
        FAQ_RESPONSES.put("track", "You can track your package by entering your tracking number on our tracking page. The tracking number starts with 'RC' followed by numbers and letters. You'll get real-time updates about your package location and status. Visit /tracking to track your package now!");
        FAQ_RESPONSES.put("tracking", "To track your package:\n1. Go to our tracking page\n2. Enter your tracking number (format: RC followed by 8-12 characters)\n3. View real-time updates including current location and estimated delivery time.\n\nYou'll receive your tracking number via email after booking.");
        FAQ_RESPONSES.put("where is", "I can help you find your package! Please provide your tracking number, or I can guide you to our tracking page. Tracking numbers start with 'RC'.");
        FAQ_RESPONSES.put("delivery time", "Our delivery times vary by service type:\n• Express Delivery: Same day or next business day\n• Standard Delivery: 2-3 business days\n• Economy Delivery: 5-7 business days\n\nDelivery times also depend on the distance between pickup and delivery locations within South Africa. Use our quote calculator to see estimated delivery times for your specific route.");
        FAQ_RESPONSES.put("delivery", "We offer multiple delivery options:\n• Same-day delivery (for Express service)\n• Next-day delivery\n• Standard 2-3 day delivery\n• Economy 5-7 day delivery\n\nDelivery times are estimates and may vary based on weather, traffic, and other factors. You'll receive real-time tracking updates via email and SMS.");
        FAQ_RESPONSES.put("when", "Delivery times depend on your selected service:\n• Express: Same or next day\n• Standard: 2-3 days\n• Economy: 5-7 days\n\nTo get an exact estimate, use our quote calculator with your pickup and delivery addresses.");
        FAQ_RESPONSES.put("package status", "You can check your package status using your tracking number. Common statuses include:\n• Pending - Awaiting pickup\n• Confirmed - Booking confirmed\n• In Transit - On the way\n• Out for Delivery - Driver is delivering\n• Delivered - Successfully delivered\n\nCheck your status at /tracking");
        FAQ_RESPONSES.put("status", "Your package status shows the current stage of delivery. To check your status, provide your tracking number or visit our tracking page. You'll see updates like: Pending → Confirmed → In Transit → Out for Delivery → Delivered.");
        FAQ_RESPONSES.put("pricing", "Our pricing depends on:\n• Package weight and dimensions\n• Distance between pickup and delivery\n• Service type (Express/Standard/Economy)\n• Additional services (insurance, special handling)\n\nUse our online quote calculator at /quote for an instant, accurate estimate. It's free and takes less than a minute!");
        FAQ_RESPONSES.put("price", "To get an accurate price, use our online quote calculator. Prices start from R50 for small packages and vary based on weight, size, distance, and service type. Visit /quote to get your instant quote now.");
        FAQ_RESPONSES.put("cost", "Shipping costs depend on several factors. Get an instant quote by:\n1. Visiting /quote\n2. Entering pickup and delivery addresses\n3. Adding package details\n4. Selecting service type\n\nYou'll see the exact cost before booking. No hidden fees!");
        FAQ_RESPONSES.put("how much", "I can help you get a quote! Please tell me:\n• Pickup location\n• Delivery location\n• Package weight and size\n\nOr visit /quote for our instant quote calculator - it's free and gives you the exact price!");
        FAQ_RESPONSES.put("payment", "We accept multiple payment methods:\n• Credit/Debit Cards (Visa, Mastercard)\n• EFT/Bank Transfer\n• Paystack payment gateway\n• Cash on delivery (limited areas)\n\nPayment is required before dispatch. You'll receive a payment link via email after booking. All transactions are secure and encrypted.");
        FAQ_RESPONSES.put("pay", "Payment can be made securely online via:\n• Credit or debit card\n• EFT/bank transfer\n• Paystack gateway\n\nAfter creating a booking, you'll receive a secure payment link. Payment must be completed before we dispatch your package. All payment information is encrypted and secure.");
        FAQ_RESPONSES.put("cancel", "Cancellation Policy:\n• Free cancellation within 24 hours of booking\n• Full refund for cancellations before dispatch\n• Partial refund (less handling fee) after dispatch\n• No refund once package is in transit\n\nTo cancel, contact our support team or log into your account. We process refunds within 3-5 business days.");
        FAQ_RESPONSES.put("refund", "Refund Policy:\n• Full refund for cancellations within 24 hours\n• Refund minus handling fee after dispatch\n• No refund once package is delivered\n\nRefunds are processed within 3-5 business days to the original payment method. Contact support for assistance with refunds.");
        FAQ_RESPONSES.put("insurance", "Package Insurance:\n• Available for valuable items\n• Cost: 2-3% of declared value\n• Covers loss, damage, or theft\n• Maximum coverage varies by service type\n\nWhen booking, declare the value of your items to add insurance. We recommend insurance for packages worth more than R500.");
        FAQ_RESPONSES.put("damaged", "If your package arrives damaged:\n1. Take photos immediately\n2. Don't throw away the packaging\n3. Contact us within 48 hours\n4. Provide tracking number and photos\n\nWe'll investigate and resolve the issue promptly. If insured, you may be eligible for compensation. Our support team will guide you through the claims process.");
        FAQ_RESPONSES.put("lost", "If your package is lost:\n• Contact us immediately\n• Provide your tracking number\n• We'll start an investigation\n• If insured, you may be eligible for compensation\n\nWe track every package, so lost packages are very rare. Our team will work quickly to locate your shipment.");
        FAQ_RESPONSES.put("hours", "Customer Service Hours:\n• Monday-Friday: 8:00 AM - 6:00 PM\n• Saturday: 9:00 AM - 2:00 PM\n• Sunday: Closed\n• Public Holidays: Closed\n\nOur online chat is available 24/7, and you can track packages anytime on our website. For urgent matters outside business hours, leave a message and we'll respond first thing the next business day.");
        FAQ_RESPONSES.put("contact", "Contact Us:\n• Phone: 0800-RELIABLE (during business hours)\n• Email: support@reliablecarriers.co.za\n• Live Chat: Available 24/7 on our website\n• Office Hours: Mon-Fri 8 AM - 6 PM, Sat 9 AM - 2 PM\n\nYou can also visit us at our offices in Johannesburg, Cape Town, or Durban. Use this chat for immediate assistance!");
        FAQ_RESPONSES.put("location", "We operate throughout South Africa:\n• Main Offices: Johannesburg, Cape Town, Durban\n• Service Areas: Nationwide coverage\n• Pickup/Delivery: Available in all major cities and towns\n\nWe provide pickup and delivery services across South Africa. Use our quote calculator to see if we service your area.");
        FAQ_RESPONSES.put("book", "To book a shipment:\n1. Visit /quote or /customer/quote-logged-in\n2. Enter pickup and delivery addresses\n3. Add package details (weight, dimensions)\n4. Select service type\n5. Complete booking and payment\n\nAfter booking, you'll receive a tracking number via email and SMS. The whole process takes just a few minutes!");
        FAQ_RESPONSES.put("booking", "Creating a booking is easy:\n• Use our online booking system at /quote\n• Enter your addresses and package details\n• Get an instant quote\n• Complete payment securely\n• Receive tracking number immediately\n\nRegistered users can save addresses and track all shipments in their dashboard. Sign up at /register to get started!");
        FAQ_RESPONSES.put("quote", "Get a free instant quote:\n1. Go to /quote\n2. Enter pickup and delivery addresses\n3. Add package weight and dimensions\n4. Select service type\n5. See your price instantly\n\nQuotes are valid for 24 hours. No obligation to book. Try it now!");
        FAQ_RESPONSES.put("business", "Business Services:\n• Bulk shipping discounts\n• API integration for tracking\n• Business accounts with credit terms\n• Dedicated account manager\n• Custom shipping solutions\n\nVisit /register/business to set up a business account or contact our business team at business@reliablecarriers.co.za");
        FAQ_RESPONSES.put("driver", "Become a Driver:\n• Earn competitive rates\n• Flexible working hours\n• Support and training provided\n• Regular routes available\n\nApply at /register/driver to join our team. We're always looking for reliable drivers across South Africa!");
    }

    // Intent patterns
    private static final Map<String, Pattern> INTENT_PATTERNS = new HashMap<>();
    static {
        INTENT_PATTERNS.put("TRACKING", Pattern.compile("(?i)(track|tracking|where is|status|location).*"));
        INTENT_PATTERNS.put("DELIVERY_TIME", Pattern.compile("(?i)(delivery|deliver|when|time|arrive|arrival).*"));
        INTENT_PATTERNS.put("PRICING", Pattern.compile("(?i)(price|cost|fee|how much|quote|charge).*"));
        INTENT_PATTERNS.put("CANCEL", Pattern.compile("(?i)(cancel|cancellation|refund|return).*"));
        INTENT_PATTERNS.put("PAYMENT", Pattern.compile("(?i)(pay|payment|paystack|card|eft).*"));
        INTENT_PATTERNS.put("COMPLAINT", Pattern.compile("(?i)(problem|issue|wrong|error|broken|damaged|complaint).*"));
        INTENT_PATTERNS.put("GREETING", Pattern.compile("(?i)^(hi|hello|hey|good morning|good afternoon|good evening).*"));
        INTENT_PATTERNS.put("THANKS", Pattern.compile("(?i)(thank|thanks|appreciate|grateful).*"));
    }

    @Override
    public ChatbotResponse getResponse(String message, String chatId, String userEmail, List<Map<String, Object>> conversationHistory) {
        if (message == null || message.trim().isEmpty()) {
            return new ChatbotResponse("I'm here to help! How can I assist you today?", "GREETING", 1.0);
        }

        String intent = analyzeIntent(message);

        // Check for tracking number in message
        String trackingNumber = extractTrackingNumber(message);

        // Try FAQ first (rule-based)
        String faqResponse = getFAQResponse(message.toLowerCase());
        if (faqResponse != null) {
            ChatbotResponse response = new ChatbotResponse(faqResponse, intent, 0.9);
            response.setQuickResponses(getQuickResponses(message));
            
            // Add tracking info if tracking number found
            if (trackingNumber != null) {
                response.setMetadata(getTrackingInfo(trackingNumber, userEmail));
            }
            
            return response;
        }

        // Try AI if enabled
        if (aiEnabled && !openaiApiKey.isEmpty()) {
            try {
                String aiResponse = getAIResponse(message, conversationHistory, userEmail, trackingNumber);
                if (aiResponse != null && !aiResponse.isEmpty()) {
                    ChatbotResponse response = new ChatbotResponse(aiResponse, intent, 0.85);
                    response.setQuickResponses(getQuickResponses(message));
                    
                    // Check if human agent needed
                    double sentiment = analyzeSentiment(message);
                    response.setRequiresHuman(requiresHumanAgent(message, sentiment));
                    
                    return response;
                }
            } catch (Exception e) {
                logger.warn("AI service unavailable, using fallback: " + e.getMessage());
            }
        }

        // Fallback to rule-based response
        return getFallbackResponse(message, intent, trackingNumber, userEmail);
    }

    @Override
    public String analyzeIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        for (Map.Entry<String, Pattern> entry : INTENT_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(message).matches()) {
                return entry.getKey();
            }
        }

        return "GENERAL";
    }

    @Override
    public List<String> getQuickResponses(String message) {
        String intent = analyzeIntent(message);
        List<String> suggestions = new ArrayList<>();

        switch (intent) {
            case "TRACKING":
                suggestions.add("How do I track my package?");
                suggestions.add("My tracking number is not working");
                suggestions.add("Package is delayed");
                break;
            case "DELIVERY_TIME":
                suggestions.add("What are your delivery times?");
                suggestions.add("My package is late");
                suggestions.add("Can I get same-day delivery?");
                break;
            case "PRICING":
                suggestions.add("Get a quote");
                suggestions.add("How are prices calculated?");
                suggestions.add("Do you offer discounts?");
                break;
            default:
                suggestions.add("Track my package");
                suggestions.add("Get a quote");
                suggestions.add("Contact support");
                suggestions.add("View FAQ");
        }

        return suggestions;
    }

    @Override
    public boolean requiresHumanAgent(String message, double sentiment) {
        // Require human if negative sentiment or specific keywords
        if (sentiment < -0.3) return true;
        
        String lowerMessage = message.toLowerCase();
        String[] urgentKeywords = {"urgent", "emergency", "asap", "immediately", "critical", "complaint", "sue", "lawyer"};
        for (String keyword : urgentKeywords) {
            if (lowerMessage.contains(keyword)) return true;
        }
        
        return false;
    }

    private String getFAQResponse(String message) {
        // Try exact matches first (higher priority)
        for (Map.Entry<String, String> entry : FAQ_RESPONSES.entrySet()) {
            if (message.equals(entry.getKey()) || message.contains(" " + entry.getKey() + " ") || 
                message.startsWith(entry.getKey() + " ") || message.endsWith(" " + entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Try partial matches
        for (Map.Entry<String, String> entry : FAQ_RESPONSES.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private ChatbotResponse getFallbackResponse(String message, String intent, String trackingNumber, String userEmail) {
        String response;

        switch (intent) {
            case "GREETING":
                response = "Hello! 👋 Welcome to Reliable Carriers. I'm here to help you with tracking, quotes, and any questions you might have. How can I assist you today?";
                break;
            case "THANKS":
                response = "You're welcome! 😊 Is there anything else I can help you with?";
                break;
            case "TRACKING":
                if (trackingNumber != null) {
                    Map<String, Object> trackingInfo = getTrackingInfo(trackingNumber, userEmail);
                    if (trackingInfo != null && trackingInfo.containsKey("status")) {
                        response = String.format("Your package with tracking number %s is currently: %s. %s",
                            trackingNumber, trackingInfo.get("status"), trackingInfo.get("message"));
                    } else {
                        response = "I couldn't find that tracking number. Please verify it starts with 'RC' followed by letters and numbers, or contact our support team for assistance.";
                    }
                } else {
                    response = "To track your package, please provide your tracking number (starts with 'RC'). You can also visit our tracking page and enter it there.";
                }
                break;
            case "DELIVERY_TIME":
                response = FAQ_RESPONSES.get("delivery time");
                break;
            case "PRICING":
                response = "Our pricing is calculated based on:\n• Package weight and dimensions\n• Distance between locations\n• Service type selected\n\nFor an accurate, instant quote, visit our quote calculator at /quote. You can also tell me your pickup and delivery locations, and I'll guide you through getting a quote!";
                break;
            case "CANCEL":
                response = FAQ_RESPONSES.get("cancel");
                break;
            case "COMPLAINT":
                response = "I'm sorry to hear you're experiencing an issue. Let me connect you with a human agent who can better assist you. Your concern is important to us.";
                return new ChatbotResponse(response, intent, 1.0) {{
                    setRequiresHuman(true);
                }};
            default:
                // More helpful default response
                String defaultResponse = "I'd be happy to help! ";
                
                // Check for common keywords and provide relevant guidance
                if (message.toLowerCase().contains("track") || message.toLowerCase().contains("where")) {
                    defaultResponse += "To track your package, please provide your tracking number (starts with 'RC'), or visit /tracking to enter it there.";
                } else if (message.toLowerCase().contains("quote") || message.toLowerCase().contains("price") || message.toLowerCase().contains("cost")) {
                    defaultResponse += "For pricing, visit our quote calculator at /quote to get an instant estimate, or tell me your pickup and delivery locations.";
                } else if (message.toLowerCase().contains("book") || message.toLowerCase().contains("ship")) {
                    defaultResponse += "To book a shipment, visit /quote to get started. The process takes just a few minutes!";
                } else {
                    defaultResponse += "I can help you with:\n" +
                        "• 📦 Tracking packages - Just provide your tracking number\n" +
                        "• 💰 Getting quotes - Visit /quote or tell me your addresses\n" +
                        "• 📋 Booking shipments - I'll guide you through the process\n" +
                        "• ❓ General questions - Ask me anything!\n" +
                        "• 👤 Human support - Say 'speak to agent' if you need human assistance\n\n" +
                        "What would you like help with?";
                }
                
                response = defaultResponse;
        }

        ChatbotResponse chatbotResponse = new ChatbotResponse(response, intent, 0.7);
        chatbotResponse.setQuickResponses(getQuickResponses(message));
        return chatbotResponse;
    }

    private String extractTrackingNumber(String message) {
        // Pattern: RC followed by alphanumeric (8-12 chars)
        Pattern pattern = Pattern.compile("\\bRC[A-Z0-9]{8,12}\\b", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group().toUpperCase();
        }
        return null;
    }

    private Map<String, Object> getTrackingInfo(String trackingNumber, String userEmail) {
        try {
            // Try booking first
            Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                Map<String, Object> info = new HashMap<>();
                info.put("status", booking.getStatus().toString());
                info.put("trackingNumber", trackingNumber);
                info.put("pickupAddress", booking.getPickupAddress());
                info.put("deliveryAddress", booking.getDeliveryAddress());
                info.put("message", String.format("Your package is %s. Estimated delivery: %s", 
                    booking.getStatus(), booking.getEstimatedDeliveryDate()));
                return info;
            }

            // Try shipment
            Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                Map<String, Object> info = new HashMap<>();
                info.put("status", shipment.getStatus() != null ? shipment.getStatus().toString() : "PENDING");
                info.put("trackingNumber", trackingNumber);
                info.put("pickupAddress", shipment.getPickupAddress());
                info.put("deliveryAddress", shipment.getDeliveryAddress());
                info.put("message", String.format("Your shipment is %s", 
                    shipment.getStatus() != null ? shipment.getStatus() : "being processed"));
                return info;
            }
        } catch (Exception e) {
            logger.error("Error fetching tracking info: " + e.getMessage());
        }
        return null;
    }

    private String getAIResponse(String message, List<Map<String, Object>> history, String userEmail, String trackingNumber) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + openaiApiKey);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Build context for AI
            StringBuilder systemPrompt = new StringBuilder("You are a helpful customer support assistant for Reliable Carriers, a courier and logistics company in South Africa. ");
            systemPrompt.append("You help customers with:\n");
            systemPrompt.append("- Tracking packages (tracking numbers start with 'RC')\n");
            systemPrompt.append("- Getting shipping quotes\n");
            systemPrompt.append("- Delivery times and status\n");
            systemPrompt.append("- Payment and billing questions\n");
            systemPrompt.append("- Service information\n\n");
            systemPrompt.append("Be friendly, professional, and concise. If you need a tracking number, ask for it politely. ");

            if (trackingNumber != null) {
                systemPrompt.append("The user has mentioned tracking number: ").append(trackingNumber).append(". ");
            }

            // Build conversation history
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt.toString()));

            // Add conversation history (last 10 messages)
            if (history != null && !history.isEmpty()) {
                int start = Math.max(0, history.size() - 10);
                for (int i = start; i < history.size(); i++) {
                    Map<String, Object> msg = history.get(i);
                    String role = userEmail.equals(msg.get("senderEmail")) ? "user" : "assistant";
                    messages.add(Map.of("role", role, "content", msg.get("text").toString()));
                }
            }

            messages.add(Map.of("role", "user", "content", message));

            Map<String, Object> requestBody = Map.of(
                "model", aiModel,
                "messages", messages,
                "max_tokens", 200,
                "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String apiUrl = "https://api.openai.com/v1/chat/completions";

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, request, 
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body == null) return null;
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    Object content = messageObj != null ? messageObj.get("content") : null;
                    return content != null ? content.toString() : null;
                }
            }
        } catch (Exception e) {
            logger.error("Error calling OpenAI API: " + e.getMessage());
        }
        return null;
    }

    private double analyzeSentiment(String message) {
        // Simple sentiment analysis based on keywords
        String lowerMessage = message.toLowerCase();
        
        int positive = 0;
        int negative = 0;
        
        String[] positiveWords = {"good", "great", "excellent", "thank", "thanks", "appreciate", "happy", "satisfied", "love", "perfect"};
        String[] negativeWords = {"bad", "terrible", "horrible", "angry", "frustrated", "disappointed", "hate", "worst", "awful", "problem"};
        
        for (String word : positiveWords) {
            if (lowerMessage.contains(word)) positive++;
        }
        for (String word : negativeWords) {
            if (lowerMessage.contains(word)) negative++;
        }
        
        if (positive == 0 && negative == 0) return 0.0;
        return (positive - negative) / (double) (positive + negative);
    }
}
