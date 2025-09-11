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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    public ShipmentServiceImpl(ShipmentRepository shipmentRepository, 
                              ShipmentTrackingRepository trackingRepository,
                              UserService userService,
                              NotificationService notificationService,
                              PricingService pricingService) {
        this.shipmentRepository = shipmentRepository;
        this.trackingRepository = trackingRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.pricingService = pricingService;
    }

    @Override
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