package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageResponse;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentTrackingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.VehicleRepository;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
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
    private final UserService userService;
    private final NotificationService notificationService;
    private final PricingService pricingService;
    
    // In-memory storage for quotes (in production, this should be in a database)
    private final Map<String, QuoteResponse> quoteStorage = new ConcurrentHashMap<>();
    private final Map<String, CustomerPackageRequest> quoteRequests = new ConcurrentHashMap<>();

    @Autowired
    public CustomerPackageServiceImpl(ShipmentRepository shipmentRepository,
                                    ShipmentTrackingRepository trackingRepository,
                                    VehicleRepository vehicleRepository,
                                    UserService userService,
                                    NotificationService notificationService,
                                    PricingService pricingService) {
        this.shipmentRepository = shipmentRepository;
        this.trackingRepository = trackingRepository;
        this.vehicleRepository = vehicleRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.pricingService = pricingService;
    }

    @Override
    public QuoteResponse createQuote(CustomerPackageRequest request) {
        // Generate quote ID
        String quoteId = "Q" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Calculate costs for different service types
        List<QuoteResponse.ServiceOption> availableServices = calculateServiceOptions(request);
        
        // Get recommended service (usually the middle option)
        QuoteResponse.ServiceOption recommendedService = availableServices.stream()
                .filter(QuoteResponse.ServiceOption::isRecommended)
                .findFirst()
                .orElse(availableServices.get(1)); // Default to second option if no recommended
        
        // Create quote response
        QuoteResponse quote = new QuoteResponse();
        quote.setQuoteId(quoteId);
        quote.setTrackingNumber("TEMP_" + quoteId); // Temporary tracking number
        quote.setTotalCost(recommendedService.getCost());
        quote.setBaseCost(calculateBaseCost(request));
        quote.setServiceFee(calculateServiceFee(request, recommendedService.getServiceType()));
        quote.setInsuranceFee(BigDecimal.ZERO); // Default no insurance
        quote.setFuelSurcharge(calculateFuelSurcharge(request));
        quote.setServiceType(recommendedService.getServiceType());
        quote.setEstimatedDeliveryTime(recommendedService.getEstimatedDeliveryTime());
        quote.setEstimatedDeliveryDate(calculateEstimatedDeliveryDate(recommendedService.getServiceType()));
        quote.setAvailableServices(availableServices);
        quote.setPickupAddress(formatAddress(request.getPickupAddress(), request.getPickupCity(), request.getPickupState()));
        quote.setDeliveryAddress(formatAddress(request.getDeliveryAddress(), request.getDeliveryCity(), request.getDeliveryState()));
        quote.setWeight(request.getWeight());
        quote.setDimensions(request.getDimensions());
        quote.setDescription(request.getDescription());
        quote.setQuoteExpiryDate(calculateQuoteExpiryDate());
        quote.setActive(true);
        
        // Store quote for later retrieval
        quoteStorage.put(quoteId, quote);
        quoteRequests.put(quoteId, request);
        
        return quote;
    }

    @Override
    public Shipment createShipmentFromQuote(String quoteId, CustomerPackageRequest request) {
        QuoteResponse quote = quoteStorage.get(quoteId);
        if (quote == null || !quote.isActive()) {
            throw new RuntimeException("Invalid or expired quote");
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
        shipment.setShippingCost(quote.getTotalCost());
        shipment.setServiceType(quote.getServiceType());
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setEstimatedDeliveryDate(quote.getEstimatedDeliveryDate());
        
        // Save shipment
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Create initial tracking entry
        createTrackingEntry(savedShipment, ShipmentStatus.PENDING, "Shipment created", "Shipment registered in the system");
        
        // Send notifications
        notificationService.sendShipmentCreatedNotification(savedShipment);
        
        // Remove quote from storage
        quoteStorage.remove(quoteId);
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
        List<QuoteResponse.ServiceOption> options = new ArrayList<>();
        
        // Economy service
        BigDecimal economyCost = pricingService.calculateCourierPrice(ServiceType.ECONOMY);
        options.add(new QuoteResponse.ServiceOption(
            ServiceType.ECONOMY, 
            economyCost, 
            "5-7 business days", 
            "Most economical option", 
            false
        ));
        
        // Standard service
        BigDecimal standardCost = pricingService.calculateCourierPrice(ServiceType.OVERNIGHT);
        options.add(new QuoteResponse.ServiceOption(
            ServiceType.OVERNIGHT, 
            standardCost, 
            "3-5 business days", 
            "Balanced speed and cost", 
            true
        ));
        
        // Express service
        BigDecimal expressCost = pricingService.calculateCourierPrice(ServiceType.SAME_DAY);
        options.add(new QuoteResponse.ServiceOption(
            ServiceType.SAME_DAY, 
            expressCost, 
            "1-2 business days", 
            "Fastest delivery option", 
            false
        ));
        
        return options;
    }

    private BigDecimal calculateBaseCost(CustomerPackageRequest request) {
        // Base cost calculation based on weight and distance
        double baseRate = 10.0; // Base rate per pound
        double distanceMultiplier = calculateDistanceMultiplier(request);
        return new BigDecimal(request.getWeight() * baseRate * distanceMultiplier);
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
        // Fuel surcharge calculation (simplified)
        return new BigDecimal("3.50");
    }

    private double calculateDistanceMultiplier(CustomerPackageRequest request) {
        // Simplified distance calculation
        // In a real implementation, this would use geocoding APIs
        return 1.2; // Default multiplier
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
