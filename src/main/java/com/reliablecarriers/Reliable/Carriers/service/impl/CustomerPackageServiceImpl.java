package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageResponse;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.repository.QuoteRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentTrackingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.VehicleRepository;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.GoogleMapsService;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import com.reliablecarriers.Reliable.Carriers.service.PricingService;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CustomerPackageServiceImpl implements CustomerPackageService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final VehicleRepository vehicleRepository;
    private final QuoteRepository quoteRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final PricingService pricingService;
    private final GoogleMapsService googleMapsService;
    private final EmailService emailService;
    
    // In-memory storage for quote requests (for backward compatibility)
    private final Map<String, CustomerPackageRequest> quoteRequests = new ConcurrentHashMap<>();

    @Autowired
    public CustomerPackageServiceImpl(ShipmentRepository shipmentRepository,
                                    ShipmentTrackingRepository trackingRepository,
                                    VehicleRepository vehicleRepository,
                                    QuoteRepository quoteRepository,
                                    UserService userService,
                                    NotificationService notificationService,
                                    PricingService pricingService,
                                    EmailService emailService,
                                    @Autowired(required = false) GoogleMapsService googleMapsService) {
        this.shipmentRepository = shipmentRepository;
        this.trackingRepository = trackingRepository;
        this.vehicleRepository = vehicleRepository;
        this.quoteRepository = quoteRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.pricingService = pricingService;
        this.emailService = emailService;
        this.googleMapsService = googleMapsService;
    }

    @Override
    public QuoteResponse createQuote(CustomerPackageRequest request) {
        try {
            System.out.println("Creating quote for request: " + request.getSenderEmail());
            
            // Validate request
            if (request.getSenderEmail() == null || request.getSenderEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Sender email is required");
            }
            if (request.getPickupAddress() == null || request.getPickupAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("Pickup address is required");
            }
            if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("Delivery address is required");
            }
            if (request.getWeight() == null || request.getWeight() <= 0) {
                throw new IllegalArgumentException("Valid weight is required");
            }
            
            // Generate quote ID
            String quoteId = "Q" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            System.out.println("Generated quote ID: " + quoteId);
            
            // Calculate costs for different service types
            List<QuoteResponse.ServiceOption> availableServices = calculateServiceOptions(request);
            System.out.println("Calculated " + availableServices.size() + " service options");
            
            if (availableServices.isEmpty()) {
                throw new RuntimeException("No service options available");
            }
            
            // Get recommended service (usually the middle option)
            QuoteResponse.ServiceOption recommendedService = availableServices.stream()
                    .filter(QuoteResponse.ServiceOption::isRecommended)
                    .findFirst()
                    .orElse(availableServices.get(1)); // Default to second option if no recommended
            
            if (recommendedService == null) {
                recommendedService = availableServices.get(0); // Fallback to first option
            }
            
            // Create quote response
            QuoteResponse quote = new QuoteResponse();
            quote.setQuoteId(quoteId);
            quote.setTrackingNumber("TEMP_" + quoteId); // Temporary tracking number
            
            // Calculate all fees
            BigDecimal baseCost = calculateBaseCost(request);
            BigDecimal serviceFee = calculateServiceFee(request, ServiceType.valueOf(recommendedService.getServiceType()));
            BigDecimal insuranceFee = calculateInsuranceFee(request);
            BigDecimal fuelSurcharge = calculateFuelSurcharge(request);
            
            // Calculate total cost including insurance
            BigDecimal totalCost = baseCost.add(serviceFee).add(insuranceFee).add(fuelSurcharge);
            
            quote.setTotalCost(totalCost);
            quote.setBaseCost(baseCost);
            quote.setServiceFee(serviceFee);
            quote.setInsuranceFee(insuranceFee);
            quote.setFuelSurcharge(fuelSurcharge);
            quote.setServiceType(recommendedService.getServiceType());
            quote.setEstimatedDeliveryTime(recommendedService.getEstimatedDeliveryTime());
            quote.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(ServiceType.valueOf(recommendedService.getServiceType())));
            quote.setAvailableServices(availableServices);
            quote.setDistanceKm(estimateDistanceKm(request));
            quote.setPickupAddress(formatAddress(request.getPickupAddress(), request.getPickupCity(), request.getPickupState()));
            quote.setDeliveryAddress(formatAddress(request.getDeliveryAddress(), request.getDeliveryCity(), request.getDeliveryState()));
            quote.setWeight(request.getWeight());
            quote.setDimensions(request.getDimensions());
            quote.setDescription(request.getDescription());
            quote.setQuoteExpiryDate(calculateQuoteExpiryDate());
            quote.setActive(true);
            
            // Store quote in database
            Quote quoteEntity = new Quote();
            quoteEntity.setQuoteId(quoteId);
            quoteEntity.setCustomerEmail(request.getSenderEmail());
            quoteEntity.setPickupAddress(request.getPickupAddress());
            quoteEntity.setPickupCity(request.getPickupCity());
            quoteEntity.setPickupState(request.getPickupState());
            quoteEntity.setPickupZipCode(request.getPickupZipCode());
            quoteEntity.setPickupCountry(request.getPickupCountry());
            quoteEntity.setDeliveryAddress(request.getDeliveryAddress());
            quoteEntity.setDeliveryCity(request.getDeliveryCity());
            quoteEntity.setDeliveryState(request.getDeliveryState());
            quoteEntity.setDeliveryZipCode(request.getDeliveryZipCode());
            quoteEntity.setDeliveryCountry(request.getDeliveryCountry());
            quoteEntity.setWeight(request.getWeight());
            quoteEntity.setDimensions(request.getDimensions());
            quoteEntity.setDescription(request.getDescription());
            quoteEntity.setTotalCost(totalCost);
            quoteEntity.setBaseCost(baseCost);
            quoteEntity.setServiceFee(serviceFee);
            quoteEntity.setInsuranceFee(insuranceFee);
            quoteEntity.setFuelSurcharge(fuelSurcharge);
            quoteEntity.setServiceType(ServiceType.valueOf(recommendedService.getServiceType()));
            quoteEntity.setEstimatedDeliveryTime(recommendedService.getEstimatedDeliveryTime());
            quoteEntity.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(ServiceType.valueOf(recommendedService.getServiceType())));
            quoteEntity.setDistanceKm(estimateDistanceKm(request));
            quoteEntity.setExpiryDate(calculateQuoteExpiryDate());
            quoteEntity.setIsActive(true);
            
            // Save to database
            quoteRepository.save(quoteEntity);
            
            // Store request for backward compatibility
            quoteRequests.put(quoteId, request);
            
            System.out.println("Quote created and saved to database: " + quoteId);
            return quote;
            
        } catch (Exception e) {
            System.err.println("Error in createQuote: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create quote: " + e.getMessage(), e);
        }
    }

    @Override
    public Shipment createShipmentFromQuote(String quoteId, CustomerPackageRequest request) {
        // Retrieve quote from database
        Quote quoteEntity = quoteRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        
        if (!quoteEntity.getIsActive()) {
            throw new RuntimeException("Quote is no longer active");
        }
        
        // Check if quote has expired
        if (quoteEntity.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Quote has expired");
        }
        
        // Create or find sender user
        User sender = findOrCreateSenderUser(request);
        
        // Create shipment
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setSender(sender);
        shipment.setRecipientName(request.getRecipientName());
        shipment.setRecipientEmail(request.getRecipientEmail());
        shipment.setRecipientPhone(request.getRecipientPhone());
        shipment.setPickupAddress(request.getPickupAddress());
        shipment.setPickupCity(request.getPickupCity());
        shipment.setPickupState(request.getPickupState());
        shipment.setPickupZipCode(request.getPickupZipCode());
        shipment.setPickupCountry(request.getPickupCountry());
        shipment.setDeliveryAddress(request.getDeliveryAddress());
        shipment.setDeliveryCity(request.getDeliveryCity());
        shipment.setDeliveryState(request.getDeliveryState());
        shipment.setDeliveryZipCode(request.getDeliveryZipCode());
        shipment.setDeliveryCountry(request.getDeliveryCountry());
        shipment.setWeight(request.getWeight());
        shipment.setDimensions(request.getDimensions());
        shipment.setDescription(request.getDescription());
        shipment.setShippingCost(quoteEntity.getTotalCost());
        shipment.setServiceType(quoteEntity.getServiceType());
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setEstimatedDeliveryDate(quoteEntity.getEstimatedDeliveryDate());
        
        // Save shipment
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Create initial tracking entry
        createTrackingEntry(savedShipment, ShipmentStatus.PENDING, "Shipment created", "Shipment registered in the system");
        
        // Send notifications (optional - can be disabled for instant quotes)
        try {
            notificationService.sendShipmentCreatedNotification(savedShipment);
        } catch (Exception e) {
            // Log but don't fail the shipment creation if notification fails
            System.out.println("Notification failed for shipment " + savedShipment.getTrackingNumber() + ": " + e.getMessage());
        }
        
        // Mark quote as inactive in database
        quoteEntity.setIsActive(false);
        quoteRepository.save(quoteEntity);
        
        // Remove request from memory storage
        quoteRequests.remove(quoteId);
        
        return savedShipment;
    }

    @Override
    public CustomerPackageResponse getPackageByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        return convertToCustomerPackageResponse(shipment);
    }

    @Override
    public List<CustomerPackageResponse> getPackagesByEmail(String email) {
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(email);
        return shipments.stream()
                .map(this::convertToCustomerPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerPackageResponse> getPackagesByPhone(String phone) {
        List<Shipment> shipments = shipmentRepository.findBySenderPhoneOrRecipientPhone(phone);
        return shipments.stream()
                .map(this::convertToCustomerPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerPackageResponse> getPackagesByStatus(String email, String status) {
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(email);
        ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
        
        return shipments.stream()
                .filter(s -> s.getStatus() == shipmentStatus)
                .map(this::convertToCustomerPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerPackageResponse> getDeliveredPackages(String email) {
        return getPackagesByStatus(email, "DELIVERED");
    }

    @Override
    public List<CustomerPackageResponse> getCurrentPackages(String email) {
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(email);
        
        return shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT || 
                           s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY ||
                           s.getStatus() == ShipmentStatus.PICKED_UP)
                .map(this::convertToCustomerPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerPackageResponse> getPendingPackages(String email) {
        return getPackagesByStatus(email, "PENDING");
    }

    @Override
    public List<Shipment> createMultiplePackages(List<CustomerPackageRequest> requests, String businessName) {
        List<Shipment> createdShipments = new ArrayList<>();
        
        for (CustomerPackageRequest request : requests) {
            // Set business information
            request.setBusinessName(businessName);
            
            // Create quote and then shipment
            QuoteResponse quote = createQuote(request);
            Shipment shipment = createShipmentFromQuote(quote.getQuoteId(), request);
            createdShipments.add(shipment);
        }
        
        return createdShipments;
    }

    @Override
    public void updateTrackingPreferences(String trackingNumber, boolean emailNotifications, boolean smsNotifications) {
        // This would typically update a preferences table
        // For now, we'll just log the preference change
        System.out.println("Updated tracking preferences for " + trackingNumber + 
                          ": Email=" + emailNotifications + ", SMS=" + smsNotifications);
    }

    @Override
    public boolean cancelPackage(String trackingNumber, String email) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        // Verify ownership
        if (!shipment.getSender().getEmail().equals(email) && 
            !shipment.getRecipientEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to cancel this package");
        }
        
        // Only allow cancellation if package is still pending
        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            return false;
        }
        
        // Update status to cancelled
        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipmentRepository.save(shipment);
        
        // Create tracking entry
        createTrackingEntry(shipment, ShipmentStatus.CANCELLED, "Package cancelled", "Package cancelled by customer");
        
        // Send notification
        notificationService.sendShipmentCancelledNotification(shipment, "Cancelled by customer");
        
        return true;
    }

    @Override
    public void requestPickup(String trackingNumber, String email, String preferredDate, String notes) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        // Verify ownership
        if (!shipment.getSender().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to request pickup for this package");
        }
        
        // Create pickup request (this would typically be stored in a separate table)
        System.out.println("Pickup requested for " + trackingNumber + " on " + preferredDate + ": " + notes);
        
        // Send notification to admin/driver
        notificationService.sendCustomEmailNotification("admin@reliablecarriers.com", 
            "Pickup Request for " + trackingNumber, 
            "Pickup requested for " + trackingNumber + " on " + preferredDate + ": " + notes);
    }

    @Override
    public List<CustomerPackageResponse> getPackageHistory(String email, int limit) {
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmailOrderByCreatedAtDesc(email);
        
        return shipments.stream()
                .limit(limit)
                .map(this::convertToCustomerPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerPackageResponse> searchPackages(String email, String searchTerm) {
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(email);
        
        return shipments.stream()
                .filter(s -> s.getTrackingNumber().contains(searchTerm) ||
                           s.getRecipientName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                           s.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                .map(this::convertToCustomerPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PackageStatistics getPackageStatistics(String email) {
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(email);
        
        PackageStatistics stats = new PackageStatistics();
        stats.setTotalPackages(shipments.size());
        stats.setDeliveredPackages((int) shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.DELIVERED).count());
        stats.setInTransitPackages((int) shipments.stream().filter(s -> 
            s.getStatus() == ShipmentStatus.IN_TRANSIT || 
            s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY ||
            s.getStatus() == ShipmentStatus.PICKED_UP).count());
        stats.setPendingPackages((int) shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.PENDING).count());
        stats.setCancelledPackages((int) shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.CANCELLED).count());
        
        BigDecimal totalSpent = shipments.stream()
                .map(Shipment::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalSpent(totalSpent);
        
        // Calculate average delivery time
        long totalDeliveryTime = shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED && s.getActualDeliveryDate() != null)
                .mapToLong(s -> s.getActualDeliveryDate().getTime() - s.getCreatedAt().getTime())
                .sum();
        
        long deliveredCount = shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED && s.getActualDeliveryDate() != null)
                .count();
        
        if (deliveredCount > 0) {
            long avgTime = totalDeliveryTime / deliveredCount;
            stats.setAverageDeliveryTime(formatDeliveryTime(avgTime));
        } else {
            stats.setAverageDeliveryTime("N/A");
        }
        
        return stats;
    }

    @Override
    public boolean isValidTrackingNumber(String trackingNumber) {
        return trackingNumber != null && 
               trackingNumber.length() >= 8 && 
               trackingNumber.matches("^[A-Z0-9]+$");
    }

    @Override
    public String getEstimatedDeliveryDate(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        if (shipment.getEstimatedDeliveryDate() != null) {
            return new java.text.SimpleDateFormat("MMM dd, yyyy").format(shipment.getEstimatedDeliveryDate());
        }
        return "To be determined";
    }

    @Override
    public boolean isEligibleForPickup(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        return shipment.getStatus() == ShipmentStatus.PENDING;
    }

    @Override
    public List<InsuranceOption> getInsuranceOptions(String trackingNumber) {
        List<InsuranceOption> options = new ArrayList<>();
        options.add(new InsuranceOption("BASIC", "Basic coverage up to $100", new BigDecimal("5.00"), new BigDecimal("100.00")));
        options.add(new InsuranceOption("STANDARD", "Standard coverage up to $500", new BigDecimal("15.00"), new BigDecimal("500.00")));
        options.add(new InsuranceOption("PREMIUM", "Premium coverage up to $1000", new BigDecimal("25.00"), new BigDecimal("1000.00")));
        return options;
    }

    @Override
    public boolean addInsurance(String trackingNumber, String insuranceType, BigDecimal amount) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
        // Only allow insurance addition if package is still pending
        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            return false;
        }
        
        // Find insurance option
        InsuranceOption option = getInsuranceOptions(trackingNumber).stream()
                .filter(o -> o.getType().equals(insuranceType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid insurance type"));
        
        // Update shipping cost to include insurance
        shipment.setShippingCost(shipment.getShippingCost().add(option.getCost()));
        shipmentRepository.save(shipment);
        
        // Create tracking entry
        createTrackingEntry(shipment, shipment.getStatus(), "Insurance added", 
                          "Added " + insuranceType + " insurance for $" + option.getCost());
        
        return true;
    }

    // Helper methods
    private List<QuoteResponse.ServiceOption> calculateServiceOptions(CustomerPackageRequest request) {
        try {
            System.out.println("Calculating service options...");
            List<QuoteResponse.ServiceOption> options = new ArrayList<>();
            
            // Calculate distance for moving services
            double distanceKm = estimateDistanceKm(request);
            System.out.println("Estimated distance: " + distanceKm + " km");
            
            // Economy courier service (Gauteng only)
            BigDecimal economyCost = pricingService.calculateCourierPrice(ServiceType.ECONOMY);
            options.add(new QuoteResponse.ServiceOption(
                ServiceType.ECONOMY.toString(), 
                economyCost, 
                "2-3 business days", 
                "Most economical option for Gauteng", 
                false
            ));
            
            // Overnight courier service (Gauteng only)
            BigDecimal overnightCost = pricingService.calculateCourierPrice(ServiceType.OVERNIGHT);
            options.add(new QuoteResponse.ServiceOption(
                ServiceType.OVERNIGHT.toString(), 
                overnightCost, 
                "Next business day", 
                "Balanced speed and cost for Gauteng", 
                true
            ));
            
            // Same day courier service (Gauteng only)
            BigDecimal sameDayCost = pricingService.calculateCourierPrice(ServiceType.SAME_DAY);
            options.add(new QuoteResponse.ServiceOption(
                ServiceType.SAME_DAY.toString(), 
                sameDayCost, 
                "Same day delivery", 
                "Fastest delivery option for Gauteng", 
                false
            ));
            
            // Moving service (distance-based pricing)
            BigDecimal movingCost = pricingService.calculateMovingServicePrice(ServiceType.FURNITURE, distanceKm);
            options.add(new QuoteResponse.ServiceOption(
                ServiceType.FURNITURE.toString(), 
                movingCost, 
                "3-5 business days", 
                "Professional moving service with insurance (R550 for 20km, R25/km thereafter)", 
                false
            ));
            
            System.out.println("Service options calculated: " + options.size());
            return options;
            
        } catch (Exception e) {
            System.err.println("Error calculating service options: " + e.getMessage());
            e.printStackTrace();
            
            // Return default options if pricing service fails
            List<QuoteResponse.ServiceOption> fallbackOptions = new ArrayList<>();
            fallbackOptions.add(new QuoteResponse.ServiceOption(
                ServiceType.ECONOMY.toString(), 
                new BigDecimal("100.00"), 
                "5-7 business days", 
                "Most economical option", 
                false
            ));
            fallbackOptions.add(new QuoteResponse.ServiceOption(
                ServiceType.OVERNIGHT.toString(), 
                new BigDecimal("120.00"), 
                "3-5 business days", 
                "Balanced speed and cost", 
                true
            ));
            fallbackOptions.add(new QuoteResponse.ServiceOption(
                ServiceType.SAME_DAY.toString(), 
                new BigDecimal("140.00"), 
                "1-2 business days", 
                "Fastest delivery option", 
                false
            ));
            
            // Moving service fallback
            fallbackOptions.add(new QuoteResponse.ServiceOption(
                ServiceType.FURNITURE.toString(), 
                new BigDecimal("200.00"), 
                "3-5 business days", 
                "Professional moving service with insurance", 
                false
            ));
            
            System.out.println("Using fallback service options: " + fallbackOptions.size());
            return fallbackOptions;
        }
    }

    private BigDecimal calculateBaseCost(CustomerPackageRequest request) {
        // South Africa (Johannesburg/Gauteng) pricing logic: R550 up to 20km, then R25/km thereafter
        double distanceKm = estimateDistanceKm(request);
        BigDecimal base = new BigDecimal("550.00");
        if (distanceKm > 20) {
            double extra = distanceKm - 20.0;
            base = base.add(new BigDecimal("25.00").multiply(BigDecimal.valueOf(extra)));
        }
        // Add a small weight component (R5/kg) to reflect handling effort
        BigDecimal weightComponent = BigDecimal.valueOf(request.getWeight() == null ? 0.0 : request.getWeight() * 5.0);
        return base.add(weightComponent);
    }

    private BigDecimal calculateServiceFee(CustomerPackageRequest request, ServiceType serviceType) {
        // Service fee based on service type
        switch (serviceType) {
            case ECONOMY: return new BigDecimal("5.00");
            case OVERNIGHT: return new BigDecimal("10.00");
            case SAME_DAY: return new BigDecimal("20.00");
            default: return new BigDecimal("10.00");
        }
    }

    private BigDecimal calculateFuelSurcharge(CustomerPackageRequest request) {
        // Fuel surcharge 5% of base for quotes
        BigDecimal base = calculateBaseCost(request);
        return base.multiply(new BigDecimal("0.05")).setScale(2, java.math.RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateInsuranceFee(CustomerPackageRequest request) {
        // Calculate insurance fee based on request
        if (request.getInsuranceCost() != null) {
            return request.getInsuranceCost();
        }
        
        // Default insurance calculation based on insurance type
        String insuranceType = request.getInsuranceType();
        if (insuranceType == null || "none".equals(insuranceType)) {
            return BigDecimal.ZERO;
        }
        
        switch (insuranceType) {
            case "basic": return new BigDecimal("15.00");
            case "standard": return new BigDecimal("35.00");
            case "premium": return new BigDecimal("65.00");
            default: return BigDecimal.ZERO;
        }
    }

    @Override
    public List<Quote> getSavedQuotes(String email) {
        return quoteRepository.findByCustomerEmailAndIsActiveTrueOrderByCreatedAtDesc(email);
    }
    
    @Override
    public Quote saveQuoteFromPage(String customerEmail, Map<String, Object> quoteData) {
        try {
            // Generate unique quote ID
            String quoteId = "Q" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Extract quote data
            String pickupAddress = (String) quoteData.getOrDefault("pickupAddress", "");
            String deliveryAddress = (String) quoteData.getOrDefault("deliveryAddress", "");
            Double weight = quoteData.get("weight") != null ? Double.parseDouble(quoteData.get("weight").toString()) : 0.0;
            String dimensions = quoteData.get("length") != null && quoteData.get("width") != null && quoteData.get("height") != null
                ? quoteData.get("length") + "×" + quoteData.get("width") + "×" + quoteData.get("height") + " cm"
                : (String) quoteData.getOrDefault("dimensions", "");
            String serviceTypeStr = (String) quoteData.getOrDefault("serviceType", "SAME_DAY");
            Double price = quoteData.get("price") != null ? Double.parseDouble(quoteData.get("price").toString()) : 0.0;
            Double distance = quoteData.get("distance") != null ? Double.parseDouble(quoteData.get("distance").toString()) : 0.0;
            
            // Map service type string to enum
            ServiceType serviceType;
            try {
                serviceType = ServiceType.valueOf(serviceTypeStr);
            } catch (IllegalArgumentException e) {
                // Map common service names to enum values
                if (serviceTypeStr.equalsIgnoreCase("standard") || serviceTypeStr.equalsIgnoreCase("same-day")) {
                    serviceType = ServiceType.SAME_DAY;
                } else if (serviceTypeStr.equalsIgnoreCase("express") || serviceTypeStr.equalsIgnoreCase("overnight")) {
                    serviceType = ServiceType.OVERNIGHT;
                } else if (serviceTypeStr.equalsIgnoreCase("economy")) {
                    serviceType = ServiceType.ECONOMY;
                } else {
                    serviceType = ServiceType.SAME_DAY; // Default
                }
            }
            
            // Create and save quote entity
            Quote quote = new Quote();
            quote.setQuoteId(quoteId);
            quote.setCustomerEmail(customerEmail);
            quote.setPickupAddress(pickupAddress);
            quote.setDeliveryAddress(deliveryAddress);
            quote.setWeight(weight);
            quote.setDimensions(dimensions);
            quote.setTotalCost(BigDecimal.valueOf(price));
            quote.setServiceType(serviceType);
            quote.setDistanceKm(distance);
            quote.setIsActive(true);
            quote.setCreatedAt(new Date());
            
            // Calculate expiry date (30 days from now)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);
            quote.setExpiryDate(cal.getTime());
            
            // Set estimated delivery time based on service type
            String estimatedDeliveryTime = getEstimatedDeliveryTimeForService(serviceType);
            quote.setEstimatedDeliveryTime(estimatedDeliveryTime);
            
            // Calculate estimated delivery date
            Calendar deliveryCal = Calendar.getInstance();
            switch (serviceType) {
                case SAME_DAY:
                    deliveryCal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case OVERNIGHT:
                    deliveryCal.add(Calendar.DAY_OF_MONTH, 2);
                    break;
                case ECONOMY:
                    deliveryCal.add(Calendar.DAY_OF_MONTH, 4);
                    break;
                default:
                    deliveryCal.add(Calendar.DAY_OF_MONTH, 2);
            }
            quote.setEstimatedDeliveryDate(deliveryCal.getTime());
            
            // Save to database
            Quote savedQuote = quoteRepository.save(quote);
            quoteRepository.flush(); // Ensure it's immediately available
            
            System.out.println("Quote saved successfully: " + quoteId + " for customer: " + customerEmail);
            
            // Get customer name for email
            String customerName = "Customer";
            try {
                User customer = userService.getUserByEmail(customerEmail);
                if (customer != null && customer.getFirstName() != null) {
                    customerName = customer.getFirstName() + (customer.getLastName() != null ? " " + customer.getLastName() : "");
                }
            } catch (Exception e) {
                System.err.println("Could not fetch customer name for email: " + e.getMessage());
            }
            
            // Send confirmation email
            try {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy");
                emailService.sendQuoteSavedEmail(
                    customerEmail,
                    customerName,
                    quoteId,
                    serviceType.name(),
                    "R " + String.format("%.2f", price),
                    pickupAddress,
                    deliveryAddress,
                    estimatedDeliveryTime,
                    dateFormat.format(quote.getExpiryDate())
                );
                System.out.println("Quote saved email sent to: " + customerEmail);
            } catch (Exception e) {
                System.err.println("Failed to send quote saved email: " + e.getMessage());
                // Don't fail the save operation if email fails
            }
            
            return savedQuote;
        } catch (Exception e) {
            System.err.println("Error saving quote: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save quote: " + e.getMessage(), e);
        }
    }
    
    private String getEstimatedDeliveryTimeForService(ServiceType serviceType) {
        switch (serviceType) {
            case SAME_DAY:
                return "Same Day Delivery";
            case OVERNIGHT:
                return "1-2 Business Days";
            case ECONOMY:
                return "4-7 Business Days";
            default:
                return "2-3 Business Days";
        }
    }
    
    private double estimateDistanceKm(CustomerPackageRequest request) {
        // Try Google Maps API first if addresses are available and service is configured
        if (googleMapsService != null && request.getPickupAddress() != null && request.getDeliveryAddress() != null) {
            try {
                System.out.println("Attempting Google Maps API call for distance calculation...");
                System.out.println("From: " + request.getPickupAddress());
                System.out.println("To: " + request.getDeliveryAddress());
                
                GoogleMapsService.DistanceResult result = googleMapsService.calculateDistance(
                    request.getPickupAddress(), 
                    request.getDeliveryAddress()
                );
                if (result != null) {
                    System.out.println("Google Maps API returned distance: " + result.getDistanceKm() + " km");
                    return result.getDistanceKm();
                } else {
                    System.out.println("Google Maps API returned null result");
                }
            } catch (Exception e) {
                System.err.println("Google Maps API call failed, falling back to other methods: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Google Maps API not available or addresses missing");
        }
        
        // Haversine if coords present; otherwise approximate within Gauteng (Johannesburg-centric)
        if (request.getPickupLatitude() != null && request.getPickupLongitude() != null 
                && request.getDeliveryLatitude() != null && request.getDeliveryLongitude() != null) {
            return haversineKm(request.getPickupLatitude(), request.getPickupLongitude(),
                    request.getDeliveryLatitude(), request.getDeliveryLongitude());
        }
        // Fallback: heuristic distance based on different city names
        String fromCity = safeLower(request.getPickupCity());
        String toCity = safeLower(request.getDeliveryCity());
        
        System.out.println("Using fallback distance calculation for: " + fromCity + " to " + toCity);
        
        if (fromCity.equals(toCity)) {
            System.out.println("Same city, using 10km estimate");
            return 10.0; // intra-city avg
        }
        
        // Common Gauteng pairs with more accurate distances
        if ((fromCity.contains("johannesburg") && toCity.contains("pretoria")) || (fromCity.contains("pretoria") && toCity.contains("johannesburg"))) {
            System.out.println("Johannesburg-Pretoria route: 55km");
            return 55.0;
        }
        if ((fromCity.contains("sandton") && toCity.contains("midrand")) || (fromCity.contains("midrand") && toCity.contains("sandton"))) {
            System.out.println("Sandton-Midrand route: 20km");
            return 20.0;
        }
        if ((fromCity.contains("johannesburg") && toCity.contains("soweto")) || (fromCity.contains("soweto") && toCity.contains("johannesburg"))) {
            System.out.println("Johannesburg-Soweto route: 25km");
            return 25.0;
        }
        if ((fromCity.contains("johannesburg") && toCity.contains("sandton")) || (fromCity.contains("sandton") && toCity.contains("johannesburg"))) {
            System.out.println("Johannesburg-Sandton route: 15km");
            return 15.0;
        }
        if ((fromCity.contains("pretoria") && toCity.contains("midrand")) || (fromCity.contains("midrand") && toCity.contains("pretoria"))) {
            System.out.println("Pretoria-Midrand route: 35km");
            return 35.0;
        }
        
        // Default fallback based on province
        if (fromCity.contains("gauteng") || toCity.contains("gauteng")) {
            System.out.println("Gauteng province route: 30km");
            return 30.0;
        }
        
        System.out.println("Unknown route, using default 30km");
        return 30.0;
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private Date calculateEstimatedDeliveryDate(ServiceType serviceType) {
        Calendar cal = Calendar.getInstance();
        switch (serviceType) {
            case ECONOMY: cal.add(Calendar.DAY_OF_MONTH, 7); break;
            case OVERNIGHT: cal.add(Calendar.DAY_OF_MONTH, 5); break;
            case SAME_DAY: cal.add(Calendar.DAY_OF_MONTH, 2); break;
            default: cal.add(Calendar.DAY_OF_MONTH, 5); break;
        }
        return cal.getTime();
    }

    private Date calculateQuoteExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7); // Quote expires in 7 days
        return cal.getTime();
    }

    private String generateTrackingNumber() {
        String trackingNumber;
        do {
            trackingNumber = "RC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (shipmentRepository.findByTrackingNumber(trackingNumber).isPresent());
        return trackingNumber;
    }

    private User findOrCreateSenderUser(CustomerPackageRequest request) {
        // Try to find existing user by email
        try {
            return userService.getUserByEmail(request.getSenderEmail());
        } catch (Exception e) {
            // Create new user if not found
            User newUser = new User();
            newUser.setFirstName(request.getSenderName().split(" ")[0]);
            newUser.setLastName(request.getSenderName().split(" ").length > 1 ? 
                              request.getSenderName().split(" ")[1] : "");
            newUser.setEmail(request.getSenderEmail());
            newUser.setPhone(request.getSenderPhone());
            newUser.setRole(UserRole.CUSTOMER);
            newUser.setPassword(UUID.randomUUID().toString()); // Temporary password
            
            return userService.createUser(newUser);
        }
    }

    private void createTrackingEntry(Shipment shipment, ShipmentStatus status, String location, String notes) {
        ShipmentTracking tracking = new ShipmentTracking();
        tracking.setShipment(shipment);
        tracking.setStatus(status);
        tracking.setLocation(location);
        tracking.setNotes(notes);
        trackingRepository.save(tracking);
    }

    private CustomerPackageResponse convertToCustomerPackageResponse(Shipment shipment) {
        CustomerPackageResponse response = new CustomerPackageResponse();
        response.setId(shipment.getId());
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setSenderName(shipment.getSender().getFirstName() + " " + shipment.getSender().getLastName());
        response.setSenderEmail(shipment.getSender().getEmail());
        response.setSenderPhone(shipment.getSender().getPhone());
        response.setRecipientName(shipment.getRecipientName());
        response.setRecipientEmail(shipment.getRecipientEmail());
        response.setRecipientPhone(shipment.getRecipientPhone());
        response.setPickupAddress(shipment.getPickupAddress());
        response.setPickupCity(shipment.getPickupCity());
        response.setPickupState(shipment.getPickupState());
        response.setPickupZipCode(shipment.getPickupZipCode());
        response.setPickupCountry(shipment.getPickupCountry());
        response.setDeliveryAddress(shipment.getDeliveryAddress());
        response.setDeliveryCity(shipment.getDeliveryCity());
        response.setDeliveryState(shipment.getDeliveryState());
        response.setDeliveryZipCode(shipment.getDeliveryZipCode());
        response.setDeliveryCountry(shipment.getDeliveryCountry());
        response.setWeight(shipment.getWeight());
        response.setDimensions(shipment.getDimensions());
        response.setDescription(shipment.getDescription());
        response.setShippingCost(shipment.getShippingCost());
        response.setServiceType(shipment.getServiceType().toString());
        response.setStatus(shipment.getStatus());
        response.setFormattedStatus(formatStatus(shipment.getStatus()));
        response.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        response.setActualDeliveryDate(shipment.getActualDeliveryDate());
        response.setCreatedAt(shipment.getCreatedAt());
        response.setFormattedEstimatedDelivery(formatDate(shipment.getEstimatedDeliveryDate()));
        response.setFormattedActualDelivery(formatDate(shipment.getActualDeliveryDate()));
        response.setFormattedCreatedAt(formatDate(shipment.getCreatedAt()));
        
        // Set driver information
        if (shipment.getAssignedDriver() != null) {
            response.setDriverName(shipment.getAssignedDriver().getFirstName() + " " + 
                                 shipment.getAssignedDriver().getLastName());
            response.setDriverPhone(shipment.getAssignedDriver().getPhone());
            
            // Get vehicle information through the assigned driver
            try {
                List<Vehicle> driverVehicles = vehicleRepository.findByAssignedDriver(shipment.getAssignedDriver());
                if (!driverVehicles.isEmpty()) {
                    Vehicle driverVehicle = driverVehicles.get(0); // Get the first assigned vehicle
                    response.setDriverVehicleMake(driverVehicle.getMake());
                    response.setDriverVehicleModel(driverVehicle.getModel());
                    response.setDriverVehiclePlate(driverVehicle.getRegistrationNumber());
                }
            } catch (Exception e) {
                // Log the error but don't fail the response
                System.err.println("Error getting vehicle information for driver: " + e.getMessage());
            }
        }
        
        // Set status flags
        response.setDelivered(shipment.getStatus() == ShipmentStatus.DELIVERED);
        response.setInTransit(shipment.getStatus() == ShipmentStatus.IN_TRANSIT || 
                            shipment.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY ||
                            shipment.getStatus() == ShipmentStatus.PICKED_UP);
        response.setPending(shipment.getStatus() == ShipmentStatus.PENDING);
        
        // Get tracking events
        List<ShipmentTracking> trackingEntries = trackingRepository.findByShipmentOrderByCreatedAtDesc(shipment);
        List<CustomerPackageResponse.TrackingEvent> events = trackingEntries.stream()
                .map(entry -> new CustomerPackageResponse.TrackingEvent(
                    entry.getStatus().toString(),
                    entry.getLocation(),
                    entry.getNotes(),
                    entry.getCreatedAt()
                ))
                .collect(Collectors.toList());
        response.setTrackingEvents(events);
        
        // Set current location and last update
        if (!events.isEmpty()) {
            response.setCurrentLocation(events.get(0).getLocation());
            response.setLastUpdate(events.get(0).getFormattedTimestamp());
        }
        
        return response;
    }

    private String formatStatus(ShipmentStatus status) {
        if (status == null) return "UNKNOWN";
        switch (status) {
            case PENDING: return "Pending";
            case PICKED_UP: return "Picked Up";
            case IN_TRANSIT: return "In Transit";
            case OUT_FOR_DELIVERY: return "Out for Delivery";
            case DELIVERED: return "Delivered";
            case FAILED_DELIVERY: return "Failed Delivery";
            case CANCELLED: return "Cancelled";
            default: return status.toString();
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("MMM dd, yyyy").format(date);
    }

    private String formatAddress(String address, String city, String state) {
        return address + ", " + city + ", " + state;
    }

    private String formatDeliveryTime(long timeInMillis) {
        long days = timeInMillis / (24 * 60 * 60 * 1000);
        return days + " days";
    }
}
