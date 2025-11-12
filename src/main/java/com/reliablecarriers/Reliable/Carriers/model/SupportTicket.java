package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "support_tickets", indexes = {
    @Index(name = "idx_tickets_ticket_number", columnList = "ticketNumber"),
    @Index(name = "idx_tickets_status", columnList = "status"),
    @Index(name = "idx_tickets_priority", columnList = "priority"),
    @Index(name = "idx_tickets_customer", columnList = "customer_id"),
    @Index(name = "idx_tickets_created", columnList = "createdAt")
})
public class SupportTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String ticketNumber;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull
    private User customer;
    
    @ManyToOne
    @JoinColumn(name = "assigned_to_id", nullable = true)
    private User assignedTo;
    
    @ManyToOne
    @JoinColumn(name = "related_booking_id", nullable = true)
    private Booking relatedBooking;
    
    @ManyToOne
    @JoinColumn(name = "related_shipment_id", nullable = true)
    private Shipment relatedShipment;
    
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String subject;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date resolvedAt;
    
    @Column(columnDefinition = "TEXT")
    private String resolution;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (status == null) {
            status = TicketStatus.OPEN;
        }
        if (priority == null) {
            priority = TicketPriority.MEDIUM;
        }
        if (category == null) {
            category = TicketCategory.GENERAL;
        }
        if (ticketNumber == null) {
            ticketNumber = generateTicketNumber();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            if (resolvedAt == null) {
                resolvedAt = new Date();
            }
        }
    }
    
    private String generateTicketNumber() {
        return "TKT-" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
    }
    
    // Enums
    public enum TicketPriority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum TicketStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
    }
    
    public enum TicketCategory {
        GENERAL, PACKAGE_TRACKING, PAYMENT, DELIVERY_ISSUE, DAMAGE_CLAIM, REFUND, TECHNICAL, OTHER
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTicketNumber() {
        return ticketNumber;
    }
    
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    
    public User getCustomer() {
        return customer;
    }
    
    public void setCustomer(User customer) {
        this.customer = customer;
    }
    
    public User getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public Booking getRelatedBooking() {
        return relatedBooking;
    }
    
    public void setRelatedBooking(Booking relatedBooking) {
        this.relatedBooking = relatedBooking;
    }
    
    public Shipment getRelatedShipment() {
        return relatedShipment;
    }
    
    public void setRelatedShipment(Shipment relatedShipment) {
        this.relatedShipment = relatedShipment;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TicketPriority getPriority() {
        return priority;
    }
    
    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
    
    public TicketStatus getStatus() {
        return status;
    }
    
    public void setStatus(TicketStatus status) {
        this.status = status;
    }
    
    public TicketCategory getCategory() {
        return category;
    }
    
    public void setCategory(TicketCategory category) {
        this.category = category;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Date getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}

