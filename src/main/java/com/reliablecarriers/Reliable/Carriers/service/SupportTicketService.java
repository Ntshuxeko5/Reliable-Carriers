package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.SupportTicket;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.util.List;
import java.util.Map;

public interface SupportTicketService {
    
    /**
     * Create a new support ticket
     */
    SupportTicket createTicket(User customer, String subject, String description, 
                               SupportTicket.TicketCategory category, 
                               SupportTicket.TicketPriority priority,
                               Long relatedBookingId, Long relatedShipmentId);
    
    /**
     * Get ticket by ID
     */
    SupportTicket getTicketById(Long ticketId);
    
    /**
     * Get ticket by ticket number
     */
    SupportTicket getTicketByNumber(String ticketNumber);
    
    /**
     * Get all tickets for a customer
     */
    List<SupportTicket> getTicketsByCustomer(User customer);
    
    /**
     * Get active tickets for a customer
     */
    List<SupportTicket> getActiveTicketsByCustomer(User customer);
    
    /**
     * Update ticket status
     */
    SupportTicket updateTicketStatus(Long ticketId, SupportTicket.TicketStatus status, String resolution);
    
    /**
     * Add comment/reply to ticket
     */
    SupportTicket addComment(Long ticketId, String comment, User commenter);
    
    /**
     * Assign ticket to support staff
     */
    SupportTicket assignTicket(Long ticketId, User assignedTo);
    
    /**
     * Get all tickets (for admin)
     */
    List<SupportTicket> getAllTickets();
    
    /**
     * Get ticket statistics for customer
     */
    Map<String, Object> getTicketStatistics(User customer);
}

