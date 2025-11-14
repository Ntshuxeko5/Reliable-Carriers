package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "moving_services")
public class MovingService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;
    
    @Column(nullable = false)
    private String pickupAddress;
    
    @Column(nullable = false)
    private String deliveryAddress;
    
    @Column(nullable = false)
    private Double distanceKm;
    
    @Column(nullable = false)
    private BigDecimal basePrice;
    
    @Column(nullable = false)
    private BigDecimal totalPrice;
    
    @Column(nullable = false)
    private BigDecimal pricePerKm;
    
    @Column(nullable = false)
    private Integer maxFreeDistanceKm;
    
    @Column(nullable = false)
    private BigDecimal maxFreePrice;
    
    @Column(nullable = false)
    private String description;
    
    // Weight is optional for moving services
    @Column
    private Double weightKg;
    
    @Column(nullable = false)
    private Integer numberOfItems;
    
    @Column
    private Integer numberOfLoads;
    
    @Column
    private String truckSize; // SMALL, MEDIUM, LARGE, EXTRA_LARGE
    
    @Column
    private Boolean fullMoving; // true for full moving, false for partial
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;
    
    @Column(nullable = false)
    private Date requestedDate;
    
    @Column
    private Date scheduledDate;
    
    @Column
    private Date completedDate;
    
    @Column
    private String specialInstructions;
    
    @Column(nullable = false)
    private Date createdAt;
    
    @Column(nullable = false)
    private Date updatedAt;
    
    // Constructor
    public MovingService() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = ShipmentStatus.PENDING;
        this.maxFreeDistanceKm = 20; // 20km radius
        this.maxFreePrice = new BigDecimal("550.00"); // R550 capped
        this.pricePerKm = new BigDecimal("25.00"); // R25 per km after 20km
    }
    
    // Calculate total price based on distance
    public void calculatePrice() {
        if (distanceKm <= maxFreeDistanceKm) {
            this.totalPrice = this.maxFreePrice;
        } else {
            double extraDistance = distanceKm - maxFreeDistanceKm;
            BigDecimal extraCost = this.pricePerKm.multiply(BigDecimal.valueOf(extraDistance));
            this.totalPrice = this.maxFreePrice.add(extraCost);
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getCustomer() {
        return customer;
    }
    
    public void setCustomer(User customer) {
        this.customer = customer;
    }
    
    public User getDriver() {
        return driver;
    }
    
    public void setDriver(User driver) {
        this.driver = driver;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getPickupAddress() {
        return pickupAddress;
    }
    
    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
        calculatePrice(); // Recalculate price when distance changes
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }
    
    public void setPricePerKm(BigDecimal pricePerKm) {
        this.pricePerKm = pricePerKm;
    }
    
    public Integer getMaxFreeDistanceKm() {
        return maxFreeDistanceKm;
    }
    
    public void setMaxFreeDistanceKm(Integer maxFreeDistanceKm) {
        this.maxFreeDistanceKm = maxFreeDistanceKm;
    }
    
    public BigDecimal getMaxFreePrice() {
        return maxFreePrice;
    }
    
    public void setMaxFreePrice(BigDecimal maxFreePrice) {
        this.maxFreePrice = maxFreePrice;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getWeightKg() {
        return weightKg;
    }
    
    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
    
    public Integer getNumberOfItems() {
        return numberOfItems;
    }
    
    public void setNumberOfItems(Integer numberOfItems) {
        this.numberOfItems = numberOfItems;
    }
    
    public Integer getNumberOfLoads() {
        return numberOfLoads;
    }
    
    public void setNumberOfLoads(Integer numberOfLoads) {
        this.numberOfLoads = numberOfLoads;
    }
    
    public String getTruckSize() {
        return truckSize;
    }
    
    public void setTruckSize(String truckSize) {
        this.truckSize = truckSize;
    }
    
    public Boolean getFullMoving() {
        return fullMoving;
    }
    
    public void setFullMoving(Boolean fullMoving) {
        this.fullMoving = fullMoving;
    }
    
    public ShipmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }
    
    public Date getRequestedDate() {
        return requestedDate;
    }
    
    public void setRequestedDate(Date requestedDate) {
        this.requestedDate = requestedDate;
    }
    
    public Date getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public Date getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
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
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }
}
