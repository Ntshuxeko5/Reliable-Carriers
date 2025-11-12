package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import com.reliablecarriers.Reliable.Carriers.service.SupportTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private static final Logger logger = LoggerFactory.getLogger(SupportTicketServiceImpl.class);

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Override
    public SupportTicket createTicket(User customer, String subject, String description,
                                     SupportTicket.TicketCategory category,
                                     SupportTicket.TicketPriority priority,
                                     Long relatedBookingId, Long relatedShipmentId) {
        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setCategory(category != null ? category : SupportTicket.TicketCategory.GENERAL);
        ticket.setPriority(priority != null ? priority : SupportTicket.TicketPriority.MEDIUM);
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);
        
        if (relatedBookingId != null) {
            bookingRepository.findById(relatedBookingId).ifPresent(ticket::setRelatedBooking);
        }
        
        if (relatedShipmentId != null) {
            shipmentRepository.findById(relatedShipmentId).ifPresent(ticket::setRelatedShipment);
        }
        
        SupportTicket savedTicket = ticketRepository.save(ticket);
        logger.info("Created support ticket {} for customer {}", savedTicket.getTicketNumber(), customer.getEmail());
        
        return savedTicket;
    }

    @Override
    public SupportTicket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
    }

    @Override
    public SupportTicket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
            .orElseThrow(() -> new RuntimeException("Ticket not found with number: " + ticketNumber));
    }

    @Override
    public List<SupportTicket> getTicketsByCustomer(User customer) {
        return ticketRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Override
    public List<SupportTicket> getActiveTicketsByCustomer(User customer) {
        return ticketRepository.findActiveTicketsByCustomer(customer);
    }

    @Override
    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public SupportTicket updateTicketStatus(Long ticketId, SupportTicket.TicketStatus status, String resolution) {
        SupportTicket ticket = getTicketById(ticketId);
        ticket.setStatus(status);
        if (resolution != null) {
            ticket.setResolution(resolution);
        }
        if (status == SupportTicket.TicketStatus.RESOLVED || status == SupportTicket.TicketStatus.CLOSED) {
            ticket.setResolvedAt(new java.util.Date());
        }
        return ticketRepository.save(ticket);
    }

    @Override
    public SupportTicket addComment(Long ticketId, String comment, User commenter) {
        SupportTicket ticket = getTicketById(ticketId);
        
        // Append comment to description (in a real system, you'd have a separate TicketComment entity)
        String updatedDescription = ticket.getDescription() + "\n\n--- Comment by " + 
            commenter.getFirstName() + " " + commenter.getLastName() + " (" + 
            commenter.getEmail() + ") on " + new java.util.Date() + " ---\n" + comment;
        ticket.setDescription(updatedDescription);
        
        // Update status to IN_PROGRESS if it was OPEN
        if (ticket.getStatus() == SupportTicket.TicketStatus.OPEN) {
            ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        }
        
        return ticketRepository.save(ticket);
    }

    @Override
    public SupportTicket assignTicket(Long ticketId, User assignedTo) {
        SupportTicket ticket = getTicketById(ticketId);
        ticket.setAssignedTo(assignedTo);
        if (ticket.getStatus() == SupportTicket.TicketStatus.OPEN) {
            ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        }
        return ticketRepository.save(ticket);
    }

    @Override
    public Map<String, Object> getTicketStatistics(User customer) {
        Map<String, Object> stats = new HashMap<>();
        
        List<SupportTicket> allTickets = getTicketsByCustomer(customer);
        stats.put("totalTickets", allTickets.size());
        stats.put("openTickets", ticketRepository.countByCustomerAndStatus(customer, SupportTicket.TicketStatus.OPEN));
        stats.put("inProgressTickets", ticketRepository.countByCustomerAndStatus(customer, SupportTicket.TicketStatus.IN_PROGRESS));
        stats.put("resolvedTickets", ticketRepository.countByCustomerAndStatus(customer, SupportTicket.TicketStatus.RESOLVED));
        stats.put("closedTickets", ticketRepository.countByCustomerAndStatus(customer, SupportTicket.TicketStatus.CLOSED));
        
        return stats;
    }
}

