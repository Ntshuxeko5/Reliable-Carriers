package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.ContactMessage;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.ContactMessageRepository;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {
    
    @Autowired
    private ContactMessageRepository contactMessageRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AuthService authService;
    
    @Value("${app.admin.email:admin@reliablecarriers.co.za}")
    private String adminEmail;
    
    @Value("${app.name:Reliable Carriers}")
    private String appName;
    
    /**
     * Submit a contact form message
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitContactMessage(@Valid @RequestBody ContactRequest request) {
        try {
            // Create and save contact message
            ContactMessage message = new ContactMessage(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getSubject(),
                request.getMessage()
            );
            
            ContactMessage savedMessage = contactMessageRepository.save(message);
            
            // Send email notification to admin
            try {
                String emailSubject = "New Contact Message: " + request.getSubject();
                String emailBody = buildAdminNotificationEmail(savedMessage);
                emailService.sendSimpleEmail(adminEmail, emailSubject, emailBody);
            } catch (Exception e) {
                // Log error but don't fail the request
                System.err.println("Failed to send admin notification email: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thank you for your message! We will get back to you soon.",
                "id", savedMessage.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to submit message: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get all contact messages (Admin only)
     */
    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllMessages(
            @RequestParam(required = false) String status) {
        try {
            List<ContactMessage> messages;
            
            if (status != null && !status.isEmpty()) {
                try {
                    ContactMessage.MessageStatus messageStatus = ContactMessage.MessageStatus.valueOf(status.toUpperCase());
                    messages = contactMessageRepository.findByStatusOrderByCreatedAtDesc(messageStatus);
                } catch (IllegalArgumentException e) {
                    messages = contactMessageRepository.findAllByOrderByCreatedAtDesc();
                }
            } else {
                messages = contactMessageRepository.findAllByOrderByCreatedAtDesc();
            }
            
            List<Map<String, Object>> messageList = messages.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
            
            long unreadCount = contactMessageRepository.countUnreadMessages();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "messages", messageList,
                "unreadCount", unreadCount,
                "totalCount", messages.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve messages: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get a single contact message (Admin only)
     */
    @GetMapping("/messages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMessage(@PathVariable Long id) {
        try {
            ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
            
            // Mark as read if it's new
            if (message.getStatus() == ContactMessage.MessageStatus.NEW) {
                message.setStatus(ContactMessage.MessageStatus.READ);
                message.setReadAt(new java.util.Date());
                contactMessageRepository.save(message);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", convertToMap(message)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve message: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update message status (Admin only)
     */
    @PutMapping("/messages/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateMessageStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
            
            String status = request.get("status");
            if (status != null) {
                try {
                    ContactMessage.MessageStatus messageStatus = ContactMessage.MessageStatus.valueOf(status.toUpperCase());
                    message.setStatus(messageStatus);
                    
                    if (messageStatus == ContactMessage.MessageStatus.READ && message.getReadAt() == null) {
                        message.setReadAt(new java.util.Date());
                    }
                    
                    if (messageStatus == ContactMessage.MessageStatus.REPLIED && message.getRepliedAt() == null) {
                        message.setRepliedAt(new java.util.Date());
                        User currentUser = authService.getCurrentUser();
                        if (currentUser != null) {
                            message.setRepliedBy(currentUser);
                        }
                    }
                    
                    contactMessageRepository.save(message);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Invalid status: " + status
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Status updated successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to update status: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get unread message count (Admin only)
     */
    @GetMapping("/messages/unread/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            long count = contactMessageRepository.countUnreadMessages();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to get unread count: " + e.getMessage()
            ));
        }
    }
    
    private String buildAdminNotificationEmail(ContactMessage message) {
        return String.format("""
            New Contact Message Received
            
            From: %s %s
            Email: %s
            Phone: %s
            Subject: %s
            
            Message:
            %s
            
            ---
            Received: %s
            Message ID: %d
            
            Please log in to the admin panel to view and respond to this message.
            """,
            message.getFirstName(),
            message.getLastName(),
            message.getEmail(),
            message.getPhone() != null ? message.getPhone() : "Not provided",
            message.getSubject(),
            message.getMessage(),
            message.getCreatedAt(),
            message.getId()
        );
    }
    
    private Map<String, Object> convertToMap(ContactMessage message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("firstName", message.getFirstName());
        map.put("lastName", message.getLastName());
        map.put("fullName", message.getFullName());
        map.put("email", message.getEmail());
        map.put("phone", message.getPhone());
        map.put("subject", message.getSubject());
        map.put("message", message.getMessage());
        map.put("status", message.getStatus().toString());
        map.put("createdAt", message.getCreatedAt());
        map.put("readAt", message.getReadAt());
        map.put("repliedAt", message.getRepliedAt());
        map.put("adminNotes", message.getAdminNotes());
        if (message.getRepliedBy() != null) {
            map.put("repliedBy", message.getRepliedBy().getEmail());
        }
        return map;
    }
    
    // DTO for contact form submission
    public static class ContactRequest {
        @NotBlank(message = "First name is required")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        private String lastName;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        
        private String phone;
        
        @NotBlank(message = "Subject is required")
        private String subject;
        
        @NotBlank(message = "Message is required")
        private String message;
        
        // Getters and Setters
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public void setSubject(String subject) {
            this.subject = subject;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}

