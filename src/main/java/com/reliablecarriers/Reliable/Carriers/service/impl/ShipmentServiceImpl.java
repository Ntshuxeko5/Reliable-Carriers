package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentTrackingRepository;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import com.reliablecarriers.Reliable.Carriers.service.PricingService;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentService;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import com.reliablecarriers.Reliable.Carriers.service.GoogleMapsGeocodingService;
import com.reliablecarriers.Reliable.Carriers.dto.AddressCoordinates;
import com.reliablecarriers.Reliable.Carriers.controller.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final PricingService pricingService;
    private final WebSocketController webSocketController;
    private final GoogleMapsGeocodingService geocodingService;

    @Autowired
    public ShipmentServiceImpl(ShipmentRepository shipmentRepository, 
                              ShipmentTrackingRepository trackingRepository,
                              UserService userService,
                              NotificationService notificationService,
                              PricingService pricingService,
                              WebSocketController webSocketController,
                              GoogleMapsGeocodingService geocodingService) {
        this.shipmentRepository = shipmentRepository;
        this.trackingRepository = trackingRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.pricingService = pricingService;
        this.webSocketController = webSocketController;
        this.geocodingService = geocodingService;
    }

    @Override
    @Transactional
    public Shipment createShipment(Shipment shipment) {
        // Generate tracking number
        shipment.setTrackingNumber(generateTrackingNumber());
        
        // Set initial status if not set
        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.PENDING);
        }
        
        // Set default service type if not set
        if (shipment.getServiceType() == null) {
            shipment.setServiceType(ServiceType.ECONOMY);
        }
        
        // Geocode addresses if coordinates are not already set
        geocodeShipmentAddresses(shipment);
        
        // Calculate shipping cost based on service type
        if (shipment.getShippingCost() == null) {
            if (shipment.getServiceType().isCourierService()) {
                shipment.setShippingCost(pricingService.calculateCourierPrice(shipment.getServiceType()));
            } else {
                // For moving services, we need distance calculation
                // This would typically be calculated from pickup and delivery addresses
                // For now, we'll use a default calculation or require it to be set
                shipment.setShippingCost(shipment.getServiceType().getBasePrice());
            }
        }
        
        // Save the shipment
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Create initial tracking entry
        createTrackingEntry(savedShipment, savedShipment.getStatus(), 
                           "Shipment created", "Shipment registered in the system");
        
        // Send notification
        notificationService.sendShipmentCreatedNotification(savedShipment);
        
        return savedShipment;
    }

    @Override
    @Transactional
    public Shipment updateShipment(Long id, Shipment shipment) {
        Shipment existingShipment = getShipmentById(id);
        
        // Update fields
        existingShipment.setRecipientName(shipment.getRecipientName());
        existingShipment.setRecipientEmail(shipment.getRecipientEmail());
        existingShipment.setRecipientPhone(shipment.getRecipientPhone());
        existingShipment.setPickupAddress(shipment.getPickupAddress());
        existingShipment.setPickupCity(shipment.getPickupCity());
        existingShipment.setPickupState(shipment.getPickupState());
        existingShipment.setPickupZipCode(shipment.getPickupZipCode());
        existingShipment.setPickupCountry(shipment.getPickupCountry());
        existingShipment.setDeliveryAddress(shipment.getDeliveryAddress());
        existingShipment.setDeliveryCity(shipment.getDeliveryCity());
        existingShipment.setDeliveryState(shipment.getDeliveryState());
        existingShipment.setDeliveryZipCode(shipment.getDeliveryZipCode());
        existingShipment.setDeliveryCountry(shipment.getDeliveryCountry());
        existingShipment.setWeight(shipment.getWeight());
        existingShipment.setDimensions(shipment.getDimensions());
        existingShipment.setDescription(shipment.getDescription());
        existingShipment.setShippingCost(shipment.getShippingCost());
        existingShipment.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        
        // Update coordinates if addresses changed or coordinates are missing
        boolean addressesChanged = !existingShipment.getPickupAddress().equals(shipment.getPickupAddress()) ||
                                  !existingShipment.getDeliveryAddress().equals(shipment.getDeliveryAddress());
        
        if (addressesChanged || existingShipment.getPickupLatitude() == null || 
            existingShipment.getDeliveryLatitude() == null) {
            geocodeShipmentAddresses(existingShipment);
        }
        
        // Don't update status, tracking number, or actual delivery date here
        // Those should be updated through specific methods
        
        return shipmentRepository.save(existingShipment);
    }

    @Override
    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
    }

    @Override
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found with tracking number: " + trackingNumber));
    }

    @Override
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @Override
    public List<Shipment> getShipmentsBySender(User sender) {
        return shipmentRepository.findBySender(sender);
    }

    @Override
    public List<Shipment> getShipmentsByDriver(User driver) {
        return shipmentRepository.findByAssignedDriver(driver);
    }

    @Override
    public List<Shipment> getShipmentsByDriverId(Long driverId) {
        return shipmentRepository.findByAssignedDriverId(driverId);
    }

    @Override
    public List<Shipment> getShipmentsByStatus(ShipmentStatus status) {
        return shipmentRepository.findByStatus(status);
    }

    @Override
    public List<Shipment> getShipmentsByDateRange(Date startDate, Date endDate) {
        return shipmentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Shipment> getShipmentsByDeliveryDateRange(Date startDate, Date endDate) {
        return shipmentRepository.findByEstimatedDeliveryDateBetween(startDate, endDate);
    }

    @Override
    public List<Shipment> getShipmentsByPickupLocation(String city, String state) {
        return shipmentRepository.findByPickupCityAndPickupState(city, state);
    }

    @Override
    public List<Shipment> getShipmentsByDeliveryLocation(String city, String state) {
        return shipmentRepository.findByDeliveryCityAndDeliveryState(city, state);
    }

    @Override
    public void deleteShipment(Long id) {
        Shipment shipment = getShipmentById(id);
        shipmentRepository.delete(shipment);
    }

    @Override
    public Shipment assignDriverToShipment(Long shipmentId, Long driverId) {
        Shipment shipment = getShipmentById(shipmentId);
        User driver = userService.getUserById(driverId);
        
        shipment.setAssignedDriver(driver);
        
        // Create tracking entry for driver assignment
        createTrackingEntry(shipment, shipment.getStatus(), 
                          "Driver assigned", "Shipment assigned to driver: " + driver.getFirstName() + " " + driver.getLastName());
        
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Send notification to driver
        notificationService.sendDriverAssignedNotification(savedShipment, driver);
        
        return savedShipment;
    }

    @Override
    public Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus status, String location, String notes) {
        Shipment shipment = getShipmentById(shipmentId);
        ShipmentStatus oldStatus = shipment.getStatus();
        
        // Update status
        shipment.setStatus(status);
        
        // If delivered, set actual delivery date
        if (status == ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryDate(new Date());
        }
        
        // Create tracking entry
        createTrackingEntry(shipment, status, location, notes);
        
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Send appropriate notifications based on status change
        sendStatusSpecificNotifications(savedShipment, oldStatus, status, location, notes);
        
        return savedShipment;
    }
    
    /**
     * Geocode shipment addresses to get coordinates
     * Only geocodes if coordinates are not already set
     */
    private void geocodeShipmentAddresses(Shipment shipment) {
        try {
            // Geocode pickup address if coordinates are missing
            if (shipment.getPickupLatitude() == null || shipment.getPickupLongitude() == null) {
                String fullPickupAddress = buildFullAddress(
                    shipment.getPickupAddress(),
                    shipment.getPickupCity(),
                    shipment.getPickupState(),
                    shipment.getPickupZipCode(),
                    shipment.getPickupCountry()
                );
                
                AddressCoordinates pickupCoords = geocodingService.validateAndNormalizeAddress(fullPickupAddress);
                if (pickupCoords != null) {
                    shipment.setPickupLatitude(pickupCoords.getLatitude());
                    shipment.setPickupLongitude(pickupCoords.getLongitude());
                    // Optionally update address with formatted version
                    if (pickupCoords.getFormattedAddress() != null && !pickupCoords.getFormattedAddress().isEmpty()) {
                        shipment.setPickupAddress(pickupCoords.getFormattedAddress());
                    }
                } else {
                    System.err.println("Failed to geocode pickup address: " + fullPickupAddress);
                }
            }
            
            // Geocode delivery address if coordinates are missing
            if (shipment.getDeliveryLatitude() == null || shipment.getDeliveryLongitude() == null) {
                String fullDeliveryAddress = buildFullAddress(
                    shipment.getDeliveryAddress(),
                    shipment.getDeliveryCity(),
                    shipment.getDeliveryState(),
                    shipment.getDeliveryZipCode(),
                    shipment.getDeliveryCountry()
                );
                
                AddressCoordinates deliveryCoords = geocodingService.validateAndNormalizeAddress(fullDeliveryAddress);
                if (deliveryCoords != null) {
                    shipment.setDeliveryLatitude(deliveryCoords.getLatitude());
                    shipment.setDeliveryLongitude(deliveryCoords.getLongitude());
                    // Optionally update address with formatted version
                    if (deliveryCoords.getFormattedAddress() != null && !deliveryCoords.getFormattedAddress().isEmpty()) {
                        shipment.setDeliveryAddress(deliveryCoords.getFormattedAddress());
                    }
                } else {
                    System.err.println("Failed to geocode delivery address: " + fullDeliveryAddress);
                }
            }
        } catch (Exception e) {
            System.err.println("Error geocoding shipment addresses: " + e.getMessage());
            // Don't fail shipment creation if geocoding fails
        }
    }
    
    /**
     * Build full address string from components
     */
    private String buildFullAddress(String address, String city, String state, String zipCode, String country) {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null && !address.isEmpty()) {
            fullAddress.append(address);
        }
        if (city != null && !city.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(state);
        }
        if (zipCode != null && !zipCode.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(zipCode);
        }
        if (country != null && !country.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country);
        }
        return fullAddress.toString();
    }
    
    // Helper methods
    private String generateTrackingNumber() {
        // Generate a unique tracking number with collision check
        String trackingNumber;
        do {
            trackingNumber = "RC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (shipmentRepository.findByTrackingNumber(trackingNumber).isPresent());
        return trackingNumber;
    }
    
    private void createTrackingEntry(Shipment shipment, ShipmentStatus status, String location, String notes) {
        ShipmentTracking tracking = new ShipmentTracking();
        tracking.setShipment(shipment);
        tracking.setStatus(status);
        tracking.setLocation(location);
        tracking.setNotes(notes);
        
        trackingRepository.save(tracking);
    }
    
    /**
     * Send appropriate notifications based on shipment status change
     */
    private void sendStatusSpecificNotifications(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus, String location, String notes) {
        String driverName = shipment.getAssignedDriver() != null ? 
            shipment.getAssignedDriver().getFirstName() + " " + shipment.getAssignedDriver().getLastName() : "Assigned Driver";
        
        // Send WebSocket real-time update
        if (webSocketController != null) {
            webSocketController.sendPackageStatusUpdate(
                shipment.getTrackingNumber(), 
                newStatus, 
                location != null ? location : "Location not specified"
            );
        }
        
        switch (newStatus) {
            case PICKED_UP:
                notificationService.sendShipmentPickedUpNotification(shipment, driverName);
                break;
            case IN_TRANSIT:
                notificationService.sendShipmentInTransitNotification(shipment, driverName, location);
                break;
            case OUT_FOR_DELIVERY:
                notificationService.sendShipmentOutForDeliveryNotification(shipment, driverName, "Within 2 hours");
                break;
            case DELIVERED:
                notificationService.sendShipmentDeliveredNotification(shipment, driverName, new Date().toString());
                break;
            case FAILED_DELIVERY:
                notificationService.sendShipmentFailedDeliveryNotification(shipment, notes, "Next business day");
                break;
            case CANCELLED:
                notificationService.sendShipmentCancelledNotification(shipment, notes);
                break;
            default:
                // Send general status update notification
                notificationService.sendShipmentStatusUpdateNotification(shipment, oldStatus, newStatus, location);
                break;
        }
    }
}