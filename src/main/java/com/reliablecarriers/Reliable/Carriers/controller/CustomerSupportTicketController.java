package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.SupportTicket;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.SupportTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/support")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "*")
public class CustomerSupportTicketController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerSupportTicketController.class);

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private AuthService authService;

    /**
     * Create a new support ticket
     */
    @PostMapping("/tickets")
    public ResponseEntity<Map<String, Object>> createTicket(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            String subject = (String) request.get("subject");
            String description = (String) request.get("description");
            String categoryStr = (String) request.get("category");
            String priorityStr = (String) request.get("priority");
            Long relatedBookingId = request.get("relatedBookingId") != null ? 
                Long.parseLong(request.get("relatedBookingId").toString()) : null;
            Long relatedShipmentId = request.get("relatedShipmentId") != null ? 
                Long.parseLong(request.get("relatedShipmentId").toString()) : null;

            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Subject is required"));
            }

            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Description is required"));
            }

            SupportTicket.TicketCategory category = SupportTicket.TicketCategory.GENERAL;
            if (categoryStr != null) {
                try {
                    category = SupportTicket.TicketCategory.valueOf(categoryStr.toUpperCase().replace("-", "_"));
                } catch (IllegalArgumentException e) {
                    // Use default
                }
            }

            SupportTicket.TicketPriority priority = SupportTicket.TicketPriority.MEDIUM;
            if (priorityStr != null) {
                try {
                    priority = SupportTicket.TicketPriority.valueOf(priorityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Use default
                }
            }

            SupportTicket ticket = supportTicketService.createTicket(
                currentUser, subject, description, category, priority,
                relatedBookingId, relatedShipmentId
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket created successfully",
                "ticket", convertToDTO(ticket)
            ));
        } catch (Exception e) {
            logger.error("Error creating support ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to create ticket: " + e.getMessage()));
        }
    }

    /**
     * Get all tickets for current customer
     */
    @GetMapping("/tickets")
    public ResponseEntity<Map<String, Object>> getTickets(
            @RequestParam(required = false) String status) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            List<SupportTicket> tickets;
            if (status != null) {
                try {
                    SupportTicket.TicketStatus ticketStatus = SupportTicket.TicketStatus.valueOf(status.toUpperCase().replace("-", "_"));
                    tickets = supportTicketService.getTicketsByCustomer(currentUser).stream()
                        .filter(t -> t.getStatus() == ticketStatus)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    tickets = supportTicketService.getTicketsByCustomer(currentUser);
                }
            } else {
                tickets = supportTicketService.getTicketsByCustomer(currentUser);
            }

            List<Map<String, Object>> ticketDTOs = tickets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", ticketDTOs,
                "total", ticketDTOs.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch tickets: " + e.getMessage()));
        }
    }

    /**
     * Get ticket by ID
     */
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<Map<String, Object>> getTicket(@PathVariable Long ticketId) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            SupportTicket ticket = supportTicketService.getTicketById(ticketId);
            
            // Verify ticket belongs to current user
            if (!ticket.getCustomer().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied"));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "ticket", convertToDTO(ticket)
            ));
        } catch (Exception e) {
            logger.error("Error fetching ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch ticket: " + e.getMessage()));
        }
    }

    /**
     * Add comment to ticket
     */
    @PostMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            SupportTicket ticket = supportTicketService.getTicketById(ticketId);
            
            // Verify ticket belongs to current user
            if (!ticket.getCustomer().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Access denied"));
            }

            String comment = request.get("comment");
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Comment is required"));
            }

            SupportTicket updatedTicket = supportTicketService.addComment(ticketId, comment, currentUser);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Comment added successfully",
                "ticket", convertToDTO(updatedTicket)
            ));
        } catch (Exception e) {
            logger.error("Error adding comment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to add comment: " + e.getMessage()));
        }
    }

    /**
     * Get ticket statistics
     */
    @GetMapping("/tickets/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            Map<String, Object> stats = supportTicketService.getTicketStatistics(currentUser);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", stats
            ));
        } catch (Exception e) {
            logger.error("Error fetching ticket statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch statistics: " + e.getMessage()));
        }
    }

    /**
     * Convert SupportTicket to DTO
     */
    private Map<String, Object> convertToDTO(SupportTicket ticket) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", ticket.getId());
        dto.put("ticketNumber", ticket.getTicketNumber());
        dto.put("subject", ticket.getSubject());
        dto.put("description", ticket.getDescription());
        dto.put("category", ticket.getCategory() != null ? ticket.getCategory().name() : null);
        dto.put("priority", ticket.getPriority() != null ? ticket.getPriority().name() : null);
        dto.put("status", ticket.getStatus() != null ? ticket.getStatus().name() : null);
        dto.put("createdAt", ticket.getCreatedAt());
        dto.put("updatedAt", ticket.getUpdatedAt());
        dto.put("resolvedAt", ticket.getResolvedAt());
        dto.put("resolution", ticket.getResolution());
        
        if (ticket.getAssignedTo() != null) {
            Map<String, Object> assignedTo = new HashMap<>();
            assignedTo.put("id", ticket.getAssignedTo().getId());
            assignedTo.put("name", ticket.getAssignedTo().getFirstName() + " " + ticket.getAssignedTo().getLastName());
            assignedTo.put("email", ticket.getAssignedTo().getEmail());
            dto.put("assignedTo", assignedTo);
        }
        
        if (ticket.getRelatedBooking() != null) {
            Map<String, Object> booking = new HashMap<>();
            booking.put("id", ticket.getRelatedBooking().getId());
            booking.put("bookingNumber", ticket.getRelatedBooking().getBookingNumber());
            dto.put("relatedBooking", booking);
        }
        
        if (ticket.getRelatedShipment() != null) {
            Map<String, Object> shipment = new HashMap<>();
            shipment.put("id", ticket.getRelatedShipment().getId());
            shipment.put("trackingNumber", ticket.getRelatedShipment().getTrackingNumber());
            dto.put("relatedShipment", shipment);
        }
        
        return dto;
    }
}

