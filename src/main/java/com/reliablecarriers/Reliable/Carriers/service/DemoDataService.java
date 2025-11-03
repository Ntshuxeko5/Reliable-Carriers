package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Calendar;

/**
 * Demo Data Service for Client Demonstrations
 * Creates realistic sample data for showcasing the platform
 */
@Service
public class DemoDataService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    // @Autowired
    // private PaymentRepository paymentRepository; // Unused for now
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${demo.auto.create.users:true}")
    private boolean autoCreateUsers;

    @Value("${demo.sample.data.enabled:true}")
    private boolean sampleDataEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (autoCreateUsers && sampleDataEnabled) {
            createDemoUsers();
            createDemoBookings();
            createDemoShipments();
        }
    }

    /**
     * Create demo users for different roles
     */
    public void createDemoUsers() {
        try {
            // Demo Admin User
            if (!userRepository.findByEmail("admin@reliablecarriers.co.za").isPresent()) {
                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEmail("admin@reliablecarriers.co.za");
                admin.setPhone("+27123456789");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(UserRole.ADMIN);
                admin.setCustomerTier(CustomerTier.PREMIUM);
                admin.setCreatedAt(new Date());
                admin.setUpdatedAt(new Date());
                userRepository.save(admin);
                System.out.println("✅ Created demo admin user: admin@reliablecarriers.co.za / admin123");
            }

            // Demo Customer User
            if (!userRepository.findByEmail("customer@demo.com").isPresent()) {
                User customer = new User();
                customer.setFirstName("John");
                customer.setLastName("Customer");
                customer.setEmail("customer@demo.com");
                customer.setPhone("+27987654321");
                customer.setPassword(passwordEncoder.encode("customer123"));
                customer.setRole(UserRole.CUSTOMER);
                customer.setCustomerTier(CustomerTier.BUSINESS);
                customer.setAddress("123 Demo Street");
                customer.setCity("Johannesburg");
                customer.setState("Gauteng");
                customer.setZipCode("2000");
                customer.setCountry("South Africa");
                customer.setCreatedAt(new Date());
                customer.setUpdatedAt(new Date());
                userRepository.save(customer);
                System.out.println("✅ Created demo customer user: customer@demo.com / customer123");
            }

            // Demo Driver User
            if (!userRepository.findByEmail("driver@demo.com").isPresent()) {
                User driver = new User();
                driver.setFirstName("Mike");
                driver.setLastName("Driver");
                driver.setEmail("driver@demo.com");
                driver.setPhone("+27555123456");
                driver.setPassword(passwordEncoder.encode("driver123"));
                driver.setRole(UserRole.DRIVER);
                driver.setAddress("456 Driver Avenue");
                driver.setCity("Cape Town");
                driver.setState("Western Cape");
                driver.setZipCode("8000");
                driver.setCountry("South Africa");
                driver.setCreatedAt(new Date());
                driver.setUpdatedAt(new Date());
                userRepository.save(driver);
                System.out.println("✅ Created demo driver user: driver@demo.com / driver123");
            }

            // Demo Tracking Manager User
            if (!userRepository.findByEmail("tracking@demo.com").isPresent()) {
                User trackingManager = new User();
                trackingManager.setFirstName("Sarah");
                trackingManager.setLastName("Tracker");
                trackingManager.setEmail("tracking@demo.com");
                trackingManager.setPhone("+27444987654");
                trackingManager.setPassword(passwordEncoder.encode("tracking123"));
                trackingManager.setRole(UserRole.TRACKING_MANAGER);
                trackingManager.setAddress("789 Tracking Road");
                trackingManager.setCity("Durban");
                trackingManager.setState("KwaZulu-Natal");
                trackingManager.setZipCode("4000");
                trackingManager.setCountry("South Africa");
                trackingManager.setCreatedAt(new Date());
                trackingManager.setUpdatedAt(new Date());
                userRepository.save(trackingManager);
                System.out.println("✅ Created demo tracking manager: tracking@demo.com / tracking123");
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating demo users: " + e.getMessage());
        }
    }

    /**
     * Create demo bookings with realistic data
     */
    public void createDemoBookings() {
        try {
            User customer = userRepository.findByEmail("customer@demo.com").orElse(null);
            User driver = userRepository.findByEmail("driver@demo.com").orElse(null);

            if (customer == null || driver == null) {
                System.out.println("⚠️ Demo users not found, skipping booking creation");
                return;
            }

            // Demo Booking 1 - Completed
            if (bookingRepository.count() == 0) {
                Booking booking1 = createDemoBooking(
                    "DEMO001",
                    customer,
                    driver,
                    "123 Pickup Street, Johannesburg, Gauteng",
                    "456 Delivery Avenue, Cape Town, Western Cape",
                    ServiceType.SAME_DAY,
                    BookingStatus.DELIVERED,
                    new BigDecimal("750.00")
                );
                bookingRepository.save(booking1);

                // Demo Booking 2 - In Transit
                Booking booking2 = createDemoBooking(
                    "DEMO002",
                    customer,
                    driver,
                    "789 Business Park, Sandton, Gauteng",
                    "321 Office Complex, Durban, KwaZulu-Natal",
                    ServiceType.OVERNIGHT,
                    BookingStatus.IN_TRANSIT,
                    new BigDecimal("550.00")
                );
                bookingRepository.save(booking2);

                // Demo Booking 3 - Pending
                Booking booking3 = createDemoBooking(
                    "DEMO003",
                    customer,
                    null, // No driver assigned yet
                    "555 Home Address, Pretoria, Gauteng",
                    "777 Destination Street, Port Elizabeth, Eastern Cape",
                    ServiceType.ECONOMY,
                    BookingStatus.PENDING,
                    new BigDecimal("450.00")
                );
                bookingRepository.save(booking3);

                System.out.println("✅ Created 3 demo bookings");
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating demo bookings: " + e.getMessage());
        }
    }

    /**
     * Create demo shipments with tracking data
     */
    public void createDemoShipments() {
        try {
            User customer = userRepository.findByEmail("customer@demo.com").orElse(null);
            User driver = userRepository.findByEmail("driver@demo.com").orElse(null);

            if (customer == null || driver == null) {
                System.out.println("⚠️ Demo users not found, skipping shipment creation");
                return;
            }

            if (shipmentRepository.count() == 0) {
                // Demo Shipment 1 - Delivered
                Shipment shipment1 = createDemoShipment(
                    "RC123456789ZA",
                    customer,
                    driver,
                    "Demo Package 1 - Documents",
                    ShipmentStatus.DELIVERED,
                    new BigDecimal("750.00")
                );
                shipmentRepository.save(shipment1);

                // Demo Shipment 2 - Out for Delivery
                Shipment shipment2 = createDemoShipment(
                    "RC987654321ZA",
                    customer,
                    driver,
                    "Demo Package 2 - Electronics",
                    ShipmentStatus.OUT_FOR_DELIVERY,
                    new BigDecimal("550.00")
                );
                shipmentRepository.save(shipment2);

                // Demo Shipment 3 - In Transit
                Shipment shipment3 = createDemoShipment(
                    "RC456789123ZA",
                    customer,
                    driver,
                    "Demo Package 3 - Clothing",
                    ShipmentStatus.IN_TRANSIT,
                    new BigDecimal("450.00")
                );
                shipmentRepository.save(shipment3);

                System.out.println("✅ Created 3 demo shipments with tracking numbers");
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating demo shipments: " + e.getMessage());
        }
    }

    // Helper methods
    private Booking createDemoBooking(String bookingNumber, User customer, User driver, 
                                    String pickupAddress, String deliveryAddress, 
                                    ServiceType serviceType, BookingStatus status, BigDecimal amount) {
        Booking booking = new Booking();
        booking.setBookingNumber(bookingNumber);
        booking.setStatus(status);
        booking.setServiceType(serviceType);
        
        // Customer details
        booking.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        booking.setCustomerEmail(customer.getEmail());
        booking.setCustomerPhone(customer.getPhone());
        
        // Pickup details
        String[] pickupParts = pickupAddress.split(", ");
        booking.setPickupAddress(pickupParts[0]);
        booking.setPickupCity(pickupParts.length > 1 ? pickupParts[1] : "Johannesburg");
        booking.setPickupState(pickupParts.length > 2 ? pickupParts[2] : "Gauteng");
        booking.setPickupPostalCode("2000");
        booking.setPickupContactName(customer.getFirstName() + " " + customer.getLastName());
        booking.setPickupContactPhone(customer.getPhone());
        
        // Delivery details
        String[] deliveryParts = deliveryAddress.split(", ");
        booking.setDeliveryAddress(deliveryParts[0]);
        booking.setDeliveryCity(deliveryParts.length > 1 ? deliveryParts[1] : "Cape Town");
        booking.setDeliveryState(deliveryParts.length > 2 ? deliveryParts[2] : "Western Cape");
        booking.setDeliveryPostalCode("8000");
        booking.setDeliveryContactName("Recipient Name");
        booking.setDeliveryContactPhone("+27123456789");
        
        // Package details
        booking.setDescription("Demo package for client presentation");
        booking.setWeight(2.5);
        booking.setDimensions("30x20x15 cm");
        
        // Pricing
        booking.setBasePrice(amount.multiply(new BigDecimal("0.8")));
        booking.setServiceFee(amount.multiply(new BigDecimal("0.05")));
        booking.setInsuranceFee(BigDecimal.ZERO);
        booking.setFuelSurcharge(amount.multiply(new BigDecimal("0.03")));
        booking.setTotalAmount(amount);
        
        // Payment
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setPaymentDate(new Date());
        booking.setPaymentReference("DEMO_PAY_" + bookingNumber);
        
        // Driver assignment
        if (driver != null) {
            booking.setDriver(driver);
        }
        
        // Timestamps
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2); // 2 days ago
        booking.setCreatedAt(cal.getTime());
        booking.setUpdatedAt(new Date());
        
        return booking;
    }

    private Shipment createDemoShipment(String trackingNumber, User sender, User driver, 
                                      String description, ShipmentStatus status, BigDecimal cost) {
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setSender(sender);
        shipment.setAssignedDriver(driver);
        shipment.setDescription(description);
        shipment.setStatus(status);
        shipment.setShippingCost(cost);
        shipment.setServiceType(ServiceType.OVERNIGHT);
        
        // Addresses
        shipment.setPickupAddress("Demo Pickup Address");
        shipment.setDeliveryAddress("Demo Delivery Address");
        
        // Dates
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // Yesterday
        shipment.setCreatedAt(cal.getTime());
        
        if (status == ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryDate(new Date());
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 2); // Day after tomorrow
            shipment.setEstimatedDeliveryDate(cal.getTime());
        }
        
        return shipment;
    }

    /**
     * Get demo login credentials for client presentation
     */
    public String getDemoCredentials() {
        return """
                🎯 DEMO LOGIN CREDENTIALS:
                
                👤 Customer Login:
                Email: customer@demo.com
                Password: customer123
                
                🚛 Driver Login:
                Email: driver@demo.com  
                Password: driver123
                
                📊 Admin Login:
                Email: admin@reliablecarriers.co.za
                Password: admin123
                
                📍 Tracking Manager:
                Email: tracking@demo.com
                Password: tracking123
                
                📦 Demo Tracking Numbers:
                RC123456789ZA (Delivered)
                RC987654321ZA (Out for Delivery)
                RC456789123ZA (In Transit)
                """;
    }
}
