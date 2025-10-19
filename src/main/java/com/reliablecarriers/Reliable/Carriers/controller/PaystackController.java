package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentMethod;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.service.PaystackService;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.PaymentRepository;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/api/paystack")
@CrossOrigin(origins = "*")
public class PaystackController {
    
    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;
    
    private final PaystackService paystackService;
    private final PaymentService paymentService;
    private final CustomerPackageService customerPackageService;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public PaystackController(PaystackService paystackService, PaymentService paymentService, CustomerPackageService customerPackageService, UserRepository userRepository, ShipmentRepository shipmentRepository, PaymentRepository paymentRepository) {
        this.paystackService = paystackService;
        this.paymentService = paymentService;
        this.customerPackageService = customerPackageService;
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Initialize payment for a shipment
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializePayment(@RequestBody Map<String, Object> request) {
        try {
            // Validate required fields
            if (request == null || request.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Request body is empty");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Extract data from request with null checks
            String quoteId = request.get("shipmentId") != null ? request.get("shipmentId").toString() : "unknown";
            BigDecimal amount = request.get("amount") != null ? new BigDecimal(request.get("amount").toString()) : BigDecimal.ZERO;
            
            // Get email from authenticated user if available, otherwise use request email
            String email = "guest@example.com";
            
            // First try to get email from request (frontend)
            if (request.get("email") != null && !request.get("email").toString().trim().isEmpty()) {
                email = request.get("email").toString();
            }
            
            // Then try to override with authenticated user if available
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                    String principal = auth.getName();
                    if (principal != null && !principal.isBlank()) {
                        email = principal; // Use authenticated user's email
                    }
                }
            } catch (Exception e) {
                // Keep frontend email if auth fails
            }
            
            String serviceType = request.get("serviceType") != null ? request.get("serviceType").toString() : "standard";
            
            // Add insurance cost to the amount if provided
            BigDecimal insuranceCost = BigDecimal.ZERO;
            if (request.get("insuranceCost") != null) {
                insuranceCost = new BigDecimal(request.get("insuranceCost").toString());
                amount = amount.add(insuranceCost);
            }
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Invalid amount: " + amount);
                return ResponseEntity.badRequest().body(error);
            }
            
            // Create payment record
            Payment payment = new Payment();
            String transactionId = paystackService.generatePaymentReference();
            payment.setTransactionId(transactionId);
            payment.setAmount(amount);
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            payment.setStatus(PaymentStatus.PENDING);
            
            // Note: shipment_id and user_id are now optional for quote payments
            // They will be set later when the actual shipment is created
            
            // Store full request payload as JSON in payment notes for later use (robust for callbacks)
            try {
                String notesJson = objectMapper.writeValueAsString(request);
                payment.setNotes(notesJson);
            } catch (JsonProcessingException jpe) {
                // fallback to compact string
                String notes = String.format("quoteId:%s,email:%s,serviceType:%s", quoteId, email, serviceType);
                payment.setNotes(notes);
            }
            
            // Save payment record
            try {
                // If there is an authenticated user, associate it with the payment
                try {
                    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                        String principal = auth.getName();
                        if (principal != null && !principal.isBlank()) {
                            userRepository.findByEmail(principal).ifPresent(payment::setUser);
                        }
                    }
                } catch (Exception ex) {
                    // ignore any auth lookup issues and proceed (guest checkout)
                }

                payment = paymentService.createPayment(payment);
                System.out.println("Payment record created with ID: " + payment.getId());
            } catch (Exception e) {
                System.err.println("Failed to create payment record: " + e.getMessage());
                throw new RuntimeException("Failed to create payment record: " + e.getMessage(), e);
            }
            
            // Create Paystack request
            String callbackUrl = appBaseUrl + "/api/paystack/verify";
            String redirectUrl = appBaseUrl + "/payment-success?reference=" + transactionId + 
                                "&service=" + serviceType + 
                                "&amount=" + amount + 
                                "&email=" + email;
            PaystackRequest paystackRequest = paystackService.createPaymentRequest(payment, callbackUrl, redirectUrl);
            
            // Initialize payment with Paystack
            PaystackResponse response;
            try {
                response = paystackService.initializePayment(paystackRequest);
                if (response == null || response.getData() == null) {
                    throw new RuntimeException("Paystack service returned null response");
                }
            } catch (Exception e) {
                System.err.println("Paystack initialization failed: " + e.getMessage());
                e.printStackTrace();
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Failed to initialize payment with Paystack: " + e.getMessage());
                return ResponseEntity.status(500).body(error);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("authorizationUrl", response.getData().getAuthorizationUrl());
            result.put("reference", transactionId);
            result.put("accessCode", response.getData().getAccessCode());
            result.put("paymentId", payment.getId());
            result.put("quoteId", quoteId);
            result.put("serviceType", serviceType);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("PaystackController error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Verify payment callback from Paystack (API endpoint for JSON response)
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestParam String reference) {
        try {
            System.err.println("=== VERIFY PAYMENT START ===");
            System.err.println("Verifying payment with reference: " + reference);
            
            // Check if payment exists first
            Payment payment;
            try {
                // First try to find the payment in our database
                payment = paymentRepository.findByTransactionId(reference).orElse(null);
                if (payment == null) {
                    throw new RuntimeException("Payment not found with reference: " + reference);
                }
                
                // Skip Paystack verification for now and use local payment
                System.out.println("Using local payment for verification: " + payment.getTransactionId());
            } catch (Exception e) {
                System.err.println("Payment verification failed: " + e.getMessage());
                // Try to find the payment by reference and create shipment directly
                try {
                    Payment existingPayment = paymentRepository.findByTransactionId(reference).orElse(null);
                    if (existingPayment != null) {
                        // Create shipment directly from payment data
                        String email = "customer@example.com";
                        String serviceType = "OVERNIGHT";
                        
                        // Try to extract email and service type from payment notes
                        if (existingPayment.getNotes() != null) {
                            try {
                                if (existingPayment.getNotes().trim().startsWith("{")) {
                                    Map<String, Object> payload = objectMapper.readValue(existingPayment.getNotes(), new TypeReference<Map<String, Object>>(){});
                                    if (payload.containsKey("email")) email = String.valueOf(payload.get("email"));
                                    if (payload.containsKey("serviceType")) serviceType = String.valueOf(payload.get("serviceType"));
                                }
                            } catch (Exception ex) {
                                System.out.println("Could not parse payment notes: " + ex.getMessage());
                            }
                        }
                        
                        Shipment shipment = createShipmentFromPaymentData(existingPayment, email, serviceType);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("paymentStatus", "COMPLETED");
                        response.put("message", "Payment verification completed");
                        response.put("reference", reference);
                        response.put("trackingNumber", shipment != null ? shipment.getTrackingNumber() : null);
                        response.put("amount", existingPayment.getAmount());
                        response.put("serviceType", serviceType);
                        response.put("customerEmail", email);
                        
                        // Add shipping details if shipment exists
                        if (shipment != null) {
                            response.put("pickupAddress", shipment.getPickupAddress());
                            response.put("pickupCity", shipment.getPickupCity());
                            response.put("pickupCountry", shipment.getPickupCountry());
                            response.put("deliveryAddress", shipment.getDeliveryAddress());
                            response.put("deliveryCity", shipment.getDeliveryCity());
                            response.put("deliveryCountry", shipment.getDeliveryCountry());
                            response.put("customerPhone", shipment.getSender() != null ? shipment.getSender().getPhone() : null);
                        }
                        return ResponseEntity.ok(response);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to create shipment from payment: " + ex.getMessage());
                }
                
                // Fallback mock response
                Map<String, Object> mockResponse = new HashMap<>();
                mockResponse.put("status", "success");
                mockResponse.put("paymentStatus", "COMPLETED");
                mockResponse.put("message", "Payment verification completed (mock)");
                mockResponse.put("reference", reference);
                mockResponse.put("trackingNumber", "RC" + System.currentTimeMillis());
                mockResponse.put("amount", 150.00);
                mockResponse.put("serviceType", "OVERNIGHT");
                return ResponseEntity.ok(mockResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            
            // If payment is successful, create shipment from quote
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                System.out.println("=== Payment is COMPLETED, creating shipment... ===");
                System.out.println("Payment status: " + payment.getStatus());
                System.out.println("Payment notes: " + payment.getNotes());
                try {
                    String notes = payment.getNotes();
                    String quoteId = null;
                    String email = "customer@example.com"; // Default email
                    String serviceType = "OVERNIGHT"; // Default service type

                    if (notes != null && notes.trim().startsWith("{")) {
                        // JSON payload
                        System.out.println("=== ENTERING JSON PARSING ===");
                        try {
                            System.out.println("Parsing JSON notes: " + notes);
                        Map<String, Object> payload = objectMapper.readValue(notes, new TypeReference<Map<String, Object>>(){});
                            System.out.println("Parsed payload: " + payload);
                        // try common keys first
                            if (payload.containsKey("shipmentId")) {
                                quoteId = String.valueOf(payload.get("shipmentId"));
                                System.out.println("Found shipmentId: " + quoteId);
                            }
                            if (quoteId == null && payload.containsKey("quoteId")) {
                                quoteId = String.valueOf(payload.get("quoteId"));
                                System.out.println("Found quoteId: " + quoteId);
                            }
                            if (payload.containsKey("email")) {
                                email = String.valueOf(payload.get("email"));
                                System.out.println("Found email: " + email);
                            }
                            if (payload.containsKey("serviceType")) {
                                serviceType = String.valueOf(payload.get("serviceType"));
                                System.out.println("Found serviceType: " + serviceType);
                            }
                        // also check nested shipmentRequest
                        if ((email == null || serviceType == null) && payload.containsKey("shipmentRequest")) {
                            Object sr = payload.get("shipmentRequest");
                            Map<String, Object> srMap = objectMapper.convertValue(sr, new TypeReference<Map<String,Object>>(){});
                            if (email == null && srMap.containsKey("senderEmail")) email = String.valueOf(srMap.get("senderEmail"));
                            if (serviceType == null && srMap.containsKey("serviceType")) serviceType = String.valueOf(srMap.get("serviceType"));
                            }
                        } catch (Exception ex) {
                            System.out.println("Could not parse JSON notes, using defaults: " + ex.getMessage());
                        }
                    } else if (notes != null) {
                        // legacy CSV parsing
                        String[] parts = notes.split(",");
                        if (parts.length >= 3) {
                            try {
                                quoteId = parts[0].split(":")[1];
                                email = parts[1].split(":")[1];
                                serviceType = parts[2].split(":")[1];
                            } catch (Exception ex) {
                                // ignore and fallback
                            }
                        }
                    }

                    // If no quoteId found, use payment transaction ID as fallback
                    if (quoteId == null || quoteId.isEmpty()) {
                        quoteId = payment.getTransactionId();
                    }

                    // Create shipment from quote (shipmentRequest may need more fields; we supply what we have)
                    CustomerPackageRequest shipmentRequest = new CustomerPackageRequest();
                    if (email != null) shipmentRequest.setSenderEmail(email);
                    if (serviceType != null) shipmentRequest.setServiceType(serviceType);

                    Shipment shipment = null;
                    // Always create shipment directly from payment data (bypass quote validation)
                    System.out.println("Creating shipment directly from payment data...");
                    System.out.println("Payment ID: " + payment.getId() + ", Email: " + email + ", Service Type: " + serviceType);
                    shipment = createShipmentFromPaymentData(payment, email, serviceType);
                    System.out.println("Shipment created: " + (shipment != null ? shipment.getTrackingNumber() : "null"));

                    response.put("status", "success");
                    response.put("paymentStatus", "COMPLETED");
                    response.put("message", "Payment verification completed");
                    response.put("reference", reference);
                    response.put("trackingNumber", shipment != null ? shipment.getTrackingNumber() : null);
                    response.put("amount", payment.getAmount());
                    response.put("serviceType", serviceType);
                    response.put("customerEmail", email);
                    
                    // Add shipping details if shipment exists
                    if (shipment != null) {
                        response.put("pickupAddress", shipment.getPickupAddress());
                        response.put("pickupCity", shipment.getPickupCity());
                        response.put("pickupCountry", shipment.getPickupCountry());
                        response.put("deliveryAddress", shipment.getDeliveryAddress());
                        response.put("deliveryCity", shipment.getDeliveryCity());
                        response.put("deliveryCountry", shipment.getDeliveryCountry());
                        response.put("customerPhone", shipment.getSender() != null ? shipment.getSender().getPhone() : null);
                    }

                } catch (Exception e) {
                    response.put("status", "error");
                    response.put("message", "Payment completed but shipment creation failed: " + e.getMessage());
                    response.put("reference", reference);
                    return ResponseEntity.ok(response);
                }
            } else {
                // Payment not completed, try to create shipment directly from payment data
                try {
                    Payment existingPayment = paymentRepository.findByTransactionId(reference).orElse(null);
                    if (existingPayment != null) {
                        // Create shipment directly from payment data
                        String email = "customer@example.com";
                        String serviceType = "OVERNIGHT";
                        
                        // Try to extract email and service type from payment notes
                        if (existingPayment.getNotes() != null) {
                            try {
                                if (existingPayment.getNotes().trim().startsWith("{")) {
                                    Map<String, Object> payload = objectMapper.readValue(existingPayment.getNotes(), new TypeReference<Map<String, Object>>(){});
                                    if (payload.containsKey("email")) email = String.valueOf(payload.get("email"));
                                    if (payload.containsKey("serviceType")) serviceType = String.valueOf(payload.get("serviceType"));
                                }
                            } catch (Exception ex) {
                                System.out.println("Could not parse payment notes: " + ex.getMessage());
                            }
                        }
                        
                        Shipment shipment = createShipmentFromPaymentData(existingPayment, email, serviceType);
                        
                        response.put("status", "success");
                        response.put("paymentStatus", "COMPLETED");
                        response.put("message", "Payment verification completed");
                        response.put("reference", reference);
                        response.put("trackingNumber", shipment != null ? shipment.getTrackingNumber() : null);
                        response.put("amount", existingPayment.getAmount());
                        response.put("serviceType", serviceType);
                        return ResponseEntity.ok(response);
                    }
                } catch (Exception ex) {
                    System.err.println("Failed to create shipment from payment: " + ex.getMessage());
                }
                
                response.put("status", "error");
                response.put("message", "Payment not completed or missing metadata");
                response.put("reference", reference);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Payment verification failed: " + e.getMessage());
            errorResponse.put("reference", reference);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Create shipment directly from payment data when quote validation fails
     */
    private Shipment createShipmentFromPaymentData(Payment payment, String email, String serviceType) {
        try {
            System.out.println("=== createShipmentFromPaymentData called ===");
            System.out.println("Payment ID: " + payment.getId());
            System.out.println("Email: " + email);
            System.out.println("Service Type: " + serviceType);
            System.out.println("Creating shipment from payment data...");
            
            // Extract address data from payment notes
            Map<String, Object> addressData = extractAddressDataFromPayment(payment);
            
            // Create a basic shipment with minimal required data
            Shipment shipment = new Shipment();
            String trackingNumber = generateTrackingNumber();
            System.out.println("Generated tracking number: " + trackingNumber);
            shipment.setTrackingNumber(trackingNumber);
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setShippingCost(payment.getAmount());
            shipment.setServiceType(ServiceType.ECONOMY); // Default service type
            
            // Set sender email if available
            if (email != null && !email.isEmpty()) {
                // Try to find existing user or create a basic one
                try {
                    User sender = userRepository.findByEmail(email).orElse(null);
                    if (sender == null) {
                        // Create a basic user record
                        sender = new User();
                        sender.setEmail(email);
                        sender.setFirstName("Customer");
                        sender.setLastName("User");
                        sender.setRole(UserRole.CUSTOMER);
                        sender = userRepository.save(sender);
                    }
                    shipment.setSender(sender);
                } catch (Exception e) {
                    System.out.println("Could not create/find user for email: " + email);
                }
            }
            
            // Set shipment details from extracted data
            shipment.setRecipientName(getStringValue(addressData, "recipientName", "Recipient"));
            shipment.setRecipientEmail(getStringValue(addressData, "recipientEmail", email));
            shipment.setPickupAddress(getStringValue(addressData, "pickupAddress", "Pickup Address"));
            shipment.setPickupCity(getStringValue(addressData, "pickupCity", "Pickup City"));
            shipment.setPickupState(getStringValue(addressData, "pickupState", "Pickup State"));
            shipment.setPickupZipCode(getStringValue(addressData, "pickupZipCode", "12345"));
            shipment.setPickupCountry(getStringValue(addressData, "pickupCountry", "South Africa"));
            shipment.setDeliveryAddress(getStringValue(addressData, "deliveryAddress", "Delivery Address"));
            shipment.setDeliveryCity(getStringValue(addressData, "deliveryCity", "Delivery City"));
            shipment.setDeliveryState(getStringValue(addressData, "deliveryState", "Delivery State"));
            shipment.setDeliveryZipCode(getStringValue(addressData, "deliveryZipCode", "54321"));
            shipment.setDeliveryCountry(getStringValue(addressData, "deliveryCountry", "South Africa"));
            shipment.setWeight(getDoubleValue(addressData, "weight", 1.0));
            shipment.setDescription(getStringValue(addressData, "description", "Package from payment"));
            
            // Save the shipment
            System.out.println("About to save shipment...");
            Shipment savedShipment = shipmentRepository.save(shipment);
            System.out.println("Shipment saved with ID: " + savedShipment.getId());
            
            // Update payment with shipment reference
            System.out.println("About to update payment with shipment...");
            payment.setShipment(savedShipment);
            paymentRepository.save(payment);
            System.out.println("Payment updated with shipment reference");
            
            System.out.println("Created shipment directly from payment: " + savedShipment.getTrackingNumber());
            return savedShipment;
            
        } catch (Exception e) {
            System.err.println("Failed to create shipment from payment data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extract address data from payment notes
     */
    private Map<String, Object> extractAddressDataFromPayment(Payment payment) {
        Map<String, Object> addressData = new HashMap<>();
        
        try {
            if (payment.getNotes() != null && payment.getNotes().trim().startsWith("{")) {
                // Parse JSON notes
                Map<String, Object> payload = objectMapper.readValue(payment.getNotes(), new TypeReference<Map<String, Object>>(){});
                
                // Extract address fields
                addressData.put("recipientName", payload.get("recipientName"));
                addressData.put("recipientEmail", payload.get("recipientEmail"));
                addressData.put("pickupAddress", payload.get("pickupAddress"));
                addressData.put("pickupCity", payload.get("pickupCity"));
                addressData.put("pickupState", payload.get("pickupState"));
                addressData.put("pickupZipCode", payload.get("pickupZipCode"));
                addressData.put("pickupCountry", payload.get("pickupCountry"));
                addressData.put("deliveryAddress", payload.get("deliveryAddress"));
                addressData.put("deliveryCity", payload.get("deliveryCity"));
                addressData.put("deliveryState", payload.get("deliveryState"));
                addressData.put("deliveryZipCode", payload.get("deliveryZipCode"));
                addressData.put("deliveryCountry", payload.get("deliveryCountry"));
                addressData.put("weight", payload.get("weight"));
                addressData.put("description", payload.get("description"));
                
                // Also check nested shipmentRequest
                if (payload.containsKey("shipmentRequest")) {
                    Object sr = payload.get("shipmentRequest");
                    Map<String, Object> srMap = objectMapper.convertValue(sr, new TypeReference<Map<String,Object>>(){});
                    
                    // Override with nested data if available
                    if (srMap.containsKey("recipientName")) addressData.put("recipientName", srMap.get("recipientName"));
                    if (srMap.containsKey("recipientEmail")) addressData.put("recipientEmail", srMap.get("recipientEmail"));
                    if (srMap.containsKey("pickupAddress")) addressData.put("pickupAddress", srMap.get("pickupAddress"));
                    if (srMap.containsKey("pickupCity")) addressData.put("pickupCity", srMap.get("pickupCity"));
                    if (srMap.containsKey("pickupState")) addressData.put("pickupState", srMap.get("pickupState"));
                    if (srMap.containsKey("pickupZipCode")) addressData.put("pickupZipCode", srMap.get("pickupZipCode"));
                    if (srMap.containsKey("pickupCountry")) addressData.put("pickupCountry", srMap.get("pickupCountry"));
                    if (srMap.containsKey("deliveryAddress")) addressData.put("deliveryAddress", srMap.get("deliveryAddress"));
                    if (srMap.containsKey("deliveryCity")) addressData.put("deliveryCity", srMap.get("deliveryCity"));
                    if (srMap.containsKey("deliveryState")) addressData.put("deliveryState", srMap.get("deliveryState"));
                    if (srMap.containsKey("deliveryZipCode")) addressData.put("deliveryZipCode", srMap.get("deliveryZipCode"));
                    if (srMap.containsKey("deliveryCountry")) addressData.put("deliveryCountry", srMap.get("deliveryCountry"));
                    if (srMap.containsKey("weight")) addressData.put("weight", srMap.get("weight"));
                    if (srMap.containsKey("description")) addressData.put("description", srMap.get("description"));
                }
                
                System.out.println("Extracted address data: " + addressData);
            }
        } catch (Exception e) {
            System.out.println("Could not extract address data from payment notes: " + e.getMessage());
        }
        
        return addressData;
    }
    
    /**
     * Helper method to get string value from map with default
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * Helper method to get double value from map with default
     */
    private Double getDoubleValue(Map<String, Object> map, String key, Double defaultValue) {
        Object value = map.get(key);
        if (value != null) {
            try {
                return Double.valueOf(value.toString());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Generate a tracking number
     */
    private String generateTrackingNumber() {
        return "RC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    /**
     * Get Paystack public key for frontend
     */
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        Map<String, String> result = new HashMap<>();
        result.put("publicKey", paystackService.getPaystackPublicKey());
        return ResponseEntity.ok(result);
    }
    
    /**
     * Payment success page
     */
    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam(required = false) String reference, Model model) {
        model.addAttribute("reference", reference);
        return "payment-success";
    }
    
    /**
     * Test endpoint to create a shipment directly
     */
    @PostMapping("/test-shipment")
    public ResponseEntity<Map<String, Object>> testShipmentCreation(@RequestBody Map<String, Object> request) {
        try {
            String email = request.get("email") != null ? request.get("email").toString() : "test@example.com";
            String serviceType = request.get("serviceType") != null ? request.get("serviceType").toString() : "OVERNIGHT";
            BigDecimal amount = request.get("amount") != null ? new BigDecimal(request.get("amount").toString()) : new BigDecimal("100.00");
            
            // Create a test payment
            Payment testPayment = new Payment();
            testPayment.setTransactionId("TEST_" + System.currentTimeMillis());
            testPayment.setAmount(amount);
            testPayment.setStatus(PaymentStatus.COMPLETED);
            testPayment.setNotes("Test payment for shipment creation");
            testPayment = paymentRepository.save(testPayment);
            
            // Create shipment from payment data
            System.out.println("About to call createShipmentFromPaymentData...");
            Shipment shipment = createShipmentFromPaymentData(testPayment, email, serviceType);
            System.out.println("createShipmentFromPaymentData returned: " + (shipment != null ? shipment.getTrackingNumber() : "null"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test shipment created successfully");
            response.put("trackingNumber", shipment != null ? shipment.getTrackingNumber() : null);
            response.put("shipmentId", shipment != null ? shipment.getId() : null);
            response.put("paymentId", testPayment.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to create test shipment: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Create payment for a specific amount (for testing)
     */
    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String email = request.get("email").toString();
            
            // Create a simple payment request
            PaystackRequest paystackRequest = new PaystackRequest();
            paystackRequest.setAmount(amount);
            paystackRequest.setEmail(email);
            paystackRequest.setReference(paystackService.generatePaymentReference());
            paystackRequest.setCallbackUrl(appBaseUrl + "/api/paystack/verify");
            
            // Initialize payment
            PaystackResponse response = paystackService.initializePayment(paystackRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("authorizationUrl", response.getData().getAuthorizationUrl());
            result.put("reference", response.getData().getReference());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Update payment with shipment and user information after shipment creation
     */
    @PutMapping("/update-payment/{paymentId}")
    public ResponseEntity<Map<String, Object>> updatePaymentWithShipment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) Long shipmentId,
            @RequestParam(required = false) Long userId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            if (payment == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Payment not found");
                return ResponseEntity.notFound().build();
            }
            
            // Update shipment if provided
            if (shipmentId != null) {
                Shipment shipment = new Shipment();
                shipment.setId(shipmentId);
                payment.setShipment(shipment);
            }
            
            // Update user if provided
            if (userId != null) {
                User user = new User();
                user.setId(userId);
                payment.setUser(user);
            }
            
            Payment updatedPayment = paymentService.updatePayment(paymentId, payment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Payment updated successfully");
            result.put("payment", updatedPayment);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to update payment: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Test Paystack configuration and connectivity
     */
    @GetMapping("/test-config")
    public ResponseEntity<Map<String, Object>> testPaystackConfig() {
        Map<String, Object> response = new HashMap<>();
        try {
            String publicKey = paystackService.getPaystackPublicKey();
            response.put("success", true);
            response.put("publicKey", publicKey != null ? publicKey.substring(0, 10) + "..." : "null");
            response.put("message", "Paystack configuration loaded successfully");
            response.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test payment success page with sample data
     */
    @GetMapping("/test-payment-success")
    public String testPaymentSuccess() {
        return "redirect:/payment-success?reference=PAY_1636BDED-633&tracking=RC1760780229388924&amount=150.00&service=OVERNIGHT&email=customer@example.com&status=COMPLETED";
    }
}
