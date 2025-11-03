package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Controller for in-app messaging/chat functionality
 */
@Controller
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class CustomerChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private com.reliablecarriers.Reliable.Carriers.service.ChatbotService chatbotService;

    private static final Logger logger = LoggerFactory.getLogger(CustomerChatController.class);

    // In-memory storage for messages (in production, use database)
    private final Map<String, List<Map<String, Object>>> chatHistory = new HashMap<>();

    /**
     * WebSocket endpoint for sending messages
     */
    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public Map<String, Object> sendMessage(@Payload Map<String, Object> message) {
        String chatId = (String) message.get("chatId");
        String senderEmail = (String) message.get("senderEmail");
        String text = (String) message.get("text");
        String timestamp = new Date().toString();

        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("chatId", chatId);
        chatMessage.put("senderEmail", senderEmail);
        chatMessage.put("text", text);
        chatMessage.put("timestamp", timestamp);
        chatMessage.put("type", "MESSAGE");

        // Store message in history
        List<Map<String, Object>> history = chatHistory.computeIfAbsent(chatId, k -> new ArrayList<>());
        history.add(chatMessage);

        // Send user message to chat room
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, chatMessage);

        // Get AI bot response
        try {
            com.reliablecarriers.Reliable.Carriers.service.ChatbotService.ChatbotResponse botResponse = 
                chatbotService.getResponse(text, chatId, senderEmail, history);

            Map<String, Object> botMessage = new HashMap<>();
            botMessage.put("chatId", chatId);
            botMessage.put("senderEmail", "support@reliablecarriers.co.za");
            botMessage.put("senderName", "Support Assistant");
            botMessage.put("text", botResponse.getMessage());
            botMessage.put("timestamp", new Date().toString());
            botMessage.put("type", "BOT_RESPONSE");
            botMessage.put("intent", botResponse.getIntent());
            botMessage.put("confidence", botResponse.getConfidence());
            botMessage.put("requiresHuman", botResponse.isRequiresHuman());
            botMessage.put("quickResponses", botResponse.getQuickResponses());
            if (botResponse.getMetadata() != null) {
                botMessage.put("metadata", botResponse.getMetadata());
            }

            // Store bot response
            history.add(botMessage);

            // Send bot response with small delay for typing indicator
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    messagingTemplate.convertAndSend("/topic/chat/" + chatId, botMessage);
                }
            }, 1000); // 1 second delay for realistic typing

        } catch (Exception e) {
            // Error getting bot response - send generic message
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("chatId", chatId);
            errorMessage.put("senderEmail", "support@reliablecarriers.co.za");
            errorMessage.put("senderName", "Support Assistant");
            errorMessage.put("text", "I'm having trouble understanding. Would you like to speak with a human agent?");
            errorMessage.put("timestamp", new Date().toString());
            errorMessage.put("type", "BOT_RESPONSE");
            errorMessage.put("requiresHuman", true);
            
            history.add(errorMessage);
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, errorMessage);
        }

        return chatMessage;
    }

    /**
     * Get chat history
     */
    @GetMapping("/history/{chatId}")
    @ResponseBody
    public ResponseEntity<?> getChatHistory(@PathVariable String chatId) {
        List<Map<String, Object>> history = chatHistory.getOrDefault(chatId, new ArrayList<>());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "messages", history
        ));
    }

    /**
     * Initialize chat session
     */
    @PostMapping("/initiate")
    @ResponseBody
    public ResponseEntity<?> initiateChat(@RequestBody Map<String, Object> request) {
        try {
            String customerEmail = (String) request.get("customerEmail");
            String trackingNumber = (String) request.get("trackingNumber");
            String subject = (String) request.get("subject");

            // Generate chat ID
            String chatId = "chat_" + UUID.randomUUID().toString().substring(0, 8);

            // Create initial message
            Map<String, Object> initialMessage = new HashMap<>();
            initialMessage.put("chatId", chatId);
            initialMessage.put("senderEmail", customerEmail);
            initialMessage.put("text", subject != null ? subject : "Chat initiated for tracking: " + trackingNumber);
            initialMessage.put("timestamp", new Date().toString());
            initialMessage.put("type", "SYSTEM");

            // Store initial message
            chatHistory.put(chatId, new ArrayList<>(List.of(initialMessage)));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "chatId", chatId,
                "message", "Chat session initiated"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error initiating chat: " + e.getMessage()));
        }
    }

    /**
     * Send message via REST API (for fallback when WebSocket unavailable)
     */
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<?> sendMessageRest(@RequestBody Map<String, Object> messageData) {
        try {
            String chatId = (String) messageData.get("chatId");
            String senderEmail = (String) messageData.get("senderEmail");
            String text = (String) messageData.get("text");

            Map<String, Object> message = new HashMap<>();
            message.put("chatId", chatId);
            message.put("senderEmail", senderEmail);
            message.put("text", text);
            message.put("timestamp", new Date().toString());
            message.put("type", "MESSAGE");

            // Store message
            List<Map<String, Object>> history = chatHistory.computeIfAbsent(chatId, k -> new ArrayList<>());
            history.add(message);

            // Send user message via WebSocket if available
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, message);

            // Get AI bot response
            try {
                com.reliablecarriers.Reliable.Carriers.service.ChatbotService.ChatbotResponse botResponse = 
                    chatbotService.getResponse(text, chatId, senderEmail, history);

                Map<String, Object> botMessage = new HashMap<>();
                botMessage.put("chatId", chatId);
                botMessage.put("senderEmail", "support@reliablecarriers.co.za");
                botMessage.put("senderName", "Support Assistant");
                botMessage.put("text", botResponse.getMessage());
                botMessage.put("timestamp", new Date().toString());
                botMessage.put("type", "BOT_RESPONSE");
                botMessage.put("intent", botResponse.getIntent());
                botMessage.put("confidence", botResponse.getConfidence());
                botMessage.put("requiresHuman", botResponse.isRequiresHuman());
                botMessage.put("quickResponses", botResponse.getQuickResponses());
                if (botResponse.getMetadata() != null) {
                    botMessage.put("metadata", botResponse.getMetadata());
                }

                history.add(botMessage);
                messagingTemplate.convertAndSend("/topic/chat/" + chatId, botMessage);

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message sent successfully",
                    "botResponse", botMessage
                ));
            } catch (Exception e) {
                logger.error("Error getting bot response: " + e.getMessage());
                // Return success even if bot fails
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message sent successfully"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error sending message: " + e.getMessage()));
        }
    }

    /**
     * Get active chat sessions for a user
     */
    @GetMapping("/sessions/{email}")
    @ResponseBody
    public ResponseEntity<?> getActiveSessions(@PathVariable String email) {
        try {
            List<Map<String, Object>> sessions = new ArrayList<>();
            
            // Find all chats where user is a participant
            for (Map.Entry<String, List<Map<String, Object>>> entry : chatHistory.entrySet()) {
                List<Map<String, Object>> messages = entry.getValue();
                if (!messages.isEmpty()) {
                    Map<String, Object> firstMessage = messages.get(0);
                    String senderEmail = (String) firstMessage.get("senderEmail");
                    
                    if (email.equalsIgnoreCase(senderEmail)) {
                        Map<String, Object> session = new HashMap<>();
                        session.put("chatId", entry.getKey());
                        session.put("lastMessage", messages.get(messages.size() - 1));
                        session.put("messageCount", messages.size());
                        sessions.add(session);
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "sessions", sessions
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error fetching sessions: " + e.getMessage()));
        }
    }
}
