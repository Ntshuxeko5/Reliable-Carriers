package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import com.reliablecarriers.Reliable.Carriers.service.UnifiedPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UnifiedPackageServiceImpl implements UnifiedPackageService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ShipmentTrackingRepository shipmentTrackingRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UnifiedPackageDTO> getAllPackages() {
        Set<String> processedTrackingNumbers = new HashSet<>();
        List<UnifiedPackageDTO> packages = new ArrayList<>();

        // Get all shipments (confirmed bookings)
        List<Shipment> shipments = shipmentRepository.findAll();
        for (Shipment shipment : shipments) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
            // Try to find associated booking
            if (shipment.getTrackingNumber() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                if (bookingOpt.isPresent()) {
                    mergeBookingData(dto, bookingOpt.get());
                }
            }
            packages.add(dto);
            processedTrackingNumbers.add(shipment.getTrackingNumber());
        }

        // Get bookings without shipments (pending bookings)
        List<Booking> bookings = bookingRepository.findAll();
        for (Booking booking : bookings) {
            if (booking.getTrackingNumber() == null || !processedTrackingNumbers.contains(booking.getTrackingNumber())) {
                UnifiedPackageDTO dto = convertBookingToDTO(booking);
                packages.add(dto);
            }
        }

        // Sort by creation date descending
        packages.sort((a, b) -> {
            Date dateA = a.getCreatedAt() != null ? a.getCreatedAt() : new Date(0);
            Date dateB = b.getCreatedAt() != null ? b.getCreatedAt() : new Date(0);
            return dateB.compareTo(dateA);
        });

        return packages;
    }

    @Override
    public UnifiedPackageDTO getPackageByTrackingNumber(String trackingNumber) {
        // Try to find shipment first
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipmentOpt.get());
            // Try to find associated booking
            Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
            if (bookingOpt.isPresent()) {
                mergeBookingData(dto, bookingOpt.get());
            }
            return dto;
        }

        // Try to find booking
        Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
        if (bookingOpt.isPresent()) {
            return convertBookingToDTO(bookingOpt.get());
        }

        throw new RuntimeException("Package not found with tracking number: " + trackingNumber);
    }

    @Override
    public List<UnifiedPackageDTO> getPackagesByCustomerEmail(String email) {
        Set<String> processedTrackingNumbers = new HashSet<>();
        List<UnifiedPackageDTO> packages = new ArrayList<>();

        // Get shipments where customer is sender or recipient
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmailOrderByCreatedAtDesc(email);
        for (Shipment shipment : shipments) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
            if (shipment.getTrackingNumber() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                if (bookingOpt.isPresent()) {
                    mergeBookingData(dto, bookingOpt.get());
                }
            }
            packages.add(dto);
            processedTrackingNumbers.add(shipment.getTrackingNumber());
        }

        // Get bookings by email
        List<Booking> bookings = bookingRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
        for (Booking booking : bookings) {
            if (booking.getTrackingNumber() == null || !processedTrackingNumbers.contains(booking.getTrackingNumber())) {
                UnifiedPackageDTO dto = convertBookingToDTO(booking);
                packages.add(dto);
            }
        }

        return packages;
    }

    @Override
    public List<UnifiedPackageDTO> getPackagesByCustomerPhone(String phone) {
        List<UnifiedPackageDTO> packages = new ArrayList<>();

        // Get shipments by phone
        List<Shipment> shipments = shipmentRepository.findBySenderPhoneOrRecipientPhone(phone);
        for (Shipment shipment : shipments) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
            if (shipment.getTrackingNumber() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                if (bookingOpt.isPresent()) {
                    mergeBookingData(dto, bookingOpt.get());
                }
            }
            packages.add(dto);
        }

        // Get bookings by phone (if phone is stored)
        // Note: Booking doesn't have direct phone search, so we filter manually
        List<Booking> allBookings = bookingRepository.findAll();
        for (Booking booking : allBookings) {
            if (phone.equals(booking.getCustomerPhone()) || phone.equals(booking.getPickupContactPhone()) || 
                phone.equals(booking.getDeliveryContactPhone())) {
                if (!packages.stream().anyMatch(p -> p.getTrackingNumber() != null && 
                    p.getTrackingNumber().equals(booking.getTrackingNumber()))) {
                    UnifiedPackageDTO dto = convertBookingToDTO(booking);
                    packages.add(dto);
                }
            }
        }

        return packages;
    }

    @Override
    public List<UnifiedPackageDTO> getPackagesByDriverId(Long driverId) {
        List<UnifiedPackageDTO> packages = new ArrayList<>();
        Set<String> processedTrackingNumbers = new HashSet<>();

        // Get shipments assigned to driver
        List<Shipment> shipments = shipmentRepository.findByAssignedDriverId(driverId);
        for (Shipment shipment : shipments) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
            if (shipment.getTrackingNumber() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                if (bookingOpt.isPresent()) {
                    mergeBookingData(dto, bookingOpt.get());
                }
            }
            packages.add(dto);
            processedTrackingNumbers.add(shipment.getTrackingNumber());
        }

        // Get bookings assigned to driver
        User driver = userRepository.findById(driverId).orElse(null);
        if (driver != null) {
            List<Booking> bookings = bookingRepository.findByDriverOrderByCreatedAtDesc(driver);
            for (Booking booking : bookings) {
                if (booking.getTrackingNumber() == null || !processedTrackingNumbers.contains(booking.getTrackingNumber())) {
                    UnifiedPackageDTO dto = convertBookingToDTO(booking);
                    packages.add(dto);
                }
            }
        }

        return packages;
    }

    @Override
    public List<UnifiedPackageDTO> getPackagesByStatus(String status) {
        // Try to match as BookingStatus first
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            return getPackagesByBookingStatus(bookingStatus);
        } catch (IllegalArgumentException e) {
            // Try as ShipmentStatus
            try {
                ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
                return getPackagesByShipmentStatus(shipmentStatus);
            } catch (IllegalArgumentException ex) {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public List<UnifiedPackageDTO> getPackagesByBookingStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(status);
        return bookings.stream()
                .map(this::convertBookingToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UnifiedPackageDTO> getPackagesByShipmentStatus(ShipmentStatus status) {
        List<Shipment> shipments = shipmentRepository.findByStatus(status);
        return shipments.stream()
                .map(shipment -> {
                    UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
                    if (shipment.getTrackingNumber() != null) {
                        Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                        if (bookingOpt.isPresent()) {
                            mergeBookingData(dto, bookingOpt.get());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UnifiedPackageDTO> getPendingPackages() {
        List<UnifiedPackageDTO> packages = new ArrayList<>();

        // Get pending shipments
        List<Shipment> pendingShipments = shipmentRepository.findByStatus(ShipmentStatus.PENDING);
        for (Shipment shipment : pendingShipments) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
            if (shipment.getTrackingNumber() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                if (bookingOpt.isPresent()) {
                    mergeBookingData(dto, bookingOpt.get());
                }
            }
            packages.add(dto);
        }

        // Get pending bookings without shipments
        List<Booking> pendingBookings = bookingRepository.findByStatusOrderByCreatedAtDesc(BookingStatus.PENDING);
        for (Booking booking : pendingBookings) {
            if (booking.getTrackingNumber() == null || 
                !packages.stream().anyMatch(p -> p.getTrackingNumber() != null && 
                    p.getTrackingNumber().equals(booking.getTrackingNumber()))) {
                UnifiedPackageDTO dto = convertBookingToDTO(booking);
                packages.add(dto);
            }
        }

        return packages;
    }

    @Override
    public List<UnifiedPackageDTO> getInTransitPackages() {
        List<UnifiedPackageDTO> packages = new ArrayList<>();

        // Get in-transit shipments
        List<Shipment> inTransitShipments = shipmentRepository.findByStatus(ShipmentStatus.IN_TRANSIT);
        inTransitShipments.addAll(shipmentRepository.findByStatus(ShipmentStatus.OUT_FOR_DELIVERY));
        inTransitShipments.addAll(shipmentRepository.findByStatus(ShipmentStatus.PICKED_UP));

        for (Shipment shipment : inTransitShipments) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
            if (shipment.getTrackingNumber() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                if (bookingOpt.isPresent()) {
                    mergeBookingData(dto, bookingOpt.get());
                }
            }
            packages.add(dto);
        }

        // Get in-transit bookings
        List<Booking> inTransitBookings = bookingRepository.findByStatusOrderByCreatedAtDesc(BookingStatus.IN_TRANSIT);
        inTransitBookings.addAll(bookingRepository.findByStatusOrderByCreatedAtDesc(BookingStatus.OUT_FOR_DELIVERY));

        for (Booking booking : inTransitBookings) {
            if (booking.getTrackingNumber() == null || 
                !packages.stream().anyMatch(p -> p.getTrackingNumber() != null && 
                    p.getTrackingNumber().equals(booking.getTrackingNumber()))) {
                UnifiedPackageDTO dto = convertBookingToDTO(booking);
                packages.add(dto);
            }
        }

        return packages;
    }

    @Override
    public List<UnifiedPackageDTO> getDeliveredPackages() {
        List<UnifiedPackageDTO> packages = new ArrayList<>();

        // Get delivered shipments
        List<Shipment> deliveredShipments = shipmentRepository.findByStatus(ShipmentStatus.DELIVERED);
        for (Shipment shipment : deliveredShipments) {
            UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
            if (shipment.getTrackingNumber() != null) {
                Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                if (bookingOpt.isPresent()) {
                    mergeBookingData(dto, bookingOpt.get());
                }
            }
            packages.add(dto);
        }

        // Get delivered bookings
        List<Booking> deliveredBookings = bookingRepository.findByStatusOrderByCreatedAtDesc(BookingStatus.DELIVERED);
        for (Booking booking : deliveredBookings) {
            if (booking.getTrackingNumber() == null || 
                !packages.stream().anyMatch(p -> p.getTrackingNumber() != null && 
                    p.getTrackingNumber().equals(booking.getTrackingNumber()))) {
                UnifiedPackageDTO dto = convertBookingToDTO(booking);
                packages.add(dto);
            }
        }

        return packages;
    }

    @Override
    public UnifiedPackageDTO updatePackageStatus(String trackingNumber, String status) {
        UnifiedPackageDTO dto = getPackageByTrackingNumber(trackingNumber);

        // Update shipment if exists
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            try {
                ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
                shipment.setStatus(shipmentStatus);
                shipmentRepository.save(shipment);
                dto.setShipmentStatus(shipmentStatus);
            } catch (IllegalArgumentException e) {
                // Status doesn't match ShipmentStatus, skip
            }
        }

        // Update booking if exists
        Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                booking.setStatus(bookingStatus);
                bookingRepository.save(booking);
                dto.setBookingStatus(bookingStatus);
            } catch (IllegalArgumentException e) {
                // Status doesn't match BookingStatus, skip
            }
        }

        // Update unified status
        updateUnifiedStatus(dto);
        return dto;
    }

    @Override
    public UnifiedPackageDTO assignPackageToDriver(String trackingNumber, Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        UnifiedPackageDTO dto = getPackageByTrackingNumber(trackingNumber);

        // Assign to shipment if exists
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setAssignedDriver(driver);
            if (shipment.getStatus() == ShipmentStatus.PENDING) {
                shipment.setStatus(ShipmentStatus.ASSIGNED);
            }
            shipmentRepository.save(shipment);
            dto.setDriverId(driverId);
            dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());
            dto.setDriverPhone(driver.getPhone());
            dto.setDriverEmail(driver.getEmail());
            dto.setShipmentStatus(shipment.getStatus());
        }

        // Assign to booking if exists
        Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setDriver(driver);
            if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) {
                booking.setStatus(BookingStatus.ASSIGNED);
            }
            bookingRepository.save(booking);
            dto.setDriverId(driverId);
            dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());
            dto.setDriverPhone(driver.getPhone());
            dto.setDriverEmail(driver.getEmail());
            dto.setBookingStatus(booking.getStatus());
        }

        updateUnifiedStatus(dto);
        return dto;
    }

    @Override
    public UnifiedPackageDTO updatePackageDetails(String trackingNumber, Map<String, Object> updates) {
        UnifiedPackageDTO dto = getPackageByTrackingNumber(trackingNumber);
        
        // Update shipment if exists
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            
            // Only allow updates for PENDING shipments
            if (shipment.getStatus() != ShipmentStatus.PENDING) {
                throw new RuntimeException("Only PENDING packages can be modified");
            }
            
            // Update recipient info
            if (updates.containsKey("recipientName")) {
                shipment.setRecipientName(updates.get("recipientName").toString());
            }
            if (updates.containsKey("recipientEmail")) {
                shipment.setRecipientEmail(updates.get("recipientEmail").toString());
            }
            if (updates.containsKey("recipientPhone")) {
                shipment.setRecipientPhone(updates.get("recipientPhone").toString());
            }
            
            // Update pickup address
            if (updates.containsKey("pickupAddress")) {
                shipment.setPickupAddress(updates.get("pickupAddress").toString());
            }
            if (updates.containsKey("pickupCity")) {
                shipment.setPickupCity(updates.get("pickupCity").toString());
            }
            if (updates.containsKey("pickupState")) {
                shipment.setPickupState(updates.get("pickupState").toString());
            }
            if (updates.containsKey("pickupZipCode")) {
                shipment.setPickupZipCode(updates.get("pickupZipCode").toString());
            }
            
            // Update delivery address
            if (updates.containsKey("deliveryAddress")) {
                shipment.setDeliveryAddress(updates.get("deliveryAddress").toString());
            }
            if (updates.containsKey("deliveryCity")) {
                shipment.setDeliveryCity(updates.get("deliveryCity").toString());
            }
            if (updates.containsKey("deliveryState")) {
                shipment.setDeliveryState(updates.get("deliveryState").toString());
            }
            if (updates.containsKey("deliveryZipCode")) {
                shipment.setDeliveryZipCode(updates.get("deliveryZipCode").toString());
            }
            
            // Update package details
            if (updates.containsKey("weight")) {
                shipment.setWeight(Double.parseDouble(updates.get("weight").toString()));
            }
            if (updates.containsKey("dimensions")) {
                shipment.setDimensions(updates.get("dimensions").toString());
            }
            if (updates.containsKey("description")) {
                shipment.setDescription(updates.get("description").toString());
            }
            
            shipmentRepository.save(shipment);
            dto = convertShipmentToDTO(shipment);
        }
        
        // Update booking if exists
        Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Only allow updates for PENDING or PAYMENT_PENDING bookings
            if (booking.getStatus() != BookingStatus.PENDING && 
                booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
                throw new RuntimeException("Only PENDING packages can be modified");
            }
            
            // Update customer info
            if (updates.containsKey("customerName")) {
                booking.setCustomerName(updates.get("customerName").toString());
            }
            if (updates.containsKey("customerEmail")) {
                booking.setCustomerEmail(updates.get("customerEmail").toString());
            }
            if (updates.containsKey("customerPhone")) {
                booking.setCustomerPhone(updates.get("customerPhone").toString());
            }
            
            // Update pickup address
            if (updates.containsKey("pickupAddress")) {
                booking.setPickupAddress(updates.get("pickupAddress").toString());
            }
            if (updates.containsKey("pickupCity")) {
                booking.setPickupCity(updates.get("pickupCity").toString());
            }
            if (updates.containsKey("pickupState")) {
                booking.setPickupState(updates.get("pickupState").toString());
            }
            if (updates.containsKey("pickupPostalCode")) {
                booking.setPickupPostalCode(updates.get("pickupPostalCode").toString());
            }
            if (updates.containsKey("pickupContactName")) {
                booking.setPickupContactName(updates.get("pickupContactName").toString());
            }
            if (updates.containsKey("pickupContactPhone")) {
                booking.setPickupContactPhone(updates.get("pickupContactPhone").toString());
            }
            
            // Update delivery address
            if (updates.containsKey("deliveryAddress")) {
                booking.setDeliveryAddress(updates.get("deliveryAddress").toString());
            }
            if (updates.containsKey("deliveryCity")) {
                booking.setDeliveryCity(updates.get("deliveryCity").toString());
            }
            if (updates.containsKey("deliveryState")) {
                booking.setDeliveryState(updates.get("deliveryState").toString());
            }
            if (updates.containsKey("deliveryPostalCode")) {
                booking.setDeliveryPostalCode(updates.get("deliveryPostalCode").toString());
            }
            if (updates.containsKey("deliveryContactName")) {
                booking.setDeliveryContactName(updates.get("deliveryContactName").toString());
            }
            if (updates.containsKey("deliveryContactPhone")) {
                booking.setDeliveryContactPhone(updates.get("deliveryContactPhone").toString());
            }
            
            // Update package details
            if (updates.containsKey("weight")) {
                booking.setWeight(Double.parseDouble(updates.get("weight").toString()));
            }
            if (updates.containsKey("dimensions")) {
                booking.setDimensions(updates.get("dimensions").toString());
            }
            if (updates.containsKey("description")) {
                booking.setDescription(updates.get("description").toString());
            }
            
            bookingRepository.save(booking);
            dto = convertBookingToDTO(booking);
        }
        
        // Merge data if both exist
        if (shipmentOpt.isPresent() && bookingOpt.isPresent()) {
            mergeBookingData(dto, bookingOpt.get());
        }
        
        return dto;
    }

    @Override
    public UnifiedPackageDTO unassignPackageFromDriver(String trackingNumber) {
        UnifiedPackageDTO dto = getPackageByTrackingNumber(trackingNumber);

        // Unassign from shipment if exists
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setAssignedDriver(null);
            if (shipment.getStatus() == ShipmentStatus.ASSIGNED) {
                shipment.setStatus(ShipmentStatus.PENDING);
            }
            shipmentRepository.save(shipment);
            dto.setDriverId(null);
            dto.setDriverName(null);
            dto.setDriverPhone(null);
            dto.setDriverEmail(null);
            dto.setShipmentStatus(shipment.getStatus());
        }

        // Unassign from booking if exists
        Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setDriver(null);
            if (booking.getStatus() == BookingStatus.ASSIGNED) {
                booking.setStatus(BookingStatus.CONFIRMED);
            }
            bookingRepository.save(booking);
            dto.setDriverId(null);
            dto.setDriverName(null);
            dto.setDriverPhone(null);
            dto.setDriverEmail(null);
            dto.setBookingStatus(booking.getStatus());
        }

        updateUnifiedStatus(dto);
        return dto;
    }

    @Override
    public PackageStatistics getPackageStatistics() {
        PackageStatistics stats = new PackageStatistics();

        // Count shipments
        stats.setTotalPackages(shipmentRepository.count());
        stats.setPendingPackages(shipmentRepository.countByStatus(ShipmentStatus.PENDING));
        stats.setAssignedPackages(shipmentRepository.countByStatus(ShipmentStatus.ASSIGNED));
        stats.setInTransitPackages(
            shipmentRepository.countByStatus(ShipmentStatus.IN_TRANSIT) +
            shipmentRepository.countByStatus(ShipmentStatus.OUT_FOR_DELIVERY) +
            shipmentRepository.countByStatus(ShipmentStatus.PICKED_UP)
        );
        stats.setDeliveredPackages(shipmentRepository.countByStatus(ShipmentStatus.DELIVERED));
        stats.setCancelledPackages(shipmentRepository.countByStatus(ShipmentStatus.CANCELLED));

        // Add bookings without shipments
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        stats.setPendingPackages(stats.getPendingPackages() + pendingBookings);
        stats.setTotalPackages(stats.getTotalPackages() + pendingBookings);

        return stats;
    }

    @Override
    public List<UnifiedPackageDTO> searchPackages(String searchTerm) {
        List<UnifiedPackageDTO> packages = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();

        // Search shipments
        List<Shipment> allShipments = shipmentRepository.findAll();
        for (Shipment shipment : allShipments) {
            if (matchesSearch(shipment, lowerSearchTerm)) {
                UnifiedPackageDTO dto = convertShipmentToDTO(shipment);
                if (shipment.getTrackingNumber() != null) {
                    Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(shipment.getTrackingNumber());
                    if (bookingOpt.isPresent()) {
                        mergeBookingData(dto, bookingOpt.get());
                    }
                }
                packages.add(dto);
            }
        }

        // Search bookings
        List<Booking> allBookings = bookingRepository.findAll();
        for (Booking booking : allBookings) {
            if (matchesSearch(booking, lowerSearchTerm)) {
                if (booking.getTrackingNumber() == null || 
                    !packages.stream().anyMatch(p -> p.getTrackingNumber() != null && 
                        p.getTrackingNumber().equals(booking.getTrackingNumber()))) {
                    UnifiedPackageDTO dto = convertBookingToDTO(booking);
                    packages.add(dto);
                }
            }
        }

        return packages;
    }

    // Helper methods

    private UnifiedPackageDTO convertShipmentToDTO(Shipment shipment) {
        UnifiedPackageDTO dto = new UnifiedPackageDTO();
        
        dto.setId(shipment.getId());
        dto.setShipmentId(shipment.getId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        
        // Sender information
        if (shipment.getSender() != null) {
            dto.setSenderName(shipment.getSender().getFirstName() + " " + shipment.getSender().getLastName());
            dto.setSenderEmail(shipment.getSender().getEmail());
            dto.setSenderPhone(shipment.getSender().getPhone());
        }
        
        // Recipient information
        dto.setRecipientName(shipment.getRecipientName());
        dto.setRecipientEmail(shipment.getRecipientEmail());
        dto.setRecipientPhone(shipment.getRecipientPhone());
        
        // Pickup details
        dto.setPickupAddress(shipment.getPickupAddress());
        dto.setPickupCity(shipment.getPickupCity());
        dto.setPickupState(shipment.getPickupState());
        dto.setPickupZipCode(shipment.getPickupZipCode());
        dto.setPickupCountry(shipment.getPickupCountry());
        dto.setPickupLatitude(shipment.getPickupLatitude());
        dto.setPickupLongitude(shipment.getPickupLongitude());
        
        // Delivery details
        dto.setDeliveryAddress(shipment.getDeliveryAddress());
        dto.setDeliveryCity(shipment.getDeliveryCity());
        dto.setDeliveryState(shipment.getDeliveryState());
        dto.setDeliveryZipCode(shipment.getDeliveryZipCode());
        dto.setDeliveryCountry(shipment.getDeliveryCountry());
        dto.setDeliveryLatitude(shipment.getDeliveryLatitude());
        dto.setDeliveryLongitude(shipment.getDeliveryLongitude());
        
        // Package details
        dto.setWeight(shipment.getWeight());
        dto.setDimensions(shipment.getDimensions());
        dto.setDescription(shipment.getDescription());
        dto.setShippingCost(shipment.getShippingCost());
        dto.setServiceType(shipment.getServiceType() != null ? shipment.getServiceType().toString() : null);
        
        // Status
        dto.setShipmentStatus(shipment.getStatus());
        updateUnifiedStatus(dto);
        
        // Dates
        dto.setCreatedAt(shipment.getCreatedAt());
        dto.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(shipment.getActualDeliveryDate());
        formatDates(dto);
        
        // Driver information
        if (shipment.getAssignedDriver() != null) {
            User driver = shipment.getAssignedDriver();
            dto.setDriverId(driver.getId());
            dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());
            dto.setDriverPhone(driver.getPhone());
            dto.setDriverEmail(driver.getEmail());
        }
        
        // Verification codes
        dto.setCollectionCode(shipment.getCollectionCode());
        dto.setDropOffCode(shipment.getDropOffCode());
        
        // Tracking events
        List<ShipmentTracking> trackingEvents = shipmentTrackingRepository.findByShipmentOrderByCreatedAtDesc(shipment);
        dto.setTrackingEvents(trackingEvents.stream()
            .map(te -> new UnifiedPackageDTO.TrackingEvent(
                te.getStatus() != null ? te.getStatus().toString() : "",
                te.getLocation() != null ? te.getLocation() : "",
                te.getNotes() != null ? te.getNotes() : "",
                te.getCreatedAt() != null ? te.getCreatedAt() : new Date()
            ))
            .collect(Collectors.toList()));
        
        return dto;
    }

    private UnifiedPackageDTO convertBookingToDTO(Booking booking) {
        UnifiedPackageDTO dto = new UnifiedPackageDTO();
        
        dto.setId(booking.getId());
        dto.setBookingId(booking.getId());
        dto.setBookingNumber(booking.getBookingNumber());
        dto.setTrackingNumber(booking.getTrackingNumber());
        
        // Customer information
        dto.setCustomerName(booking.getCustomerName());
        dto.setCustomerEmail(booking.getCustomerEmail());
        dto.setCustomerPhone(booking.getCustomerPhone());
        dto.setSenderName(booking.getCustomerName());
        dto.setSenderEmail(booking.getCustomerEmail());
        dto.setSenderPhone(booking.getCustomerPhone());
        
        // Recipient information
        dto.setRecipientName(booking.getDeliveryContactName());
        dto.setRecipientEmail(booking.getCustomerEmail());
        dto.setRecipientPhone(booking.getDeliveryContactPhone());
        
        // Pickup details
        dto.setPickupAddress(booking.getPickupAddress());
        dto.setPickupCity(booking.getPickupCity());
        dto.setPickupState(booking.getPickupState());
        dto.setPickupPostalCode(booking.getPickupPostalCode());
        dto.setPickupContactName(booking.getPickupContactName());
        dto.setPickupContactPhone(booking.getPickupContactPhone());
        dto.setPickupLatitude(booking.getPickupLatitude());
        dto.setPickupLongitude(booking.getPickupLongitude());
        
        // Delivery details
        dto.setDeliveryAddress(booking.getDeliveryAddress());
        dto.setDeliveryCity(booking.getDeliveryCity());
        dto.setDeliveryState(booking.getDeliveryState());
        dto.setDeliveryPostalCode(booking.getDeliveryPostalCode());
        dto.setDeliveryContactName(booking.getDeliveryContactName());
        dto.setDeliveryContactPhone(booking.getDeliveryContactPhone());
        dto.setDeliveryLatitude(booking.getDeliveryLatitude());
        dto.setDeliveryLongitude(booking.getDeliveryLongitude());
        
        // Package details
        dto.setWeight(booking.getWeight());
        dto.setDimensions(booking.getDimensions());
        dto.setDescription(booking.getDescription());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setServiceType(booking.getServiceType() != null ? booking.getServiceType().toString() : null);
        
        // Status
        dto.setBookingStatus(booking.getStatus());
        updateUnifiedStatus(dto);
        
        // Dates
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setEstimatedDeliveryDate(booking.getEstimatedDeliveryDate());
        dto.setPickupDate(booking.getPickupDate());
        dto.setDeliveryDate(booking.getDeliveryDate());
        formatDates(dto);
        
        // Driver information
        if (booking.getDriver() != null) {
            User driver = booking.getDriver();
            dto.setDriverId(driver.getId());
            dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());
            dto.setDriverPhone(driver.getPhone());
            dto.setDriverEmail(driver.getEmail());
        }
        
        // Verification codes
        dto.setCustomerPickupCode(booking.getCustomerPickupCode());
        dto.setCustomerDeliveryCode(booking.getCustomerDeliveryCode());
        
        // Payment information
        if (booking.getPaymentStatus() != null) {
            dto.setPaymentStatus(booking.getPaymentStatus().toString());
        }
        dto.setPaymentReference(booking.getPaymentReference());
        dto.setPaymentDate(booking.getPaymentDate());
        
        // Special instructions
        dto.setSpecialInstructions(booking.getSpecialInstructions());
        
        return dto;
    }

    private void mergeBookingData(UnifiedPackageDTO dto, Booking booking) {
        // Merge booking data into existing DTO (from shipment)
        if (dto.getBookingId() == null) {
            dto.setBookingId(booking.getId());
        }
        if (dto.getBookingNumber() == null) {
            dto.setBookingNumber(booking.getBookingNumber());
        }
        
        // Merge customer information if missing
        if (dto.getCustomerName() == null) {
            dto.setCustomerName(booking.getCustomerName());
        }
        if (dto.getCustomerEmail() == null) {
            dto.setCustomerEmail(booking.getCustomerEmail());
        }
        if (dto.getCustomerPhone() == null) {
            dto.setCustomerPhone(booking.getCustomerPhone());
        }
        
        // Merge pickup contact info
        if (dto.getPickupContactName() == null) {
            dto.setPickupContactName(booking.getPickupContactName());
        }
        if (dto.getPickupContactPhone() == null) {
            dto.setPickupContactPhone(booking.getPickupContactPhone());
        }
        
        // Merge delivery contact info
        if (dto.getDeliveryContactName() == null) {
            dto.setDeliveryContactName(booking.getDeliveryContactName());
        }
        if (dto.getDeliveryContactPhone() == null) {
            dto.setDeliveryContactPhone(booking.getDeliveryContactPhone());
        }
        
        // Merge coordinates
        if (dto.getPickupLatitude() == null) {
            dto.setPickupLatitude(booking.getPickupLatitude());
        }
        if (dto.getPickupLongitude() == null) {
            dto.setPickupLongitude(booking.getPickupLongitude());
        }
        if (dto.getDeliveryLatitude() == null) {
            dto.setDeliveryLatitude(booking.getDeliveryLatitude());
        }
        if (dto.getDeliveryLongitude() == null) {
            dto.setDeliveryLongitude(booking.getDeliveryLongitude());
        }
        
        // Merge payment info
        if (dto.getPaymentStatus() == null && booking.getPaymentStatus() != null) {
            dto.setPaymentStatus(booking.getPaymentStatus().toString());
        }
        if (dto.getPaymentReference() == null) {
            dto.setPaymentReference(booking.getPaymentReference());
        }
        if (dto.getPaymentDate() == null) {
            dto.setPaymentDate(booking.getPaymentDate());
        }
        
        // Merge verification codes
        if (dto.getCustomerPickupCode() == null) {
            dto.setCustomerPickupCode(booking.getCustomerPickupCode());
        }
        if (dto.getCustomerDeliveryCode() == null) {
            dto.setCustomerDeliveryCode(booking.getCustomerDeliveryCode());
        }
        
        // Merge special instructions
        if (dto.getSpecialInstructions() == null) {
            dto.setSpecialInstructions(booking.getSpecialInstructions());
        }
        
        // Update booking status
        dto.setBookingStatus(booking.getStatus());
        updateUnifiedStatus(dto);
    }

    private void updateUnifiedStatus(UnifiedPackageDTO dto) {
        // Determine unified status from BookingStatus and ShipmentStatus
        String unifiedStatus = "UNKNOWN";
        String formattedStatus = "Unknown";
        
        // Prefer ShipmentStatus if available (more detailed)
        if (dto.getShipmentStatus() != null) {
            unifiedStatus = dto.getShipmentStatus().toString();
            formattedStatus = formatShipmentStatus(dto.getShipmentStatus());
            dto.setPending(dto.getShipmentStatus() == ShipmentStatus.PENDING);
            dto.setAssigned(dto.getShipmentStatus() == ShipmentStatus.ASSIGNED);
            dto.setPickedUp(dto.getShipmentStatus() == ShipmentStatus.PICKED_UP);
            dto.setInTransit(dto.getShipmentStatus() == ShipmentStatus.IN_TRANSIT || 
                           dto.getShipmentStatus() == ShipmentStatus.OUT_FOR_DELIVERY);
            dto.setDelivered(dto.getShipmentStatus() == ShipmentStatus.DELIVERED);
        } else if (dto.getBookingStatus() != null) {
            unifiedStatus = dto.getBookingStatus().toString();
            formattedStatus = formatBookingStatus(dto.getBookingStatus());
            dto.setPending(dto.getBookingStatus() == BookingStatus.PENDING || 
                         dto.getBookingStatus() == BookingStatus.PAYMENT_PENDING);
            dto.setAssigned(dto.getBookingStatus() == BookingStatus.ASSIGNED);
            dto.setInTransit(dto.getBookingStatus() == BookingStatus.IN_TRANSIT || 
                           dto.getBookingStatus() == BookingStatus.OUT_FOR_DELIVERY);
            dto.setDelivered(dto.getBookingStatus() == BookingStatus.DELIVERED);
        }
        
        dto.setUnifiedStatus(unifiedStatus);
        dto.setFormattedStatus(formattedStatus);
    }

    private String formatShipmentStatus(ShipmentStatus status) {
        if (status == null) return "Unknown";
        switch (status) {
            case PENDING: return "Pending";
            case ASSIGNED: return "Assigned";
            case PICKED_UP: return "Picked Up";
            case IN_TRANSIT: return "In Transit";
            case OUT_FOR_DELIVERY: return "Out for Delivery";
            case DELIVERED: return "Delivered";
            case CANCELLED: return "Cancelled";
            case FAILED_DELIVERY: return "Failed Delivery";
            default: return status.toString();
        }
    }

    private String formatBookingStatus(BookingStatus status) {
        if (status == null) return "Unknown";
        switch (status) {
            case PENDING: return "Pending";
            case PAYMENT_PENDING: return "Payment Pending";
            case CONFIRMED: return "Confirmed";
            case ASSIGNED: return "Assigned";
            case IN_TRANSIT: return "In Transit";
            case OUT_FOR_DELIVERY: return "Out for Delivery";
            case DELIVERED: return "Delivered";
            case CANCELLED: return "Cancelled";
            default: return status.toString();
        }
    }

    private void formatDates(UnifiedPackageDTO dto) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a");
        
        if (dto.getCreatedAt() != null) {
            dto.setFormattedCreatedAt(dateTimeFormat.format(dto.getCreatedAt()));
        }
        if (dto.getEstimatedDeliveryDate() != null) {
            dto.setFormattedEstimatedDelivery(dateFormat.format(dto.getEstimatedDeliveryDate()));
        }
        if (dto.getActualDeliveryDate() != null) {
            dto.setFormattedActualDelivery(dateFormat.format(dto.getActualDeliveryDate()));
        }
    }

    private boolean matchesSearch(Shipment shipment, String searchTerm) {
        return (shipment.getTrackingNumber() != null && shipment.getTrackingNumber().toLowerCase().contains(searchTerm)) ||
               (shipment.getRecipientName() != null && shipment.getRecipientName().toLowerCase().contains(searchTerm)) ||
               (shipment.getRecipientEmail() != null && shipment.getRecipientEmail().toLowerCase().contains(searchTerm)) ||
               (shipment.getRecipientPhone() != null && shipment.getRecipientPhone().contains(searchTerm)) ||
               (shipment.getPickupAddress() != null && shipment.getPickupAddress().toLowerCase().contains(searchTerm)) ||
               (shipment.getDeliveryAddress() != null && shipment.getDeliveryAddress().toLowerCase().contains(searchTerm));
    }

    private boolean matchesSearch(Booking booking, String searchTerm) {
        return (booking.getBookingNumber() != null && booking.getBookingNumber().toLowerCase().contains(searchTerm)) ||
               (booking.getTrackingNumber() != null && booking.getTrackingNumber().toLowerCase().contains(searchTerm)) ||
               (booking.getCustomerName() != null && booking.getCustomerName().toLowerCase().contains(searchTerm)) ||
               (booking.getCustomerEmail() != null && booking.getCustomerEmail().toLowerCase().contains(searchTerm)) ||
               (booking.getCustomerPhone() != null && booking.getCustomerPhone().contains(searchTerm)) ||
               (booking.getPickupAddress() != null && booking.getPickupAddress().toLowerCase().contains(searchTerm)) ||
               (booking.getDeliveryAddress() != null && booking.getDeliveryAddress().toLowerCase().contains(searchTerm));
    }
}

