package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.SupportTicket;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
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
@RequestMapping("/api/admin/support-tickets")
@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
@CrossOrigin(origins = "*")
public class AdminSupportTicketController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSupportTicketController.class);

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all support tickets
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category) {
        try {
            List<SupportTicket> tickets = supportTicketService.getAllTickets();
            
            // Filter by status
            if (status != null && !status.isEmpty()) {
                try {
                    SupportTicket.TicketStatus ticketStatus = SupportTicket.TicketStatus.valueOf(status.toUpperCase().replace("-", "_"));
                    tickets = tickets.stream()
                        .filter(t -> t.getStatus() == ticketStatus)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore filter
                }
            }
            
            // Filter by priority
            if (priority != null && !priority.isEmpty()) {
                try {
                    SupportTicket.TicketPriority ticketPriority = SupportTicket.TicketPriority.valueOf(priority.toUpperCase());
                    tickets = tickets.stream()
                        .filter(t -> t.getPriority() == ticketPriority)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid priority, ignore filter
                }
            }
            
            // Filter by category
            if (category != null && !category.isEmpty()) {
                try {
                    SupportTicket.TicketCategory ticketCategory = SupportTicket.TicketCategory.valueOf(category.toUpperCase().replace("-", "_"));
                    tickets = tickets.stream()
                        .filter(t -> t.getCategory() == ticketCategory)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid category, ignore filter
                }
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
    @GetMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> getTicket(@PathVariable Long ticketId) {
        try {
            SupportTicket ticket = supportTicketService.getTicketById(ticketId);
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
     * Update ticket status
     */
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            String statusStr = request.get("status");
            String resolutionNotes = request.get("resolutionNotes");

            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Status is required"));
            }

            SupportTicket.TicketStatus status;
            try {
                status = SupportTicket.TicketStatus.valueOf(statusStr.toUpperCase().replace("-", "_"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid status"));
            }

            SupportTicket updatedTicket = supportTicketService.updateTicketStatus(ticketId, status, resolutionNotes);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket status updated successfully",
                "ticket", convertToDTO(updatedTicket)
            ));
        } catch (Exception e) {
            logger.error("Error updating ticket status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to update ticket: " + e.getMessage()));
        }
    }

    /**
     * Assign ticket to staff member
     */
    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Map<String, Object>> assignTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, Long> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            Long assignedToId = request.get("assignedToId");
            if (assignedToId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Assigned user ID is required"));
            }

            User assignedTo = userRepository.findById(assignedToId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + assignedToId));

            SupportTicket updatedTicket = supportTicketService.assignTicket(ticketId, assignedTo);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket assigned successfully",
                "ticket", convertToDTO(updatedTicket)
            ));
        } catch (Exception e) {
            logger.error("Error assigning ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to assign ticket: " + e.getMessage()));
        }
    }

    /**
     * Add comment to ticket
     */
    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
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
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            List<SupportTicket> allTickets = supportTicketService.getAllTickets();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTickets", allTickets.size());
            stats.put("openTickets", allTickets.stream().filter(t -> t.getStatus() == SupportTicket.TicketStatus.OPEN).count());
            stats.put("inProgressTickets", allTickets.stream().filter(t -> t.getStatus() == SupportTicket.TicketStatus.IN_PROGRESS).count());
            stats.put("resolvedTickets", allTickets.stream().filter(t -> t.getStatus() == SupportTicket.TicketStatus.RESOLVED).count());
            stats.put("closedTickets", allTickets.stream().filter(t -> t.getStatus() == SupportTicket.TicketStatus.CLOSED).count());
            stats.put("urgentTickets", allTickets.stream().filter(t -> t.getPriority() == SupportTicket.TicketPriority.URGENT).count());
            stats.put("highPriorityTickets", allTickets.stream().filter(t -> t.getPriority() == SupportTicket.TicketPriority.HIGH).count());

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
        
        if (ticket.getCustomer() != null) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("id", ticket.getCustomer().getId());
            customer.put("name", ticket.getCustomer().getFirstName() + " " + ticket.getCustomer().getLastName());
            customer.put("email", ticket.getCustomer().getEmail());
            customer.put("phone", ticket.getCustomer().getPhone());
            dto.put("customer", customer);
        }
        
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

