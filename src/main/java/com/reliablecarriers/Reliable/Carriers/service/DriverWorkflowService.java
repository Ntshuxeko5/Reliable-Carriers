package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Complete Driver Workflow Service
 * Handles package acceptance, pickup, delivery, and verification processes
 */
@Service
@Transactional
public class DriverWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(DriverWorkflowService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ProofOfDeliveryRepository proofOfDeliveryRepository;

    @Autowired
    private ComprehensiveNotificationService notificationService;

    @Autowired
    private RealtimeTrackingService realtimeTrackingService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Get available packages for driver to accept
     */
    public List<Map<String, Object>> getAvailablePackages(Long driverId) {
        List<Booking> availableBookings = bookingRepository.findByStatusAndDriverIsNull(BookingStatus.CONFIRMED);
        List<Map<String, Object>> packages = new ArrayList<>();

        for (Booking booking : availableBookings) {
            Map<String, Object> packageInfo = new HashMap<>();
            packageInfo.put("bookingId", booking.getId());
            packageInfo.put("bookingNumber", booking.getBookingNumber());
            packageInfo.put("trackingNumber", booking.getTrackingNumber());
            packageInfo.put("serviceType", booking.getServiceType().getDisplayName());
            packageInfo.put("description", booking.getDescription());
            packageInfo.put("weight", booking.getWeight());
            packageInfo.put("dimensions", booking.getDimensions());
            
            // Pickup details
            packageInfo.put("pickupAddress", booking.getPickupAddress());
            packageInfo.put("pickupCity", booking.getPickupCity());
            packageInfo.put("pickupContact", booking.getPickupContactName());
            packageInfo.put("pickupPhone", booking.getPickupContactPhone());
            packageInfo.put("pickupCode", booking.getCustomerPickupCode());
            
            // Delivery details
            packageInfo.put("deliveryAddress", booking.getDeliveryAddress());
            packageInfo.put("deliveryCity", booking.getDeliveryCity());
            packageInfo.put("deliveryContact", booking.getDeliveryContactName());
            packageInfo.put("deliveryPhone", booking.getDeliveryContactPhone());
            packageInfo.put("deliveryCode", booking.getCustomerDeliveryCode());
            
            // Distance and payment info
            packageInfo.put("totalAmount", booking.getTotalAmount());
            packageInfo.put("createdAt", booking.getCreatedAt());
            
            packages.add(packageInfo);
        }

        return packages;
    }

    /**
     * Driver accepts a package assignment
     */
    public Map<String, Object> acceptPackage(Long bookingId, Long driverId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (booking.getDriver() != null) {
                result.put("success", false);
                result.put("message", "Package already assigned to another driver");
                return result;
            }

            // Find driver
            User driver = new User();
            driver.setId(driverId);
            
            // Assign driver to booking
            booking.setDriver(driver);
            booking.setStatus(BookingStatus.ASSIGNED);
            booking.setUpdatedAt(new Date());
            
            bookingRepository.save(booking);

            // Create or update shipment
            Shipment shipment = shipmentRepository.findByTrackingNumber(booking.getTrackingNumber())
                .orElse(new Shipment());
            
            if (shipment.getId() == null) {
                shipment.setTrackingNumber(booking.getTrackingNumber());
                shipment.setDescription(booking.getDescription());
                shipment.setPickupAddress(booking.getPickupAddress() + ", " + booking.getPickupCity());
                shipment.setDeliveryAddress(booking.getDeliveryAddress() + ", " + booking.getDeliveryCity());
                shipment.setShippingCost(booking.getTotalAmount());
                shipment.setServiceType(ServiceType.valueOf(booking.getServiceType().name()));
                shipment.setCreatedAt(new Date());
            }
            
            shipment.setAssignedDriver(driver);
            shipment.setStatus(ShipmentStatus.ASSIGNED);
            shipmentRepository.save(shipment);

            // Send notifications
            notificationService.sendDriverAssignmentNotification(booking, driver);

            // Send real-time updates
            realtimeTrackingService.sendPackageStatusUpdate(
                booking.getTrackingNumber(), 
                ShipmentStatus.ASSIGNED, 
                "Package assigned to driver", 
                "Driver has accepted the package assignment"
            );

            result.put("success", true);
            result.put("message", "Package accepted successfully");
            result.put("bookingNumber", booking.getBookingNumber());
            result.put("trackingNumber", booking.getTrackingNumber());

            logger.info("Driver {} accepted package {}", driverId, booking.getBookingNumber());

        } catch (Exception e) {
            logger.error("Error accepting package: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to accept package: " + e.getMessage());
        }

        return result;
    }

    /**
     * Driver initiates pickup process
     */
    public Map<String, Object> initiatePickup(Long bookingId, Long driverId, String pickupCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify driver assignment
            if (booking.getDriver() == null || !booking.getDriver().getId().equals(driverId)) {
                result.put("success", false);
                result.put("message", "You are not assigned to this package");
                return result;
            }

            // Verify pickup code
            if (!booking.getCustomerPickupCode().equals(pickupCode)) {
                result.put("success", false);
                result.put("message", "Invalid pickup code. Please verify with the sender.");
                return result;
            }

            // Update booking status
            booking.setStatus(BookingStatus.PICKED_UP);
            booking.setUpdatedAt(new Date());
            bookingRepository.save(booking);

            // Update shipment status
            Shipment shipment = shipmentRepository.findByTrackingNumber(booking.getTrackingNumber())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
            
            shipment.setStatus(ShipmentStatus.PICKED_UP);
            shipmentRepository.save(shipment);

            result.put("success", true);
            result.put("message", "Pickup code verified. Ready to capture pickup details.");
            result.put("requiresSignature", true);
            result.put("requiresPhoto", true);

            logger.info("Pickup initiated for booking {} by driver {}", booking.getBookingNumber(), driverId);

        } catch (Exception e) {
            logger.error("Error initiating pickup: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to initiate pickup: " + e.getMessage());
        }

        return result;
    }

    /**
     * Complete pickup with signature and photo
     */
    public Map<String, Object> completePickup(Long bookingId, Long driverId, String signature, 
                                            MultipartFile pickupPhoto, String notes) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            User driver = new User();
            driver.setId(driverId);

            // Store pickup photo
            String photoUrl = null;
            if (pickupPhoto != null && !pickupPhoto.isEmpty()) {
                photoUrl = fileStorageService.storeFile(pickupPhoto, "pickup-photos");
            }

            // Create proof of pickup record
            ProofOfDelivery proofOfPickup = new ProofOfDelivery();
            proofOfPickup.setShipment(shipmentRepository.findByTrackingNumber(booking.getTrackingNumber()).orElse(null));
            proofOfPickup.setDriver(driver);
            proofOfPickup.setDeliveryDate(new Date());
            proofOfPickup.setDeliveryLocation(booking.getPickupAddress());
            proofOfPickup.setRecipientSignature(signature);
            proofOfPickup.setPackagePhotoUrl(photoUrl);
            proofOfPickup.setRecipientName(booking.getPickupContactName());
            proofOfPickup.setRecipientPhone(booking.getPickupContactPhone());
            proofOfPickup.setDeliveryNotes(notes);
            proofOfPickup.setDeliveryMethod("PICKUP_COMPLETED");
            proofOfPickup.setDeliveryStatus("COMPLETED");
            proofOfPickup.setSignatureRequired(true);
            proofOfPickup.setPhotoRequired(true);
            proofOfPickup.setCreatedAt(new Date());
            proofOfPickup.setUpdatedAt(new Date());
            
            proofOfDeliveryRepository.save(proofOfPickup);

            // Update booking and shipment status
            booking.setStatus(BookingStatus.IN_TRANSIT);
            booking.setUpdatedAt(new Date());
            bookingRepository.save(booking);

            Shipment shipment = shipmentRepository.findByTrackingNumber(booking.getTrackingNumber())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            shipmentRepository.save(shipment);

            // Send notifications
            notificationService.sendPickupNotification(booking, driver);

            // Send real-time updates
            realtimeTrackingService.sendPackageStatusUpdate(
                booking.getTrackingNumber(), 
                ShipmentStatus.IN_TRANSIT, 
                booking.getPickupAddress(), 
                "Package picked up and in transit"
            );

            result.put("success", true);
            result.put("message", "Pickup completed successfully");
            result.put("nextStep", "TRANSPORT_TO_DESTINATION");
            result.put("deliveryAddress", booking.getDeliveryAddress());
            result.put("deliveryContact", booking.getDeliveryContactName());
            result.put("deliveryPhone", booking.getDeliveryContactPhone());

            logger.info("Pickup completed for booking {} by driver {}", booking.getBookingNumber(), driverId);

        } catch (Exception e) {
            logger.error("Error completing pickup: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to complete pickup: " + e.getMessage());
        }

        return result;
    }

    /**
     * Mark package as out for delivery
     */
    public Map<String, Object> markOutForDelivery(Long bookingId, Long driverId, String estimatedDeliveryTime) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            User driver = new User();
            driver.setId(driverId);

            // Update booking status
            booking.setStatus(BookingStatus.OUT_FOR_DELIVERY);
            booking.setUpdatedAt(new Date());
            bookingRepository.save(booking);

            // Update shipment status
            Shipment shipment = shipmentRepository.findByTrackingNumber(booking.getTrackingNumber())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
            shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
            shipmentRepository.save(shipment);

            // Send notifications
            notificationService.sendOutForDeliveryNotification(booking, driver, estimatedDeliveryTime);

            // Send real-time updates
            realtimeTrackingService.sendPackageStatusUpdate(
                booking.getTrackingNumber(), 
                ShipmentStatus.OUT_FOR_DELIVERY, 
                "Out for delivery", 
                "Package is out for delivery to " + booking.getDeliveryAddress()
            );

            result.put("success", true);
            result.put("message", "Package marked as out for delivery");
            result.put("deliveryCode", booking.getCustomerDeliveryCode());
            result.put("deliveryContact", booking.getDeliveryContactName());
            result.put("deliveryPhone", booking.getDeliveryContactPhone());

            logger.info("Package {} marked out for delivery by driver {}", booking.getBookingNumber(), driverId);

        } catch (Exception e) {
            logger.error("Error marking out for delivery: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to mark out for delivery: " + e.getMessage());
        }

        return result;
    }

    /**
     * Verify delivery code and initiate delivery
     */
    public Map<String, Object> verifyDeliveryCode(Long bookingId, Long driverId, String deliveryCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify driver assignment
            if (booking.getDriver() == null || !booking.getDriver().getId().equals(driverId)) {
                result.put("success", false);
                result.put("message", "You are not assigned to this package");
                return result;
            }

            // Verify delivery code
            if (!booking.getCustomerDeliveryCode().equals(deliveryCode)) {
                result.put("success", false);
                result.put("message", "Invalid delivery code. Please verify with the recipient.");
                return result;
            }

            result.put("success", true);
            result.put("message", "Delivery code verified. Ready to complete delivery.");
            result.put("requiresSignature", true);
            result.put("requiresPhoto", true);
            result.put("recipientName", booking.getDeliveryContactName());
            result.put("deliveryAddress", booking.getDeliveryAddress());

            logger.info("Delivery code verified for booking {} by driver {}", booking.getBookingNumber(), driverId);

        } catch (Exception e) {
            logger.error("Error verifying delivery code: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to verify delivery code: " + e.getMessage());
        }

        return result;
    }

    /**
     * Complete delivery with signature and photo
     */
    public Map<String, Object> completeDelivery(Long bookingId, Long driverId, String recipientName, 
                                              String recipientIdNumber, String signature, 
                                              MultipartFile deliveryPhoto, String notes) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            User driver = new User();
            driver.setId(driverId);

            // Store delivery photo
            String photoUrl = null;
            if (deliveryPhoto != null && !deliveryPhoto.isEmpty()) {
                photoUrl = fileStorageService.storeFile(deliveryPhoto, "delivery-photos");
            }

            // Create proof of delivery record
            ProofOfDelivery proofOfDelivery = new ProofOfDelivery();
            proofOfDelivery.setShipment(shipmentRepository.findByTrackingNumber(booking.getTrackingNumber()).orElse(null));
            proofOfDelivery.setDriver(driver);
            proofOfDelivery.setDeliveryDate(new Date());
            proofOfDelivery.setDeliveryLocation(booking.getDeliveryAddress());
            proofOfDelivery.setRecipientSignature(signature);
            proofOfDelivery.setDeliveryPhotoUrl(photoUrl);
            proofOfDelivery.setRecipientName(recipientName);
            proofOfDelivery.setRecipientPhone(booking.getDeliveryContactPhone());
            proofOfDelivery.setRecipientIdNumber(recipientIdNumber);
            proofOfDelivery.setDeliveryNotes(notes);
            proofOfDelivery.setDeliveryMethod("HAND_TO_RECIPIENT");
            proofOfDelivery.setDeliveryStatus("COMPLETED");
            proofOfDelivery.setSignatureRequired(true);
            proofOfDelivery.setPhotoRequired(true);
            proofOfDelivery.setIdVerificationRequired(true);
            proofOfDelivery.setCreatedAt(new Date());
            proofOfDelivery.setUpdatedAt(new Date());
            
            proofOfDeliveryRepository.save(proofOfDelivery);

            // Update booking status
            booking.setStatus(BookingStatus.DELIVERED);
            booking.setUpdatedAt(new Date());
            bookingRepository.save(booking);

            // Update shipment status
            Shipment shipment = shipmentRepository.findByTrackingNumber(booking.getTrackingNumber())
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment.setActualDeliveryDate(new Date());
            shipmentRepository.save(shipment);

            // Send notifications
            notificationService.sendDeliveryConfirmation(booking, driver);

            // Send real-time updates
            realtimeTrackingService.sendPackageStatusUpdate(
                booking.getTrackingNumber(), 
                ShipmentStatus.DELIVERED, 
                booking.getDeliveryAddress(), 
                "Package delivered successfully"
            );

            result.put("success", true);
            result.put("message", "Delivery completed successfully");
            result.put("deliveryTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            result.put("recipientName", recipientName);
            result.put("proofOfDeliveryId", proofOfDelivery.getId());

            logger.info("Delivery completed for booking {} by driver {}", booking.getBookingNumber(), driverId);

        } catch (Exception e) {
            logger.error("Error completing delivery: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to complete delivery: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get driver's assigned packages
     */
    public List<Map<String, Object>> getDriverPackages(Long driverId) {
        List<Booking> driverBookings = bookingRepository.findByDriverIdAndStatusIn(
            driverId, 
            Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.PICKED_UP, BookingStatus.IN_TRANSIT, BookingStatus.OUT_FOR_DELIVERY)
        );

        List<Map<String, Object>> packages = new ArrayList<>();
        for (Booking booking : driverBookings) {
            Map<String, Object> packageInfo = new HashMap<>();
            packageInfo.put("bookingId", booking.getId());
            packageInfo.put("bookingNumber", booking.getBookingNumber());
            packageInfo.put("trackingNumber", booking.getTrackingNumber());
            packageInfo.put("status", booking.getStatus());
            packageInfo.put("serviceType", booking.getServiceType().getDisplayName());
            packageInfo.put("description", booking.getDescription());
            
            // Current action required
            switch (booking.getStatus()) {
                case ASSIGNED:
                    packageInfo.put("nextAction", "GO_TO_PICKUP");
                    packageInfo.put("actionLocation", booking.getPickupAddress());
                    break;
                case PICKED_UP:
                case IN_TRANSIT:
                    packageInfo.put("nextAction", "GO_TO_DELIVERY");
                    packageInfo.put("actionLocation", booking.getDeliveryAddress());
                    break;
                case PAYMENT_PENDING:
                case REFUNDED:
                case CANCELLED:
                case DELIVERED:
                case CONFIRMED:
                case PAYMENT_FAILED:
                case PENDING:
                    // Default handling for other statuses
                    packageInfo.put("nextAction", "NO_ACTION");
                    packageInfo.put("actionLocation", "");
                    break;
                case OUT_FOR_DELIVERY:
                    packageInfo.put("nextAction", "COMPLETE_DELIVERY");
                    packageInfo.put("actionLocation", booking.getDeliveryAddress());
                    break;
            }
            
            packages.add(packageInfo);
        }

        return packages;
    }
}
