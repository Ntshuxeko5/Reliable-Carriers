package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_bookings_booking_number", columnList = "bookingNumber"),
    @Index(name = "idx_bookings_status", columnList = "status"),
    @Index(name = "idx_bookings_customer_email", columnList = "customerEmail"),
    @Index(name = "idx_bookings_assigned_driver", columnList = "assigned_driver_id"),
    @Index(name = "idx_bookings_status_created", columnList = "status,created_at"),
    @Index(name = "idx_bookings_payment_status", columnList = "payment_status"),
    @Index(name = "idx_bookings_pickup_coords", columnList = "pickup_latitude,pickup_longitude"),
    @Index(name = "idx_bookings_delivery_coords", columnList = "delivery_latitude,delivery_longitude")
})
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String bookingNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;
    
    // Customer details
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String customerName;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String customerEmail;
    
    @Column(nullable = false, length = 15)
    @NotBlank
    @Size(max = 15)
    private String customerPhone;
    
    // Pickup details
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String pickupAddress;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String pickupCity;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String pickupState;
    
    @Column(nullable = false, length = 10)
    @NotBlank
    @Size(max = 10)
    private String pickupPostalCode;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String pickupContactName;
    
    @Column(nullable = false, length = 15)
    @NotBlank
    @Size(max = 15)
    private String pickupContactPhone;
    
    // Google Maps coordinates for pickup
    @Column(precision = 10, scale = 8)
    private BigDecimal pickupLatitude;
    
    @Column(precision = 11, scale = 8)
    private BigDecimal pickupLongitude;
    
    // Delivery details
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String deliveryAddress;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String deliveryCity;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String deliveryState;
    
    @Column(nullable = false, length = 10)
    @NotBlank
    @Size(max = 10)
    private String deliveryPostalCode;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String deliveryContactName;
    
    @Column(nullable = false, length = 15)
    @NotBlank
    @Size(max = 15)
    private String deliveryContactPhone;
    
    // Google Maps coordinates for delivery
    @Column(precision = 10, scale = 8)
    private BigDecimal deliveryLatitude;
    
    @Column(precision = 11, scale = 8)
    private BigDecimal deliveryLongitude;
    
    // Package details
    @Column(nullable = false)
    @NotNull
    @Positive
    private Double weight;
    
    @Column(length = 20)
    @Size(max = 20)
    private String dimensions;
    
    @Column(length = 500)
    @Size(max = 500)
    private String description;
    
    // Dates
    @Temporal(TemporalType.TIMESTAMP)
    private Date pickupDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveryDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date estimatedDeliveryDate;
    
    // Pricing
    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basePrice;
    
    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal serviceFee;
    
    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal insuranceFee;
    
    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal fuelSurcharge;
    
    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal totalAmount;
    
    // Additional services
    @Column(nullable = false)
    private boolean insurance = false;
    
    @Column(nullable = false)
    private boolean packing = false;
    
    @Column(nullable = false)
    private boolean saturdayDelivery = false;
    
    @Column(nullable = false)
    private boolean signatureRequired = false;
    
    @Column(length = 500)
    @Size(max = 500)
    private String specialInstructions;
    
    // Payment details
    @Column(length = 100)
    @Size(max = 100)
    private String paymentReference;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;
    
    // Tracking
    @Column(length = 50)
    @Size(max = 50)
    private String trackingNumber;
    
    @Column(length = 50)
    @Size(max = 50)
    private String shipmentId;
    
    // Customer verification codes
    @Column(length = 10)
    @Size(max = 10)
    private String customerPickupCode;
    
    @Column(length = 10)
    @Size(max = 10)
    private String customerDeliveryCode;
    
    // Driver assignment
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;
    
    // Timestamps
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
        if (bookingNumber == null) {
            bookingNumber = generateBookingNumber();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupCity() {
        return pickupCity;
    }

    public void setPickupCity(String pickupCity) {
        this.pickupCity = pickupCity;
    }

    public String getPickupState() {
        return pickupState;
    }

    public void setPickupState(String pickupState) {
        this.pickupState = pickupState;
    }

    public String getPickupPostalCode() {
        return pickupPostalCode;
    }

    public void setPickupPostalCode(String pickupPostalCode) {
        this.pickupPostalCode = pickupPostalCode;
    }

    public String getPickupContactName() {
        return pickupContactName;
    }

    public void setPickupContactName(String pickupContactName) {
        this.pickupContactName = pickupContactName;
    }

    public String getPickupContactPhone() {
        return pickupContactPhone;
    }

    public void setPickupContactPhone(String pickupContactPhone) {
        this.pickupContactPhone = pickupContactPhone;
    }

    public BigDecimal getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(BigDecimal pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public BigDecimal getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(BigDecimal pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(String deliveryState) {
        this.deliveryState = deliveryState;
    }

    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    public void setDeliveryPostalCode(String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
    }

    public String getDeliveryContactName() {
        return deliveryContactName;
    }

    public void setDeliveryContactName(String deliveryContactName) {
        this.deliveryContactName = deliveryContactName;
    }

    public String getDeliveryContactPhone() {
        return deliveryContactPhone;
    }

    public void setDeliveryContactPhone(String deliveryContactPhone) {
        this.deliveryContactPhone = deliveryContactPhone;
    }

    public BigDecimal getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(BigDecimal deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public BigDecimal getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(BigDecimal deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(Date pickupDate) {
        this.pickupDate = pickupDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Date getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getInsuranceFee() {
        return insuranceFee;
    }

    public void setInsuranceFee(BigDecimal insuranceFee) {
        this.insuranceFee = insuranceFee;
    }

    public BigDecimal getFuelSurcharge() {
        return fuelSurcharge;
    }

    public void setFuelSurcharge(BigDecimal fuelSurcharge) {
        this.fuelSurcharge = fuelSurcharge;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isInsurance() {
        return insurance;
    }

    public void setInsurance(boolean insurance) {
        this.insurance = insurance;
    }

    public boolean isPacking() {
        return packing;
    }

    public void setPacking(boolean packing) {
        this.packing = packing;
    }

    public boolean isSaturdayDelivery() {
        return saturdayDelivery;
    }

    public void setSaturdayDelivery(boolean saturdayDelivery) {
        this.saturdayDelivery = saturdayDelivery;
    }

    public boolean isSignatureRequired() {
        return signatureRequired;
    }

    public void setSignatureRequired(boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
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

    public String getCustomerPickupCode() {
        return customerPickupCode;
    }

    public void setCustomerPickupCode(String customerPickupCode) {
        this.customerPickupCode = customerPickupCode;
    }

    public String getCustomerDeliveryCode() {
        return customerDeliveryCode;
    }

    public void setCustomerDeliveryCode(String customerDeliveryCode) {
        this.customerDeliveryCode = customerDeliveryCode;
    }
}
