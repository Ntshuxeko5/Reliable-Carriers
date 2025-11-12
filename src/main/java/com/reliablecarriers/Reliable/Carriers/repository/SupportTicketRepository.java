package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.SupportTicket;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    
    List<SupportTicket> findByCustomerOrderByCreatedAtDesc(User customer);
    
    List<SupportTicket> findByCustomerAndStatusOrderByCreatedAtDesc(User customer, SupportTicket.TicketStatus status);
    
    List<SupportTicket> findByAssignedToOrderByCreatedAtDesc(User assignedTo);
    
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(SupportTicket.TicketStatus status);
    
    List<SupportTicket> findByPriorityOrderByCreatedAtDesc(SupportTicket.TicketPriority priority);
    
    List<SupportTicket> findByCategoryOrderByCreatedAtDesc(SupportTicket.TicketCategory category);
    
    List<SupportTicket> findByCreatedAtBetweenOrderByCreatedAtDesc(Date startDate, Date endDate);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.customer = :customer AND (t.status = 'OPEN' OR t.status = 'IN_PROGRESS') ORDER BY t.createdAt DESC")
    List<SupportTicket> findActiveTicketsByCustomer(@Param("customer") User customer);
    
    long countByCustomerAndStatus(User customer, SupportTicket.TicketStatus status);
    
    long countByStatus(SupportTicket.TicketStatus status);
}

