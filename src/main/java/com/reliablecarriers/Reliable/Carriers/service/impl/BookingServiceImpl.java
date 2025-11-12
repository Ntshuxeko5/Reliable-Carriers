package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.BookingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.BookingResponse;
import com.reliablecarriers.Reliable.Carriers.dto.AddressCoordinates;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import com.reliablecarriers.Reliable.Carriers.service.BookingService;
import com.reliablecarriers.Reliable.Carriers.service.PricingService;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentService;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.GoogleMapsGeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Random;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PricingService pricingService;
    private final ShipmentService shipmentService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final GoogleMapsGeocodingService geocodingService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, 
                             PricingService pricingService,
                             ShipmentService shipmentService,
                             NotificationService notificationService,
                             EmailService emailService,
                             GoogleMapsGeocodingService geocodingService) {
        this.bookingRepository = bookingRepository;
        this.pricingService = pricingService;
        this.shipmentService = shipmentService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.geocodingService = geocodingService;
    }

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        try {
            // Calculate pricing
            Map<String, Object> priceDetails = calculatePrice(request);
            
            // Create booking entity
            Booking booking = new Booking();
            booking.setServiceType(request.getServiceType());
            booking.setStatus(BookingStatus.PENDING);
            booking.setCustomerName(request.getCustomerName());
            booking.setCustomerEmail(request.getCustomerEmail());
            booking.setCustomerPhone(request.getCustomerPhone());
            
            // Pickup details
            booking.setPickupAddress(request.getPickupAddress());
            booking.setPickupCity(request.getPickupCity());
            booking.setPickupState(request.getPickupState());
            booking.setPickupPostalCode(request.getPickupPostalCode());
            booking.setPickupContactName(request.getPickupContactName());
            booking.setPickupContactPhone(request.getPickupContactPhone());
            
            // Delivery details
            booking.setDeliveryAddress(request.getDeliveryAddress());
            booking.setDeliveryCity(request.getDeliveryCity());
            booking.setDeliveryState(request.getDeliveryState());
            booking.setDeliveryPostalCode(request.getDeliveryPostalCode());
            booking.setDeliveryContactName(request.getDeliveryContactName());
            booking.setDeliveryContactPhone(request.getDeliveryContactPhone());
            
            // Get Google Maps coordinates for addresses
            try {
                String fullPickupAddress = request.getPickupAddress() + ", " + request.getPickupCity() + 
                    ", " + request.getPickupState() + " " + request.getPickupPostalCode();
                AddressCoordinates pickupCoords = geocodingService.validateAndNormalizeAddress(fullPickupAddress);
                if (pickupCoords != null) {
                    booking.setPickupLatitude(pickupCoords.getLatitude());
                    booking.setPickupLongitude(pickupCoords.getLongitude());
                    booking.setPickupAddress(pickupCoords.getFormattedAddress());
                }
                
                String fullDeliveryAddress = request.getDeliveryAddress() + ", " + request.getDeliveryCity() + 
                    ", " + request.getDeliveryState() + " " + request.getDeliveryPostalCode();
                AddressCoordinates deliveryCoords = geocodingService.validateAndNormalizeAddress(fullDeliveryAddress);
                if (deliveryCoords != null) {
                    booking.setDeliveryLatitude(deliveryCoords.getLatitude());
                    booking.setDeliveryLongitude(deliveryCoords.getLongitude());
                    booking.setDeliveryAddress(deliveryCoords.getFormattedAddress());
                }
            } catch (Exception e) {
                // Log error but continue - coordinates are optional
                System.err.println("Failed to geocode addresses: " + e.getMessage());
            }
            
            // Package details
            booking.setWeight(request.getWeight());
            booking.setDimensions(request.getDimensions());
            booking.setDescription(request.getDescription());
            booking.setPickupDate(request.getPickupDate());
            booking.setDeliveryDate(request.getDeliveryDate());
            
            // Additional services
            booking.setInsurance(request.isInsurance());
            booking.setPacking(request.isPacking());
            booking.setSaturdayDelivery(request.isSaturdayDelivery());
            booking.setSignatureRequired(request.isSignatureRequired());
            booking.setSpecialInstructions(request.getSpecialInstructions());
            
            // Set pricing
            booking.setBasePrice((BigDecimal) priceDetails.get("basePrice"));
            booking.setServiceFee((BigDecimal) priceDetails.get("serviceFee"));
            booking.setInsuranceFee((BigDecimal) priceDetails.get("insuranceFee"));
            booking.setFuelSurcharge((BigDecimal) priceDetails.get("fuelSurcharge"));
            booking.setTotalAmount((BigDecimal) priceDetails.get("totalAmount"));
            
            // Set estimated delivery date
            booking.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(request.getServiceType()));
            
            // Save booking
            Booking savedBooking = bookingRepository.save(booking);
            
            // Convert to response
            return convertToResponse(savedBooking);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        return booking.map(this::convertToResponse).orElse(null);
    }

    @Override
    public Booking getBookingEntityById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    @Override
    public List<BookingResponse> getBookingsByEmail(String email) {
        List<Booking> bookings = bookingRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        
        // Only allow updates if booking is still pending or payment pending
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new RuntimeException("Cannot update booking that is already confirmed");
        }
        
        // Update fields
        booking.setServiceType(request.getServiceType());
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        
        // Update addresses
        booking.setPickupAddress(request.getPickupAddress());
        booking.setPickupCity(request.getPickupCity());
        booking.setPickupState(request.getPickupState());
        booking.setPickupPostalCode(request.getPickupPostalCode());
        booking.setPickupContactName(request.getPickupContactName());
        booking.setPickupContactPhone(request.getPickupContactPhone());
        
        booking.setDeliveryAddress(request.getDeliveryAddress());
        booking.setDeliveryCity(request.getDeliveryCity());
        booking.setDeliveryState(request.getDeliveryState());
        booking.setDeliveryPostalCode(request.getDeliveryPostalCode());
        booking.setDeliveryContactName(request.getDeliveryContactName());
        booking.setDeliveryContactPhone(request.getDeliveryContactPhone());
        
        // Update package details
        booking.setWeight(request.getWeight());
        booking.setDimensions(request.getDimensions());
        booking.setDescription(request.getDescription());
        booking.setPickupDate(request.getPickupDate());
        booking.setDeliveryDate(request.getDeliveryDate());
        
        // Update additional services
        booking.setInsurance(request.isInsurance());
        booking.setPacking(request.isPacking());
        booking.setSaturdayDelivery(request.isSaturdayDelivery());
        booking.setSignatureRequired(request.isSignatureRequired());
        booking.setSpecialInstructions(request.getSpecialInstructions());
        
        // Recalculate pricing
        Map<String, Object> priceDetails = calculatePrice(request);
        booking.setBasePrice((BigDecimal) priceDetails.get("basePrice"));
        booking.setServiceFee((BigDecimal) priceDetails.get("serviceFee"));
        booking.setInsuranceFee((BigDecimal) priceDetails.get("insuranceFee"));
        booking.setFuelSurcharge((BigDecimal) priceDetails.get("fuelSurcharge"));
        booking.setTotalAmount((BigDecimal) priceDetails.get("totalAmount"));
        
        // Save updated booking
        Booking savedBooking = bookingRepository.save(booking);
        
        return convertToResponse(savedBooking);
    }

    @Override
    public boolean cancelBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }
        
        Booking booking = bookingOpt.get();
        BookingStatus oldStatus = booking.getStatus();
        
        // Check if booking can be cancelled
        if (!canCancelBooking(bookingId)) {
            return false;
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send cancellation notification
        notificationService.sendBookingCancellationNotification(savedBooking);
        
        // Send status update notification
        if (oldStatus != BookingStatus.CANCELLED) {
            notificationService.sendBookingStatusUpdateNotification(
                savedBooking,
                oldStatus != null ? oldStatus.toString() : "UNKNOWN",
                BookingStatus.CANCELLED.toString()
            );
        }
        
        return true;
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId, String paymentReference) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        BookingStatus oldStatus = booking.getStatus();
        
        // Update booking status and payment details
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentReference(paymentReference);
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setPaymentDate(new Date());
        
        // Generate customer verification codes
        String customerPickupCode = generateCustomerCode();
        String customerDeliveryCode = generateCustomerCode();
        booking.setCustomerPickupCode(customerPickupCode);
        booking.setCustomerDeliveryCode(customerDeliveryCode);
        
        // Create shipment from booking
        Shipment shipment = createShipmentFromBooking(booking);
        booking.setShipmentId(shipment.getId().toString());
        booking.setTrackingNumber(shipment.getTrackingNumber());
        
        // Save updated booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send confirmation notification
        notificationService.sendBookingConfirmationNotification(savedBooking);
        
        // Send status update notification if status changed
        if (oldStatus != BookingStatus.CONFIRMED) {
            notificationService.sendBookingStatusUpdateNotification(
                savedBooking,
                oldStatus != null ? oldStatus.toString() : "UNKNOWN",
                BookingStatus.CONFIRMED.toString()
            );
        }
        
        // Send detailed email confirmation with codes
        sendBookingConfirmationEmail(savedBooking);
        
        return convertToResponse(savedBooking);
    }

    @Override
    public void updateBookingStatus(Long bookingId, BookingStatus status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            BookingStatus oldStatus = booking.getStatus();
            booking.setStatus(status);
            Booking savedBooking = bookingRepository.save(booking);
            
            // Send notification for status change
            if (oldStatus != status) {
                notificationService.sendBookingStatusUpdateNotification(
                    savedBooking, 
                    oldStatus != null ? oldStatus.toString() : "UNKNOWN",
                    status.toString()
                );
            }
        }
    }

    @Override
    public BookingStatus getBookingStatus(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        return bookingOpt.map(Booking::getStatus).orElse(null);
    }

    @Override
    public void updateBookingPaymentReference(Long bookingId, String paymentReference) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setPaymentReference(paymentReference);
            booking.setStatus(BookingStatus.PAYMENT_PENDING);
            bookingRepository.save(booking);
        }
    }

    @Override
    public Map<String, Object> calculatePrice(BookingRequest request) {
        Map<String, Object> priceDetails = new HashMap<>();
        
        try {
            BigDecimal basePrice = BigDecimal.ZERO;
            BigDecimal serviceFee = BigDecimal.ZERO;
            BigDecimal insuranceFee = BigDecimal.ZERO;
            BigDecimal fuelSurcharge = BigDecimal.ZERO;
            
            // Use quote base price if provided, otherwise calculate from service type
            if (request.getBasePrice() != null && request.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
                // Use the quote price as base price
                basePrice = request.getBasePrice();
            } else {
                // Calculate base price based on service type
                if (request.getServiceType().isCourierService()) {
                    basePrice = pricingService.calculateCourierPrice(request.getServiceType());
                } else {
                    // For moving services, calculate distance-based pricing
                    Double distance = estimateDistance(request);
                    basePrice = pricingService.calculateMovingServicePrice(request.getServiceType(), distance);
                }
            }
            
            // Calculate service fee (5% of base price)
            serviceFee = basePrice.multiply(new BigDecimal("0.05"));
            
            // Calculate insurance fee - use provided insuranceCost if available, otherwise calculate
            if (request.isInsurance()) {
                if (request.getInsuranceFee() != null && request.getInsuranceFee().compareTo(BigDecimal.ZERO) > 0) {
                    // Use provided insurance fee from frontend
                    insuranceFee = request.getInsuranceFee();
                } else {
                    // Calculate insurance fee (2% of base price as fallback)
                    insuranceFee = basePrice.multiply(new BigDecimal("0.02"));
                }
            }
            
            // Calculate fuel surcharge (3% of base price)
            fuelSurcharge = basePrice.multiply(new BigDecimal("0.03"));
            
            // Calculate total amount
            BigDecimal totalAmount = basePrice.add(serviceFee).add(insuranceFee).add(fuelSurcharge);
            
            priceDetails.put("basePrice", basePrice);
            priceDetails.put("serviceFee", serviceFee);
            priceDetails.put("insuranceFee", insuranceFee);
            priceDetails.put("fuelSurcharge", fuelSurcharge);
            priceDetails.put("totalAmount", totalAmount);
            priceDetails.put("currency", "ZAR");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate price: " + e.getMessage(), e);
        }
        
        return priceDetails;
    }

    @Override
    public List<Map<String, Object>> getAvailableServices() {
        List<Map<String, Object>> services = new ArrayList<>();
        
        try {
            // Use PricingService to get accurate pricing
            BigDecimal sameDayPrice = pricingService.calculateCourierPrice(ServiceType.SAME_DAY);
            BigDecimal overnightPrice = pricingService.calculateCourierPrice(ServiceType.OVERNIGHT);
            BigDecimal economyPrice = pricingService.calculateCourierPrice(ServiceType.ECONOMY);
            BigDecimal movingPrice = pricingService.calculateMovingServicePrice(ServiceType.FURNITURE, 50.0); // Default 50km for moving
            
            // Courier services
            Map<String, Object> sameDay = new HashMap<>();
            sameDay.put("type", "SAME_DAY");
            sameDay.put("name", "Same Day Delivery");
            sameDay.put("price", sameDayPrice.toString());
            sameDay.put("description", "Priority same-day delivery within Gauteng");
            sameDay.put("available", true);
            sameDay.put("category", "COURIER");
            sameDay.put("estimatedTime", "1-2 business days");
            sameDay.put("recommended", false);
            services.add(sameDay);
            
            Map<String, Object> overnight = new HashMap<>();
            overnight.put("type", "OVERNIGHT");
            overnight.put("name", "Overnight Delivery");
            overnight.put("price", overnightPrice.toString());
            overnight.put("description", "Next-day delivery within Gauteng");
            overnight.put("available", true);
            overnight.put("category", "COURIER");
            overnight.put("estimatedTime", "3-5 business days");
            overnight.put("recommended", true);
            services.add(overnight);
            
            Map<String, Object> economy = new HashMap<>();
            economy.put("type", "ECONOMY");
            economy.put("name", "Economy Delivery");
            economy.put("price", economyPrice.toString());
            economy.put("description", "Most economical option");
            economy.put("available", true);
            economy.put("category", "COURIER");
            economy.put("estimatedTime", "5-7 business days");
            economy.put("recommended", false);
            services.add(economy);
            
            // Moving services
            Map<String, Object> moving = new HashMap<>();
            moving.put("type", "FURNITURE");
            moving.put("name", "Furniture Moving");
            moving.put("price", movingPrice.toString());
            moving.put("description", "Professional moving service with insurance");
            moving.put("available", true);
            moving.put("category", "MOVING");
            moving.put("estimatedTime", "3-5 business days");
            moving.put("recommended", false);
            services.add(moving);
            
        } catch (Exception e) {
            System.err.println("Error getting service options from PricingService, using fallback: " + e.getMessage());
            
            // Fallback to hardcoded pricing if PricingService fails
            Map<String, Object> sameDay = new HashMap<>();
            sameDay.put("type", "SAME_DAY");
            sameDay.put("name", "Same Day Delivery");
            sameDay.put("price", "140.00");
            sameDay.put("description", "Priority same-day delivery within Gauteng");
            sameDay.put("available", true);
            sameDay.put("category", "COURIER");
            sameDay.put("estimatedTime", "1-2 business days");
            sameDay.put("recommended", false);
            services.add(sameDay);
            
            Map<String, Object> overnight = new HashMap<>();
            overnight.put("type", "OVERNIGHT");
            overnight.put("name", "Overnight Delivery");
            overnight.put("price", "120.00");
            overnight.put("description", "Next-day delivery within Gauteng");
            overnight.put("available", true);
            overnight.put("category", "COURIER");
            overnight.put("estimatedTime", "3-5 business days");
            overnight.put("recommended", true);
            services.add(overnight);
            
            Map<String, Object> economy = new HashMap<>();
            economy.put("type", "ECONOMY");
            economy.put("name", "Economy Delivery");
            economy.put("price", "100.00");
            economy.put("description", "Cost-effective 2-3 business days delivery");
            economy.put("available", true);
            economy.put("category", "COURIER");
            economy.put("estimatedTime", "5-7 business days");
            economy.put("recommended", false);
            services.add(economy);
            
            Map<String, Object> urgent = new HashMap<>();
            urgent.put("type", "URGENT");
            urgent.put("name", "Urgent Delivery");
            urgent.put("price", "425.00");
            urgent.put("description", "Premium urgent delivery service");
            urgent.put("available", true);
            urgent.put("category", "COURIER");
            urgent.put("estimatedTime", "Same day");
            urgent.put("recommended", false);
            services.add(urgent);
            
            // Moving services
            Map<String, Object> furniture = new HashMap<>();
            furniture.put("type", "FURNITURE");
            furniture.put("name", "Furniture Moving");
            furniture.put("price", "550.00");
            furniture.put("description", "Professional furniture moving service");
            furniture.put("available", true);
            furniture.put("category", "MOVING");
            furniture.put("estimatedTime", "3-5 business days");
            furniture.put("recommended", false);
            services.add(furniture);
            
            Map<String, Object> moving = new HashMap<>();
            moving.put("type", "MOVING");
            moving.put("name", "Complete Moving");
            moving.put("price", "550.00");
            moving.put("description", "Complete home/office relocation");
            moving.put("available", true);
            moving.put("category", "MOVING");
            moving.put("estimatedTime", "3-5 business days");
            moving.put("recommended", false);
            services.add(moving);
            
            Map<String, Object> loadTransport = new HashMap<>();
            loadTransport.put("type", "LOAD_TRANSPORT");
            loadTransport.put("name", "Load Transport");
            loadTransport.put("price", "550.00");
            loadTransport.put("description", "Heavy load and equipment transport");
            loadTransport.put("available", true);
            loadTransport.put("category", "MOVING");
            loadTransport.put("estimatedTime", "3-5 business days");
            loadTransport.put("recommended", false);
            services.add(loadTransport);
            
            Map<String, Object> expressDelivery = new HashMap<>();
            expressDelivery.put("type", "EXPRESS_DELIVERY");
            expressDelivery.put("name", "Express Delivery");
            expressDelivery.put("price", "550.00");
            expressDelivery.put("description", "Fast delivery within 24 hours");
            expressDelivery.put("available", true);
            expressDelivery.put("category", "MOVING");
            expressDelivery.put("estimatedTime", "1-2 business days");
            expressDelivery.put("recommended", false);
            services.add(expressDelivery);
        }
        
        return services;
    }

    @Override
    public boolean canCancelBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }
        
        Booking booking = bookingOpt.get();
        BookingStatus status = booking.getStatus();
        
        // Can only cancel if booking is pending, payment pending, or payment failed
        return status == BookingStatus.PENDING || 
               status == BookingStatus.PAYMENT_PENDING || 
               status == BookingStatus.PAYMENT_FAILED;
    }

    @Override
    public Map<String, Object> getBookingStatistics(String email) {
        List<Booking> bookings = bookingRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", bookings.size());
        stats.put("pendingBookings", bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count());
        stats.put("confirmedBookings", bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count());
        stats.put("deliveredBookings", bookings.stream().filter(b -> b.getStatus() == BookingStatus.DELIVERED).count());
        stats.put("cancelledBookings", bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count());
        
        return stats;
    }

    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingNumber(booking.getBookingNumber());
        response.setServiceType(booking.getServiceType());
        response.setStatus(booking.getStatus());
        
        // Customer details
        response.setCustomerName(booking.getCustomerName());
        response.setCustomerEmail(booking.getCustomerEmail());
        response.setCustomerPhone(booking.getCustomerPhone());
        
        // Pickup details
        response.setPickupAddress(booking.getPickupAddress());
        response.setPickupCity(booking.getPickupCity());
        response.setPickupState(booking.getPickupState());
        response.setPickupPostalCode(booking.getPickupPostalCode());
        response.setPickupContactName(booking.getPickupContactName());
        response.setPickupContactPhone(booking.getPickupContactPhone());
        
        // Delivery details
        response.setDeliveryAddress(booking.getDeliveryAddress());
        response.setDeliveryCity(booking.getDeliveryCity());
        response.setDeliveryState(booking.getDeliveryState());
        response.setDeliveryPostalCode(booking.getDeliveryPostalCode());
        response.setDeliveryContactName(booking.getDeliveryContactName());
        response.setDeliveryContactPhone(booking.getDeliveryContactPhone());
        
        // Package details
        response.setWeight(booking.getWeight());
        response.setDimensions(booking.getDimensions());
        response.setDescription(booking.getDescription());
        response.setPickupDate(booking.getPickupDate());
        response.setDeliveryDate(booking.getDeliveryDate());
        response.setEstimatedDeliveryDate(booking.getEstimatedDeliveryDate());
        
        // Pricing
        response.setBasePrice(booking.getBasePrice());
        response.setServiceFee(booking.getServiceFee());
        response.setInsuranceFee(booking.getInsuranceFee());
        response.setFuelSurcharge(booking.getFuelSurcharge());
        response.setTotalAmount(booking.getTotalAmount());
        
        // Additional services
        response.setInsurance(booking.isInsurance());
        response.setPacking(booking.isPacking());
        response.setSaturdayDelivery(booking.isSaturdayDelivery());
        response.setSignatureRequired(booking.isSignatureRequired());
        response.setSpecialInstructions(booking.getSpecialInstructions());
        
        // Payment details
        response.setPaymentReference(booking.getPaymentReference());
        response.setPaymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().toString() : null);
        response.setPaymentDate(booking.getPaymentDate());
        
        // Tracking
        response.setTrackingNumber(booking.getTrackingNumber());
        response.setShipmentId(booking.getShipmentId());
        
        // Driver details
        if (booking.getDriver() != null) {
            response.setDriverId(booking.getDriver().getId());
            response.setDriverName(booking.getDriver().getFirstName() + " " + booking.getDriver().getLastName());
            response.setDriverPhone(booking.getDriver().getPhone());
        }
        
        // Timestamps
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        return response;
    }

    private Shipment createShipmentFromBooking(Booking booking) {
        // Create a shipment from the confirmed booking
        Shipment shipment = new Shipment();
        
        // Set basic details
        shipment.setRecipientName(booking.getDeliveryContactName());
        shipment.setRecipientEmail(booking.getCustomerEmail());
        shipment.setRecipientPhone(booking.getDeliveryContactPhone());
        
        // Set pickup details
        shipment.setPickupAddress(booking.getPickupAddress());
        shipment.setPickupCity(booking.getPickupCity());
        shipment.setPickupState(booking.getPickupState());
        shipment.setPickupZipCode(booking.getPickupPostalCode());
        shipment.setPickupCountry("South Africa");
        // Copy coordinates from booking if available
        shipment.setPickupLatitude(booking.getPickupLatitude());
        shipment.setPickupLongitude(booking.getPickupLongitude());
        
        // Set delivery details
        shipment.setDeliveryAddress(booking.getDeliveryAddress());
        shipment.setDeliveryCity(booking.getDeliveryCity());
        shipment.setDeliveryState(booking.getDeliveryState());
        shipment.setDeliveryZipCode(booking.getDeliveryPostalCode());
        shipment.setDeliveryCountry("South Africa");
        // Copy coordinates from booking if available
        shipment.setDeliveryLatitude(booking.getDeliveryLatitude());
        shipment.setDeliveryLongitude(booking.getDeliveryLongitude());
        
        // Set package details
        shipment.setWeight(booking.getWeight());
        shipment.setDimensions(booking.getDimensions());
        shipment.setDescription(booking.getDescription());
        shipment.setShippingCost(booking.getTotalAmount());
        shipment.setServiceType(booking.getServiceType());
        shipment.setStatus(ShipmentStatus.PENDING);
        
        // Set estimated delivery date
        shipment.setEstimatedDeliveryDate(booking.getEstimatedDeliveryDate());
        
        // Create and return the shipment
        return shipmentService.createShipment(shipment);
    }

    private Date calculateEstimatedDeliveryDate(ServiceType serviceType) {
        Calendar calendar = Calendar.getInstance();
        
        switch (serviceType) {
            case SAME_DAY:
                // Same day delivery
                break;
            case OVERNIGHT:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case ECONOMY:
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                break;
            case URGENT:
                // Same day for urgent
                break;
            case FURNITURE:
            case MOVING:
            case LOAD_TRANSPORT:
            case EXPRESS_DELIVERY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            default:
                calendar.add(Calendar.DAY_OF_MONTH, 2);
                break;
        }
        
        return calendar.getTime();
    }

    private Double estimateDistance(BookingRequest request) {
        // Simple distance estimation based on city differences
        // In a real implementation, you would use a mapping service like Google Maps API
        
        if (request.getPickupCity().equals(request.getDeliveryCity())) {
            return 15.0; // Same city, estimate 15km
        } else if (request.getPickupState().equals(request.getDeliveryState())) {
            return 50.0; // Same state, estimate 50km
        } else {
            return 200.0; // Different state, estimate 200km
        }
    }
    
    /**
     * Generate a random customer verification code
     */
    private String generateCustomerCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    /**
     * Send booking confirmation email with verification codes
     */
    private void sendBookingConfirmationEmail(Booking booking) {
        try {
            String estimatedDelivery = calculateEstimatedDeliveryText(booking.getServiceType());
            
            emailService.sendBookingConfirmationEmail(
                booking.getCustomerEmail(),
                booking.getCustomerName(),
                booking.getBookingNumber(),
                booking.getTrackingNumber(),
                booking.getServiceType().toString(),
                "R" + booking.getTotalAmount().toString(),
                estimatedDelivery,
                booking.getPickupAddress() + ", " + booking.getPickupCity() + ", " + booking.getPickupState(),
                booking.getDeliveryAddress() + ", " + booking.getDeliveryCity() + ", " + booking.getDeliveryState(),
                booking.getWeight() + " kg",
                booking.getDescription(),
                booking.getCustomerPickupCode(),
                booking.getCustomerDeliveryCode(),
                booking.getPickupContactName(),
                booking.getPickupContactPhone(),
                booking.getDeliveryContactName(),
                booking.getDeliveryContactPhone(),
                booking.getDimensions() != null ? booking.getDimensions() : "Not specified",
                booking.getSpecialInstructions() != null ? booking.getSpecialInstructions() : "None"
            );
        } catch (Exception e) {
            // Log error but don't fail the booking confirmation
            System.err.println("Failed to send booking confirmation email: " + e.getMessage());
        }
    }
    
    /**
     * Calculate estimated delivery text based on service type
     */
    private String calculateEstimatedDeliveryText(ServiceType serviceType) {
        switch (serviceType) {
            case SAME_DAY:
                return "Same day delivery";
            case OVERNIGHT:
                return "1-2 business days";
            case ECONOMY:
                return "2-3 business days";
            case URGENT:
                return "Same day delivery";
            case FURNITURE:
            case MOVING:
            case LOAD_TRANSPORT:
            case EXPRESS_DELIVERY:
                return "1-2 business days";
            default:
                return "2-3 business days";
        }
    }
}
