package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.BookingStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Unified Package DTO that works for Customers, Drivers, and Admins
 * Combines data from both Booking and Shipment entities
 */
public class UnifiedPackageDTO {
    // Identifiers
    private Long id;
    private Long bookingId; // If from Booking
    private Long shipmentId; // If from Shipment
    private String trackingNumber;
    private String bookingNumber;
    
    // Customer/Sender Information
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    
    // Recipient Information
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    
    // Pickup Details
    private String pickupAddress;
    private String pickupCity;
    private String pickupState;
    private String pickupZipCode;
    private String pickupPostalCode;
    private String pickupCountry;
    private String pickupContactName;
    private String pickupContactPhone;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    
    // Delivery Details
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryZipCode;
    private String deliveryPostalCode;
    private String deliveryCountry;
    private String deliveryContactName;
    private String deliveryContactPhone;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    
    // Package Details
    private Double weight;
    private String dimensions;
    private String description;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String serviceType;
    
    // Status Information
    private BookingStatus bookingStatus;
    private ShipmentStatus shipmentStatus;
    private String unifiedStatus; // Normalized status string
    private String formattedStatus;
    
    // Dates
    private Date createdAt;
    private Date estimatedDeliveryDate;
    private Date actualDeliveryDate;
    private Date pickupDate;
    private Date deliveryDate;
    private String formattedEstimatedDelivery;
    private String formattedActualDelivery;
    private String formattedCreatedAt;
    
    // Driver Information
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String driverEmail;
    private String driverVehicleMake;
    private String driverVehicleModel;
    private String driverVehiclePlate;
    
    // Verification Codes
    private String collectionCode;
    private String dropOffCode;
    private String customerPickupCode;
    private String customerDeliveryCode;
    
    // Tracking Information
    private List<TrackingEvent> trackingEvents;
    private String currentLocation;
    private String lastUpdate;
    
    // Status Flags
    private boolean isDelivered;
    private boolean isInTransit;
    private boolean isPending;
    private boolean isAssigned;
    private boolean isPickedUp;
    
    // Payment Information
    private String paymentStatus;
    private String paymentReference;
    private Date paymentDate;
    
    // Additional Fields
    private String specialInstructions;
    private String businessName;
    private String businessId;
    
    // Route optimization (for drivers)
    private Integer deliveryPriority;
    private Double distanceFromCurrentLocation;
    private Integer estimatedTimeMinutes;
    private String suggestedRoute;
    private Boolean isCurrentlyCarrying;
    
    public static class TrackingEvent {
        private String status;
        private String location;
        private String notes;
        private Date timestamp;
        private String formattedTimestamp;
        
        public TrackingEvent() {}
        
        public TrackingEvent(String status, String location, String notes, Date timestamp) {
            this.status = status;
            this.location = location;
            this.notes = notes;
            this.timestamp = timestamp;
            this.formattedTimestamp = formatTimestamp(timestamp);
        }
        
        private String formatTimestamp(Date timestamp) {
            if (timestamp == null) return "";
            return new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a").format(timestamp);
        }
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        public String getFormattedTimestamp() { return formattedTimestamp; }
        public void setFormattedTimestamp(String formattedTimestamp) { this.formattedTimestamp = formattedTimestamp; }
    }
    
    // Constructor
    public UnifiedPackageDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    
    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public String getPickupCity() { return pickupCity; }
    public void setPickupCity(String pickupCity) { this.pickupCity = pickupCity; }
    
    public String getPickupState() { return pickupState; }
    public void setPickupState(String pickupState) { this.pickupState = pickupState; }
    
    public String getPickupZipCode() { return pickupZipCode != null ? pickupZipCode : pickupPostalCode; }
    public void setPickupZipCode(String pickupZipCode) { this.pickupZipCode = pickupZipCode; }
    
    public String getPickupPostalCode() { return pickupPostalCode != null ? pickupPostalCode : pickupZipCode; }
    public void setPickupPostalCode(String pickupPostalCode) { this.pickupPostalCode = pickupPostalCode; }
    
    public String getPickupCountry() { return pickupCountry; }
    public void setPickupCountry(String pickupCountry) { this.pickupCountry = pickupCountry; }
    
    public String getPickupContactName() { return pickupContactName; }
    public void setPickupContactName(String pickupContactName) { this.pickupContactName = pickupContactName; }
    
    public String getPickupContactPhone() { return pickupContactPhone; }
    public void setPickupContactPhone(String pickupContactPhone) { this.pickupContactPhone = pickupContactPhone; }
    
    public BigDecimal getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(BigDecimal pickupLatitude) { this.pickupLatitude = pickupLatitude; }
    
    public BigDecimal getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(BigDecimal pickupLongitude) { this.pickupLongitude = pickupLongitude; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }
    
    public String getDeliveryState() { return deliveryState; }
    public void setDeliveryState(String deliveryState) { this.deliveryState = deliveryState; }
    
    public String getDeliveryZipCode() { return deliveryZipCode != null ? deliveryZipCode : deliveryPostalCode; }
    public void setDeliveryZipCode(String deliveryZipCode) { this.deliveryZipCode = deliveryZipCode; }
    
    public String getDeliveryPostalCode() { return deliveryPostalCode != null ? deliveryPostalCode : deliveryZipCode; }
    public void setDeliveryPostalCode(String deliveryPostalCode) { this.deliveryPostalCode = deliveryPostalCode; }
    
    public String getDeliveryCountry() { return deliveryCountry; }
    public void setDeliveryCountry(String deliveryCountry) { this.deliveryCountry = deliveryCountry; }
    
    public String getDeliveryContactName() { return deliveryContactName; }
    public void setDeliveryContactName(String deliveryContactName) { this.deliveryContactName = deliveryContactName; }
    
    public String getDeliveryContactPhone() { return deliveryContactPhone; }
    public void setDeliveryContactPhone(String deliveryContactPhone) { this.deliveryContactPhone = deliveryContactPhone; }
    
    public BigDecimal getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(BigDecimal deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }
    
    public BigDecimal getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(BigDecimal deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getShippingCost() { return shippingCost != null ? shippingCost : totalAmount; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }
    
    public BigDecimal getTotalAmount() { return totalAmount != null ? totalAmount : shippingCost; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
    
    public ShipmentStatus getShipmentStatus() { return shipmentStatus; }
    public void setShipmentStatus(ShipmentStatus shipmentStatus) { this.shipmentStatus = shipmentStatus; }
    
    public String getUnifiedStatus() { return unifiedStatus; }
    public void setUnifiedStatus(String unifiedStatus) { this.unifiedStatus = unifiedStatus; }
    
    public String getFormattedStatus() { return formattedStatus; }
    public void setFormattedStatus(String formattedStatus) { this.formattedStatus = formattedStatus; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    
    public Date getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(Date actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
    
    public Date getPickupDate() { return pickupDate; }
    public void setPickupDate(Date pickupDate) { this.pickupDate = pickupDate; }
    
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public String getFormattedEstimatedDelivery() { return formattedEstimatedDelivery; }
    public void setFormattedEstimatedDelivery(String formattedEstimatedDelivery) { this.formattedEstimatedDelivery = formattedEstimatedDelivery; }
    
    public String getFormattedActualDelivery() { return formattedActualDelivery; }
    public void setFormattedActualDelivery(String formattedActualDelivery) { this.formattedActualDelivery = formattedActualDelivery; }
    
    public String getFormattedCreatedAt() { return formattedCreatedAt; }
    public void setFormattedCreatedAt(String formattedCreatedAt) { this.formattedCreatedAt = formattedCreatedAt; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    
    public String getDriverEmail() { return driverEmail; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
    
    public String getDriverVehicleMake() { return driverVehicleMake; }
    public void setDriverVehicleMake(String driverVehicleMake) { this.driverVehicleMake = driverVehicleMake; }
    
    public String getDriverVehicleModel() { return driverVehicleModel; }
    public void setDriverVehicleModel(String driverVehicleModel) { this.driverVehicleModel = driverVehicleModel; }
    
    public String getDriverVehiclePlate() { return driverVehiclePlate; }
    public void setDriverVehiclePlate(String driverVehiclePlate) { this.driverVehiclePlate = driverVehiclePlate; }
    
    public String getCollectionCode() { return collectionCode != null ? collectionCode : customerPickupCode; }
    public void setCollectionCode(String collectionCode) { this.collectionCode = collectionCode; }
    
    public String getDropOffCode() { return dropOffCode != null ? dropOffCode : customerDeliveryCode; }
    public void setDropOffCode(String dropOffCode) { this.dropOffCode = dropOffCode; }
    
    public String getCustomerPickupCode() { return customerPickupCode != null ? customerPickupCode : collectionCode; }
    public void setCustomerPickupCode(String customerPickupCode) { this.customerPickupCode = customerPickupCode; }
    
    public String getCustomerDeliveryCode() { return customerDeliveryCode != null ? customerDeliveryCode : dropOffCode; }
    public void setCustomerDeliveryCode(String customerDeliveryCode) { this.customerDeliveryCode = customerDeliveryCode; }
    
    public List<TrackingEvent> getTrackingEvents() { return trackingEvents; }
    public void setTrackingEvents(List<TrackingEvent> trackingEvents) { this.trackingEvents = trackingEvents; }
    
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    
    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
    
    public boolean isDelivered() { return isDelivered; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    
    public boolean isInTransit() { return isInTransit; }
    public void setInTransit(boolean inTransit) { isInTransit = inTransit; }
    
    public boolean isPending() { return isPending; }
    public void setPending(boolean pending) { isPending = pending; }
    
    public boolean isAssigned() { return isAssigned; }
    public void setAssigned(boolean assigned) { isAssigned = assigned; }
    
    public boolean isPickedUp() { return isPickedUp; }
    public void setPickedUp(boolean pickedUp) { isPickedUp = pickedUp; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    
    public Integer getDeliveryPriority() { return deliveryPriority; }
    public void setDeliveryPriority(Integer deliveryPriority) { this.deliveryPriority = deliveryPriority; }
    
    public Double getDistanceFromCurrentLocation() { return distanceFromCurrentLocation; }
    public void setDistanceFromCurrentLocation(Double distanceFromCurrentLocation) { this.distanceFromCurrentLocation = distanceFromCurrentLocation; }
    
    public Integer getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }
    
    public String getSuggestedRoute() { return suggestedRoute; }
    public void setSuggestedRoute(String suggestedRoute) { this.suggestedRoute = suggestedRoute; }
    
    public Boolean getIsCurrentlyCarrying() { return isCurrentlyCarrying; }
    public void setIsCurrentlyCarrying(Boolean isCurrentlyCarrying) { this.isCurrentlyCarrying = isCurrentlyCarrying; }
}

