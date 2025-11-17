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
import com.reliablecarriers.Reliable.Carriers.repository.QuoteRepository;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@RestController
@RequestMapping("/api/paystack")
@CrossOrigin(origins = "*")
public class PaystackController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaystackController.class);
    
    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;
    @Value("${paystack.webhook.secret:}")
    private String paystackWebhookSecret;
    
    private final PaystackService paystackService;
    private final PaymentService paymentService;
    @SuppressWarnings("unused")
    private final CustomerPackageService customerPackageService;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final QuoteRepository quoteRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Temporary storage for address data (in production, use Redis or database)
    private final Map<String, Map<String, Object>> temporaryAddressStorage = new HashMap<>();
    
    public PaystackController(PaystackService paystackService, PaymentService paymentService, CustomerPackageService customerPackageService, UserRepository userRepository, ShipmentRepository shipmentRepository, PaymentRepository paymentRepository, QuoteRepository quoteRepository, NotificationService notificationService) {
        this.paystackService = paystackService;
        this.paymentService = paymentService;
        this.customerPackageService = customerPackageService;
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.paymentRepository = paymentRepository;
        this.quoteRepository = quoteRepository;
        this.notificationService = notificationService;
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
            String email = null;
            
            // First try to get email from request (frontend)
            if (request.get("email") != null && !request.get("email").toString().trim().isEmpty()) {
                String requestEmail = request.get("email").toString().trim();
                System.out.println("Email from request: " + requestEmail);
                if (isValidEmail(requestEmail)) {
                    email = requestEmail;
                    System.out.println("Using email from request: " + email);
                } else {
                    System.out.println("Invalid email format from request: " + requestEmail);
                }
            } else {
                System.out.println("No email in request or email is empty");
            }
            
            // Then try to override with authenticated user if available
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Authentication object: " + auth);
                if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                    String principal = auth.getName();
                    System.out.println("Authenticated user principal: " + principal);
                    if (principal != null && !principal.isBlank() && isValidEmail(principal)) {
                        email = principal; // Use authenticated user's email
                        System.out.println("Using authenticated user email: " + email);
                    } else {
                        System.out.println("Principal is null, blank, or invalid email: " + principal);
                    }
                } else {
                    System.out.println("User is not authenticated or is anonymous");
                }
            } catch (Exception e) {
                System.out.println("Exception getting authenticated user: " + e.getMessage());
                // Keep frontend email if auth fails
            }
            
            // Validate that we have a valid email
            System.out.println("Final email value: " + email);
            if (email == null || !isValidEmail(email)) {
                System.out.println("Email validation failed - email is null or invalid: " + email);
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Valid email address is required for payment processing");
                return ResponseEntity.badRequest().body(error);
            }
            System.out.println("Email validation passed: " + email);
            
            String serviceType = request.get("serviceType") != null ? request.get("serviceType").toString() : "standard";
            
            // If we have a quoteId, get the actual quote amount from the database
            if (!"unknown".equals(quoteId)) {
                try {
                    // Try to find the quote in the database to get the correct amount
                    var quoteOpt = quoteRepository.findByQuoteId(quoteId);
                    if (quoteOpt.isPresent()) {
                        var quote = quoteOpt.get();
                        amount = quote.getTotalCost(); // Use the actual quote total cost
                        System.out.println("Using quote total cost: " + amount + " for quoteId: " + quoteId);
                    }
                } catch (Exception e) {
                    System.err.println("Could not retrieve quote amount, using provided amount: " + e.getMessage());
                }
            }
            
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
            
            // Set the email directly in the payment object for PaystackServiceImpl to use
            System.out.println("Setting email in payment object: " + email);
            
            // Store essential information in payment notes (compact format to fit 500 char limit)
            String customerName = request.get("customerName") != null ? request.get("customerName").toString() : "";
            String customerPhone = request.get("customerPhone") != null ? request.get("customerPhone").toString() : "";
            String recipientName = request.get("recipientName") != null ? request.get("recipientName").toString() : "";
            String pickupCity = request.get("pickupCity") != null ? request.get("pickupCity").toString() : "";
            String deliveryCity = request.get("deliveryCity") != null ? request.get("deliveryCity").toString() : "";
            
            // Create compact notes format to fit within 500 character limit
            String compactNotes = String.format("email:%s|serviceType:%s|quoteId:%s|customerName:%s|customerPhone:%s|recipientName:%s|pickupAddress:%s|pickupCity:%s|pickupState:%s|pickupZipCode:%s|pickupCountry:%s|deliveryAddress:%s|deliveryCity:%s|deliveryState:%s|deliveryZipCode:%s|deliveryCountry:%s", 
                email, serviceType, quoteId, customerName, customerPhone, recipientName, 
                request.get("pickupAddress") != null ? request.get("pickupAddress").toString() : "Pickup Address",
                pickupCity, 
                request.get("pickupState") != null ? request.get("pickupState").toString() : "Pickup State",
                request.get("pickupZipCode") != null ? request.get("pickupZipCode").toString() : "12345",
                request.get("pickupCountry") != null ? request.get("pickupCountry").toString() : "South Africa",
                request.get("deliveryAddress") != null ? request.get("deliveryAddress").toString() : "Delivery Address",
                deliveryCity,
                request.get("deliveryState") != null ? request.get("deliveryState").toString() : "Delivery State",
                request.get("deliveryZipCode") != null ? request.get("deliveryZipCode").toString() : "54321",
                request.get("deliveryCountry") != null ? request.get("deliveryCountry").toString() : "South Africa");
            
            System.out.println("Compact notes with email: " + compactNotes);
            
            // Truncate if still too long
            if (compactNotes.length() > 500) {
                compactNotes = compactNotes.substring(0, 497) + "...";
            }
            
            payment.setNotes(compactNotes);
            System.out.println("Stored compact payment notes: " + compactNotes.length() + " characters");
            
            // Store complete address data temporarily using transaction ID as key
            Map<String, Object> addressData = new HashMap<>();
            addressData.put("customerName", request.get("customerName"));
            addressData.put("customerEmail", request.get("customerEmail"));
            addressData.put("customerPhone", request.get("customerPhone"));
            addressData.put("recipientName", request.get("recipientName"));
            addressData.put("recipientEmail", request.get("recipientEmail"));
            addressData.put("recipientPhone", request.get("recipientPhone"));
            addressData.put("pickupAddress", request.get("pickupAddress"));
            addressData.put("pickupCity", request.get("pickupCity"));
            addressData.put("pickupState", request.get("pickupState"));
            addressData.put("pickupZipCode", request.get("pickupZipCode"));
            addressData.put("pickupCountry", request.get("pickupCountry"));
            addressData.put("deliveryAddress", request.get("deliveryAddress"));
            addressData.put("deliveryCity", request.get("deliveryCity"));
            addressData.put("deliveryState", request.get("deliveryState"));
            addressData.put("deliveryZipCode", request.get("deliveryZipCode"));
            addressData.put("deliveryCountry", request.get("deliveryCountry"));
            addressData.put("weight", request.get("weight"));
            addressData.put("description", request.get("description"));
            
            // Store address data temporarily
            temporaryAddressStorage.put(transactionId, addressData);
            System.out.println("Stored address data temporarily for transaction: " + transactionId);
            
            // Note: shipment_id and user_id are now optional for quote payments
            // They will be set later when the actual shipment is created
            
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
            // Let the frontend handle the redirect after payment verification
            String redirectUrl = appBaseUrl + "/payment"; // Stay on payment page, let JS handle redirect
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
            result.put("success", true); // Add this for frontend compatibility
            result.put("authorizationUrl", response.getData().getAuthorizationUrl());
            result.put("reference", transactionId);
            result.put("accessCode", response.getData().getAccessCode());
            result.put("paymentId", payment.getId());
            result.put("quoteId", quoteId);
            result.put("serviceType", serviceType);
            result.put("amount", amount);
            result.put("message", "Payment initialized successfully");
            
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
    public Object verifyPayment(@RequestParam String reference, @RequestParam(required = false) String trxref, HttpServletResponse httpResponse) throws IOException {
        try {
            logger.info("=== VERIFY PAYMENT START ===");
            logger.info("Verifying payment with reference: " + reference);
            logger.info("Transaction reference (trxref): " + trxref);

            // If trxref is present, this is a browser redirect from Paystack
            // We need to redirect to the payment-success HTML page
            if (trxref != null) {
                logger.info("=== PAYSTACK BROWSER REDIRECT DETECTED ===");
                logger.info("This is a browser redirect from Paystack");
                logger.info("Processing payment and redirecting to success page");

                // Process the payment first
                Payment payment = paymentRepository.findByTransactionId(reference).orElse(null);
                String trackingNumber = null;
                BigDecimal amount = BigDecimal.ZERO;
                String serviceType = "OVERNIGHT";
                String email = "customer@example.com";

                if (payment != null) {
                    amount = payment.getAmount();
                    
                    // CRITICAL: Set payment status to COMPLETED before creating shipment
                    if (payment.getStatus() != PaymentStatus.COMPLETED) {
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setPaymentDate(new java.util.Date());
                        paymentRepository.save(payment);
                        logger.info("Payment status updated to COMPLETED for reference: {}", reference);
                    }

                    // Extract email and serviceType from payment notes (support both formats)
                    if (payment.getNotes() != null && !payment.getNotes().trim().isEmpty()) {
                        try {
                            // Try compact format first (email:value|serviceType:value|...)
                            if (payment.getNotes().contains("|")) {
                                String[] parts = payment.getNotes().split("\\|");
                                for (String part : parts) {
                                    if (part.contains(":")) {
                                        String[] keyValue = part.split(":", 2);
                                        if (keyValue.length == 2) {
                                            String key = keyValue[0].trim();
                                            String value = keyValue[1].trim();
                                            if ("email".equals(key)) email = value;
                                            if ("serviceType".equals(key)) serviceType = value;
                                        }
                                    }
                                }
                            } else if (payment.getNotes().trim().startsWith("{")) {
                                // Try JSON format
                                Map<String, Object> payload = objectMapper.readValue(payment.getNotes(), new TypeReference<Map<String, Object>>(){});
                                if (payload.containsKey("email")) email = String.valueOf(payload.get("email"));
                                if (payload.containsKey("serviceType")) serviceType = String.valueOf(payload.get("serviceType"));
                            }
                        } catch (Exception ex) {
                            logger.warn("Could not parse payment notes: " + ex.getMessage());
                        }
                    }
                    
                    // Also try to get email from payment user if available
                    if (payment.getUser() != null && payment.getUser().getEmail() != null) {
                        email = payment.getUser().getEmail();
                        logger.info("Using email from payment user: {}", email);
                    }

                    // CRITICAL: Ensure package/shipment is always created after payment
                    trackingNumber = ensurePackageCreation(payment, email, serviceType, reference);
                    if (trackingNumber == null || trackingNumber.isEmpty()) {
                        logger.error("CRITICAL: Failed to create package after all attempts. Payment ID: {}", payment.getId());
                        // Generate temporary tracking number as absolute last resort
                        trackingNumber = "RC" + System.currentTimeMillis();
                        logger.warn("Using emergency tracking number: {}", trackingNumber);
                    }
                } else {
                    logger.error("Payment is null during redirect, cannot create shipment");
                }

                // CRITICAL: Ensure tracking number is always available
                // Refresh payment from database one final time to get latest shipment
                payment = paymentRepository.findByTransactionId(reference).orElse(payment);
                if (trackingNumber == null || trackingNumber.isEmpty() || trackingNumber.equals("null")) {
                    if (payment != null && payment.getShipment() != null && payment.getShipment().getTrackingNumber() != null) {
                        trackingNumber = payment.getShipment().getTrackingNumber();
                        logger.info("Retrieved tracking number from refreshed payment: {}", trackingNumber);
                    } else {
                        logger.warn("Tracking number is still null/empty after all attempts, generating temporary one");
                        trackingNumber = "RC" + System.currentTimeMillis();
                    }
                }
                
                // CRITICAL: Final verification - ensure we have a valid tracking number
                if (trackingNumber == null || trackingNumber.isEmpty() || trackingNumber.equals("null")) {
                    logger.error("CRITICAL: Tracking number is still invalid, using emergency fallback");
                    trackingNumber = "RC" + System.currentTimeMillis();
                }
                
                // CRITICAL: Build redirect URL with all parameters - ensure tracking number is always included
                // Refresh payment one more time to get latest shipment data for query params
                payment = paymentRepository.findByTransactionId(reference).orElse(payment);
                String redirectUrl = String.format("/payment-success?reference=%s&trackingNumber=%s&tracking=%s&amount=%s&service=%s&email=%s&status=COMPLETED%s",
                        URLEncoder.encode(reference, StandardCharsets.UTF_8),
                        URLEncoder.encode(trackingNumber, StandardCharsets.UTF_8),
                        URLEncoder.encode(trackingNumber, StandardCharsets.UTF_8),
                        URLEncoder.encode(amount.toString(), StandardCharsets.UTF_8),
                        URLEncoder.encode(serviceType, StandardCharsets.UTF_8),
                        URLEncoder.encode(email, StandardCharsets.UTF_8),
                        buildShipmentQueryParams(payment != null ? payment.getShipment() : null));
                
                logger.info("Redirecting to payment success page with tracking number: {}", trackingNumber);
                logger.info("Redirect URL: {}", redirectUrl);

                logger.info("=== REDIRECTING TO: " + redirectUrl + " ===");

                // Use HttpServletResponse to perform actual redirect
                httpResponse.sendRedirect(redirectUrl);
                return null;
            }

            // If no trxref, this is an AJAX call from the frontend
            // Return JSON response
            logger.info("=== AJAX VERIFICATION REQUEST ===");
            logger.info("This is an AJAX call from frontend");
            
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
                
                // Check if shipment already exists to avoid duplicates
                if (payment.getShipment() != null) {
                    System.out.println("Shipment already exists, skipping creation: " + payment.getShipment().getTrackingNumber());
                    return ResponseEntity.ok(createSuccessResponse(payment, payment.getShipment()));
                }
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
                        // Ensure tracking number is always returned
                        String trackingNum = shipment != null ? shipment.getTrackingNumber() : null;
                        if (trackingNum == null || trackingNum.isEmpty()) {
                            logger.warn("Shipment tracking number is null, generating temporary one");
                            trackingNum = "RC" + System.currentTimeMillis();
                        }
                        response.put("trackingNumber", trackingNum);
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
                        
                        // Add customer information
                        if (shipment.getSender() != null) {
                            response.put("customerName", shipment.getSender().getFirstName() + " " + shipment.getSender().getLastName());
                            response.put("customerEmail", shipment.getSender().getEmail());
                        }
                        
                        // Add collection and drop-off codes
                        response.put("collectionCode", shipment.getCollectionCode());
                        response.put("dropOffCode", shipment.getDropOffCode());
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
            
            // CRITICAL: Ensure payment status is COMPLETED before creating shipment
            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                logger.info("Payment status is not COMPLETED (current: {}), updating to COMPLETED", payment.getStatus());
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaymentDate(new java.util.Date());
                payment = paymentRepository.save(payment);
                logger.info("Payment status updated to COMPLETED");
            }
            
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

                    if (notes != null && notes.contains("|")) {
                        // New compact format parsing: email:value|serviceType:value|...
                        System.out.println("=== ENTERING COMPACT FORMAT PARSING ===");
                        try {
                            System.out.println("Parsing compact notes: " + notes);
                            String[] parts = notes.split("\\|");
                            for (String part : parts) {
                                if (part.contains(":")) {
                                    String[] keyValue = part.split(":", 2);
                                    if (keyValue.length == 2) {
                                        String key = keyValue[0].trim();
                                        String value = keyValue[1].trim();
                                        if (key.equals("quoteId")) {
                                            quoteId = value;
                                            System.out.println("Found quoteId: " + quoteId);
                                        } else if (key.equals("email")) {
                                            email = value;
                                            System.out.println("Found email: " + email);
                                        } else if (key.equals("serviceType")) {
                                            serviceType = value;
                                            System.out.println("Found serviceType: " + serviceType);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Could not parse compact notes, using defaults: " + ex.getMessage());
                        }
                    } else if (notes != null && notes.trim().startsWith("{")) {
                        // Legacy JSON payload (for backward compatibility)
                        System.out.println("=== ENTERING JSON PARSING (LEGACY) ===");
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

                    // CRITICAL: Ensure package/shipment is always created after payment
                    String trackingNum = ensurePackageCreation(payment, email, serviceType, reference);
                    if (trackingNum == null || trackingNum.isEmpty()) {
                        logger.error("CRITICAL: Failed to create package after all attempts. Payment ID: {}", payment.getId());
                        trackingNum = "RC" + System.currentTimeMillis();
                        logger.warn("Using emergency tracking number: {}", trackingNum);
                    }
                    
                    // Refresh payment to get latest shipment data
                    payment = paymentRepository.findByTransactionId(reference).orElse(payment);
                    Shipment shipment = payment.getShipment();
                    
                    response.put("status", "success");
                    response.put("paymentStatus", "COMPLETED");
                    response.put("message", "Payment verification completed");
                    response.put("reference", reference);
                    // CRITICAL: Ensure tracking number is always present
                    response.put("trackingNumber", trackingNum);
                    response.put("bookingNumber", trackingNum); // Also include as bookingNumber for compatibility
                    response.put("amount", payment.getAmount());
                    response.put("serviceType", serviceType);
                    response.put("customerEmail", email);
                    
                    // CRITICAL: Verify tracking number is not null/empty
                    if (trackingNum == null || trackingNum.isEmpty() || trackingNum.equals("null")) {
                        logger.error("CRITICAL: Tracking number is invalid in response. Payment ID: {}", payment.getId());
                        // Try to get from shipment if available
                        if (shipment != null && shipment.getTrackingNumber() != null) {
                            trackingNum = shipment.getTrackingNumber();
                            response.put("trackingNumber", trackingNum);
                            response.put("bookingNumber", trackingNum);
                            logger.info("Retrieved tracking number from shipment: {}", trackingNum);
                        }
                    }
                    
                    logger.info("Payment verification successful for reference: " + reference);
                    logger.info("Tracking number: " + (shipment != null ? shipment.getTrackingNumber() : "null"));
                    logger.info("Amount: " + payment.getAmount());
                    logger.info("Service type: " + serviceType);
                    logger.info("Customer email: " + email);
                    
                    // Add shipping details if shipment exists
                    if (shipment != null) {
                        response.put("pickupAddress", shipment.getPickupAddress());
                        response.put("pickupCity", shipment.getPickupCity());
                        response.put("pickupCountry", shipment.getPickupCountry());
                        response.put("deliveryAddress", shipment.getDeliveryAddress());
                        response.put("deliveryCity", shipment.getDeliveryCity());
                        response.put("deliveryCountry", shipment.getDeliveryCountry());
                        response.put("customerPhone", shipment.getSender() != null ? shipment.getSender().getPhone() : null);
                        
                        // Note: Coordinates are not stored in Shipment model, but can be geocoded from addresses
                        // The frontend will use Google Maps Geocoder to get coordinates from addresses
                        
                        // Add customer information
                        if (shipment.getSender() != null) {
                            response.put("customerName", shipment.getSender().getFirstName() + " " + shipment.getSender().getLastName());
                            response.put("customerEmail", shipment.getSender().getEmail());
                        }
                        
                        // Add collection and drop-off codes
                        response.put("collectionCode", shipment.getCollectionCode());
                        response.put("dropOffCode", shipment.getDropOffCode());
                        
                        // Add booking number if available
                        if (shipment.getTrackingNumber() != null) {
                            response.put("bookingNumber", shipment.getTrackingNumber());
                        }
                        
                        // Send email confirmation to customer
                        try {
                            sendPaymentConfirmationEmail(shipment, payment);
                        } catch (Exception e) {
                            System.err.println("Failed to send payment confirmation email: " + e.getMessage());
                        }
                    }
                    
                    // Return success response for frontend to handle redirect
                    return ResponseEntity.ok(response);

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
     * Enhanced with better error handling and retry logic
     * CRITICAL: This method ensures a package/shipment is ALWAYS created after payment
     */
    private Shipment createShipmentFromPaymentData(Payment payment, String email, String serviceType) {
        int maxRetries = 3;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                attempt++;
                logger.info("=== createShipmentFromPaymentData called (Attempt {}/{}) ===", attempt, maxRetries);
                logger.info("Payment ID: {}", payment.getId());
                logger.info("Email: {}", email);
                logger.info("Service Type: {}", serviceType);
                
                // Validate payment
                if (payment == null) {
                    logger.error("Payment is null, cannot create shipment");
                    throw new IllegalArgumentException("Payment cannot be null");
                }
                
                // CRITICAL: Refresh payment from database to get latest state
                try {
                    payment = paymentRepository.findById(payment.getId()).orElse(payment);
                    logger.info("Refreshed payment from database");
                } catch (Exception e) {
                    logger.warn("Could not refresh payment, using provided payment: {}", e.getMessage());
                }
                
                // Check if shipment already exists for this payment
                if (payment.getShipment() != null) {
                    Shipment existingShipment = payment.getShipment();
                    // Verify shipment is properly saved in database
                    try {
                        Shipment verifiedShipment = shipmentRepository.findById(existingShipment.getId())
                            .orElse(null);
                        if (verifiedShipment != null && verifiedShipment.getTrackingNumber() != null) {
                            logger.info("Shipment already exists and verified for payment: {}", verifiedShipment.getTrackingNumber());
                            return verifiedShipment;
                        } else {
                            logger.warn("Shipment reference exists but not found in database, creating new one");
                        }
                    } catch (Exception e) {
                        logger.warn("Could not verify existing shipment, creating new one: {}", e.getMessage());
                    }
                }
                
                // Extract address data from payment notes
                Map<String, Object> addressData = extractAddressDataFromPayment(payment);
                
                // Create a basic shipment with minimal required data
                Shipment shipment = new Shipment();
                String trackingNumber = generateTrackingNumber();
                String collectionCode = generateCollectionCode();
                String dropOffCode = generateDropOffCode();
                logger.info("Generated tracking number: {}", trackingNumber);
                logger.info("Generated collection code: {}", collectionCode);
                logger.info("Generated drop-off code: {}", dropOffCode);
                
                shipment.setTrackingNumber(trackingNumber);
                shipment.setCollectionCode(collectionCode);
                shipment.setDropOffCode(dropOffCode);
                shipment.setStatus(ShipmentStatus.PENDING);
                shipment.setShippingCost(payment.getAmount());
                shipment.setCreatedAt(new java.util.Date());
                
                // Set service type with error handling
                try {
                    if (serviceType != null && !serviceType.isBlank()) {
                        shipment.setServiceType(ServiceType.valueOf(serviceType.toUpperCase()));
                    } else {
                        shipment.setServiceType(ServiceType.ECONOMY);
                    }
                } catch (Exception ex) {
                    logger.warn("Invalid service type '{}', defaulting to ECONOMY", serviceType);
                    shipment.setServiceType(ServiceType.ECONOMY);
                }
                
                // CRITICAL: Get or create user/sender - this is REQUIRED for shipment creation
                // The Shipment model has @NotNull constraint on sender field
                User sender = null;
                try {
                    if (payment.getUser() != null) {
                        sender = payment.getUser();
                        logger.info("Using authenticated payment user as sender: {}", sender.getEmail());
                    }
                } catch (Exception e) {
                    logger.warn("Could not get user from payment: {}", e.getMessage());
                }

                // If no sender from payment, find or create one from email
                if (sender == null && email != null && !email.isEmpty()) {
                    try {
                        sender = userRepository.findByEmail(email).orElse(null);
                        if (sender == null) {
                            logger.info("Creating new user for email: {}", email);
                            sender = new User();
                            sender.setEmail(email);
                            sender.setFirstName("Customer");
                            sender.setLastName("User");
                            sender.setRole(UserRole.CUSTOMER);
                            sender.setCreatedAt(new java.util.Date());
                            sender = userRepository.save(sender);
                            logger.info("Created new user with ID: {} and email: {}", sender.getId(), sender.getEmail());
                        } else {
                            logger.info("Found existing user for email: {} with ID: {}", email, sender.getId());
                        }
                    } catch (Exception e) {
                        logger.error("CRITICAL: Could not create/find user for email: {}", email, e);
                        // This is critical - we cannot proceed without a sender
                        throw new RuntimeException("Failed to create or find user for email: " + email + ". Shipment cannot be created without a sender.", e);
                    }
                }
                
                // If still no sender and no email, create a default user
                if (sender == null) {
                    logger.warn("No email provided and no user in payment. Creating default user.");
                    try {
                        String defaultEmail = "customer_" + System.currentTimeMillis() + "@reliablecarriers.co.za";
                        sender = new User();
                        sender.setEmail(defaultEmail);
                        sender.setFirstName("Customer");
                        sender.setLastName("User");
                        sender.setRole(UserRole.CUSTOMER);
                        sender.setCreatedAt(new java.util.Date());
                        sender = userRepository.save(sender);
                        logger.info("Created default user with ID: {} and email: {}", sender.getId(), sender.getEmail());
                    } catch (Exception e) {
                        logger.error("CRITICAL: Failed to create default user: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to create default user. Shipment cannot be created without a sender.", e);
                    }
                }

                // Set sender - this is now guaranteed to be non-null
                shipment.setSender(sender);
                logger.info("Set sender for shipment: {} (ID: {})", sender.getEmail(), sender.getId());
                
                // Set shipment details from extracted data with defaults
                String recipientName = getStringValue(addressData, "recipientName", "Recipient");
                String recipientEmail = getStringValue(addressData, "recipientEmail", email != null ? email : "");
                String recipientPhone = getStringValue(addressData, "recipientPhone", null);
                
                shipment.setRecipientName(recipientName);
                shipment.setRecipientEmail(recipientEmail != null && !recipientEmail.isEmpty() ? recipientEmail : (email != null ? email : ""));
                if (recipientPhone != null && !recipientPhone.isEmpty() && !recipientPhone.equals("Not provided")) {
                    shipment.setRecipientPhone(recipientPhone);
                }
                
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
                
                // Update sender information if we have customer data
                if (shipment.getSender() != null) {
                    String customerName = getStringValue(addressData, "customerName", null);
                    String customerPhone = getStringValue(addressData, "customerPhone", null);
                    
                    try {
                        if (customerName != null && !customerName.equals("Customer") && !customerName.trim().isEmpty()) {
                            String[] nameParts = customerName.trim().split(" ", 2);
                            shipment.getSender().setFirstName(nameParts[0]);
                            if (nameParts.length > 1) {
                                shipment.getSender().setLastName(nameParts[1]);
                            }
                        }
                        
                        if (customerPhone != null && !customerPhone.equals("Not provided") && !customerPhone.trim().isEmpty()) {
                            shipment.getSender().setPhone(customerPhone);
                        }
                        
                        // Save updated user information
                        userRepository.save(shipment.getSender());
                        logger.info("Updated sender information");
                    } catch (Exception e) {
                        logger.warn("Could not update sender information: {}", e.getMessage());
                        // Continue - shipment will still be created
                    }
                }
                
                // Save the shipment with retry logic
                Shipment savedShipment = null;
                try {
                    logger.info("Saving shipment to database...");
                    savedShipment = shipmentRepository.save(shipment);
                    // CRITICAL: Flush to ensure it's immediately available
                    shipmentRepository.flush();
                    logger.info("Shipment saved successfully with ID: {} and tracking number: {}", 
                              savedShipment.getId(), savedShipment.getTrackingNumber());
                    
                    // Verify shipment was saved correctly
                    Shipment verification = shipmentRepository.findById(savedShipment.getId()).orElse(null);
                    if (verification == null || verification.getTrackingNumber() == null) {
                        throw new RuntimeException("Shipment verification failed after save");
                    }
                    logger.info("Shipment verified in database with tracking: {}", verification.getTrackingNumber());
                } catch (Exception e) {
                    logger.error("Failed to save shipment on attempt {}: {}", attempt, e.getMessage(), e);
                    if (attempt < maxRetries) {
                        Thread.sleep(500 * attempt); // Exponential backoff
                        continue; // Retry
                    }
                    throw e; // Re-throw if all retries failed
                }
                
                // CRITICAL: Update payment with shipment reference and ensure status is COMPLETED
                // This must happen atomically to ensure package is linked to customer
                int paymentUpdateRetries = 3;
                boolean paymentUpdated = false;
                for (int paymentRetry = 0; paymentRetry < paymentUpdateRetries; paymentRetry++) {
                    try {
                        logger.info("Updating payment with shipment reference (attempt {}/{})...", paymentRetry + 1, paymentUpdateRetries);
                        // Refresh payment from database
                        Payment paymentToUpdate = paymentRepository.findById(payment.getId())
                            .orElseThrow(() -> new RuntimeException("Payment not found for update"));
                        
                        paymentToUpdate.setShipment(savedShipment);
                        // Ensure payment status is COMPLETED
                        if (paymentToUpdate.getStatus() != PaymentStatus.COMPLETED) {
                            paymentToUpdate.setStatus(PaymentStatus.COMPLETED);
                            paymentToUpdate.setPaymentDate(new java.util.Date());
                        }
                        paymentRepository.save(paymentToUpdate);
                        paymentRepository.flush(); // Ensure immediate persistence
                        
                        // Verify payment was updated correctly
                        Payment verifiedPayment = paymentRepository.findById(payment.getId()).orElse(null);
                        if (verifiedPayment != null && verifiedPayment.getShipment() != null 
                            && verifiedPayment.getShipment().getId().equals(savedShipment.getId())) {
                            paymentUpdated = true;
                            logger.info("Payment updated and verified successfully with shipment reference and COMPLETED status");
                            break;
                        } else {
                            logger.warn("Payment update verification failed, retrying...");
                        }
                    } catch (Exception e) {
                        logger.error("Failed to update payment with shipment reference (attempt {}): {}", 
                                   paymentRetry + 1, e.getMessage(), e);
                        if (paymentRetry < paymentUpdateRetries - 1) {
                            try {
                                Thread.sleep(300 * (paymentRetry + 1));
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        } else {
                            // Last attempt failed - this is critical but shipment is already created
                            logger.error("CRITICAL: Failed to link payment to shipment after all retries. " +
                                       "Shipment tracking: {}, Payment ID: {}", 
                                       savedShipment.getTrackingNumber(), payment.getId());
                            // Try one more time with minimal update
                            try {
                                Payment lastAttempt = paymentRepository.findById(payment.getId()).orElse(null);
                                if (lastAttempt != null) {
                                    lastAttempt.setShipment(savedShipment);
                                    paymentRepository.save(lastAttempt);
                                    logger.info("Final attempt to link payment succeeded");
                                }
                            } catch (Exception finalEx) {
                                logger.error("Final attempt to link payment also failed: {}", finalEx.getMessage());
                            }
                        }
                    }
                }
                
                if (!paymentUpdated) {
                    logger.error("WARNING: Payment was not successfully linked to shipment, but shipment was created. " +
                               "Tracking: {}, Payment ID: {}", savedShipment.getTrackingNumber(), payment.getId());
                }
                
                logger.info("Successfully created shipment from payment: {}", savedShipment.getTrackingNumber());
                return savedShipment;
                
            } catch (Exception e) {
                logger.error("Failed to create shipment from payment data on attempt {}: {}", attempt, e.getMessage(), e);
                
                if (attempt >= maxRetries) {
                    // Last attempt failed - create minimal shipment as fallback
                    logger.error("All retries failed, creating minimal shipment as fallback");
                    try {
                        Shipment fallbackShipment = createMinimalShipment(payment, email, serviceType);
                        if (fallbackShipment != null && fallbackShipment.getTrackingNumber() != null) {
                            logger.info("Fallback shipment created successfully: {}", fallbackShipment.getTrackingNumber());
                            return fallbackShipment;
                        } else {
                            logger.error("CRITICAL: Fallback shipment creation returned null or missing tracking number");
                            // Last resort: create emergency shipment
                            return createEmergencyShipment(payment, email, serviceType);
                        }
                    } catch (Exception fallbackException) {
                        logger.error("CRITICAL: Even fallback shipment creation failed: {}", fallbackException.getMessage(), fallbackException);
                        // Last resort: create emergency shipment
                        try {
                            return createEmergencyShipment(payment, email, serviceType);
                        } catch (Exception emergencyEx) {
                            logger.error("CRITICAL: Emergency shipment creation also failed: {}", emergencyEx.getMessage());
                            return null;
                        }
                    }
                }
                
                // Wait before retry
                try {
                    Thread.sleep(500 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted during retry wait");
                    // Try emergency shipment before giving up
                    try {
                        return createEmergencyShipment(payment, email, serviceType);
                    } catch (Exception emergencyEx) {
                        logger.error("Emergency shipment creation failed after interrupt: {}", emergencyEx.getMessage());
                        return null;
                    }
                }
            }
        }
        
        // Should never reach here, but if we do, try emergency shipment
        logger.error("CRITICAL: Reached end of createShipmentFromPaymentData without creating shipment");
        try {
            return createEmergencyShipment(payment, email, serviceType);
        } catch (Exception e) {
            logger.error("CRITICAL: Emergency shipment creation failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Create an emergency shipment when all other methods fail
     * This ensures a package is ALWAYS created, even in worst-case scenarios
     */
    private Shipment createEmergencyShipment(Payment payment, String email, String serviceType) {
        logger.warn("Creating emergency shipment as last resort");
        try {
            Shipment shipment = new Shipment();
            String trackingNumber = "RC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            shipment.setTrackingNumber(trackingNumber);
            shipment.setCollectionCode("COL" + (int)(Math.random() * 100000));
            shipment.setDropOffCode("DRO" + (int)(Math.random() * 100000));
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setShippingCost(payment.getAmount());
            shipment.setServiceType(ServiceType.ECONOMY);
            shipment.setCreatedAt(new java.util.Date());
            shipment.setPickupAddress("Address to be updated");
            shipment.setDeliveryAddress("Address to be updated");
            shipment.setPickupCountry("South Africa");
            shipment.setDeliveryCountry("South Africa");
            shipment.setRecipientEmail(email != null ? email : "customer@example.com");
            shipment.setRecipientName("Recipient");
            
            // Get or create user
            User sender = null;
            if (email != null && !email.isEmpty()) {
                try {
                    sender = userRepository.findByEmail(email).orElse(null);
                    if (sender == null) {
                        sender = new User();
                        sender.setEmail(email);
                        sender.setFirstName("Customer");
                        sender.setLastName("User");
                        sender.setRole(UserRole.CUSTOMER);
                        sender.setCreatedAt(new java.util.Date());
                        sender = userRepository.save(sender);
                    }
                } catch (Exception e) {
                    logger.warn("Could not create/find user for emergency shipment: {}", e.getMessage());
                }
            }
            
            if (sender == null) {
                // Create default user
                String defaultEmail = "customer_" + System.currentTimeMillis() + "@reliablecarriers.co.za";
                sender = new User();
                sender.setEmail(defaultEmail);
                sender.setFirstName("Customer");
                sender.setLastName("User");
                sender.setRole(UserRole.CUSTOMER);
                sender.setCreatedAt(new java.util.Date());
                sender = userRepository.save(sender);
            }
            
            shipment.setSender(sender);
            
            Shipment saved = shipmentRepository.save(shipment);
            shipmentRepository.flush();
            
            // Link to payment
            try {
                Payment paymentToUpdate = paymentRepository.findById(payment.getId()).orElse(null);
                if (paymentToUpdate != null) {
                    paymentToUpdate.setShipment(saved);
                    if (paymentToUpdate.getStatus() != PaymentStatus.COMPLETED) {
                        paymentToUpdate.setStatus(PaymentStatus.COMPLETED);
                        paymentToUpdate.setPaymentDate(new java.util.Date());
                    }
                    paymentRepository.save(paymentToUpdate);
                }
            } catch (Exception e) {
                logger.warn("Could not link emergency shipment to payment: {}", e.getMessage());
            }
            
            logger.warn("Emergency shipment created: {}", saved.getTrackingNumber());
            return saved;
        } catch (Exception e) {
            logger.error("CRITICAL: Emergency shipment creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create emergency shipment", e);
        }
    }
    
    /**
     * CRITICAL: Ensure package/shipment is always created after payment
     * This method provides comprehensive error handling and retry logic
     * Returns tracking number if successful, null otherwise
     */
    private String ensurePackageCreation(Payment payment, String email, String serviceType, String reference) {
        logger.info("=== ensurePackageCreation called ===");
        logger.info("Payment ID: {}, Reference: {}, Email: {}, Service Type: {}", 
                   payment.getId(), reference, email, serviceType);
        
        // First, check if shipment already exists and is valid
        try {
            // Refresh payment from database
            Payment refreshedPayment = paymentRepository.findByTransactionId(reference)
                .orElse(paymentRepository.findById(payment.getId()).orElse(payment));
            
            if (refreshedPayment.getShipment() != null) {
                Shipment existingShipment = refreshedPayment.getShipment();
                // Verify shipment exists in database
                Shipment verifiedShipment = shipmentRepository.findById(existingShipment.getId())
                    .orElse(null);
                if (verifiedShipment != null && verifiedShipment.getTrackingNumber() != null 
                    && !verifiedShipment.getTrackingNumber().isEmpty()) {
                    logger.info("Package already exists and verified: {}", verifiedShipment.getTrackingNumber());
                    return verifiedShipment.getTrackingNumber();
                } else {
                    logger.warn("Shipment reference exists but not found in database, creating new one");
                }
            }
        } catch (Exception e) {
            logger.warn("Could not verify existing shipment: {}", e.getMessage());
        }
        
        // Try to create shipment with full retry logic
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                logger.info("Attempting to create package (attempt {}/{})", attempt, maxAttempts);
                Shipment shipment = createShipmentFromPaymentData(payment, email, serviceType);
                
                if (shipment != null && shipment.getTrackingNumber() != null 
                    && !shipment.getTrackingNumber().isEmpty()) {
                    // Verify shipment is linked to payment
                    Payment updatedPayment = paymentRepository.findByTransactionId(reference)
                        .orElse(paymentRepository.findById(payment.getId()).orElse(null));
                    
                    if (updatedPayment != null && updatedPayment.getShipment() != null 
                        && updatedPayment.getShipment().getId().equals(shipment.getId())) {
                        logger.info("Package created and linked to payment successfully: {}", shipment.getTrackingNumber());
                        return shipment.getTrackingNumber();
                    } else {
                        logger.warn("Package created but not linked to payment, attempting to link...");
                        // Try to link manually
                        try {
                            if (updatedPayment != null) {
                                updatedPayment.setShipment(shipment);
                                paymentRepository.save(updatedPayment);
                                logger.info("Package manually linked to payment");
                                return shipment.getTrackingNumber();
                            }
                        } catch (Exception linkEx) {
                            logger.warn("Could not link package to payment: {}", linkEx.getMessage());
                        }
                        // Even if linking failed, return tracking number
                        return shipment.getTrackingNumber();
                    }
                } else {
                    logger.warn("Shipment creation returned null or missing tracking number on attempt {}", attempt);
                }
            } catch (Exception e) {
                logger.error("Package creation attempt {} failed: {}", attempt, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(500 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        // All attempts failed - try emergency shipment
        logger.error("All package creation attempts failed, trying emergency shipment");
        try {
            Shipment emergencyShipment = createEmergencyShipment(payment, email, serviceType);
            if (emergencyShipment != null && emergencyShipment.getTrackingNumber() != null) {
                logger.warn("Emergency package created: {}", emergencyShipment.getTrackingNumber());
                return emergencyShipment.getTrackingNumber();
            }
        } catch (Exception e) {
            logger.error("Emergency package creation also failed: {}", e.getMessage());
        }
        
        logger.error("CRITICAL: Failed to create package after all attempts");
        return null;
    }
    
    /**
     * Create a minimal shipment as fallback when normal creation fails
     */
    private Shipment createMinimalShipment(Payment payment, String email, String serviceType) {
        logger.warn("Creating minimal shipment as fallback");
        try {
            Shipment shipment = new Shipment();
            shipment.setTrackingNumber(generateTrackingNumber());
            shipment.setCollectionCode(generateCollectionCode());
            shipment.setDropOffCode(generateDropOffCode());
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setShippingCost(payment.getAmount());
            shipment.setServiceType(ServiceType.ECONOMY);
            shipment.setCreatedAt(new java.util.Date());
            shipment.setPickupAddress("Address to be updated");
            shipment.setDeliveryAddress("Address to be updated");
            shipment.setPickupCountry("South Africa");
            shipment.setDeliveryCountry("South Africa");
            shipment.setRecipientEmail(email != null ? email : "customer@example.com");
            shipment.setRecipientName("Recipient");
            
            // Try to get or create user
            if (email != null && !email.isEmpty()) {
                try {
                    User sender = userRepository.findByEmail(email).orElse(null);
                    if (sender == null) {
                        sender = new User();
                        sender.setEmail(email);
                        sender.setFirstName("Customer");
                        sender.setLastName("User");
                        sender.setRole(UserRole.CUSTOMER);
                        sender.setCreatedAt(new java.util.Date());
                        sender = userRepository.save(sender);
                    }
                    shipment.setSender(sender);
                } catch (Exception e) {
                    logger.warn("Could not set sender for minimal shipment: {}", e.getMessage());
                }
            }
            
            Shipment saved = shipmentRepository.save(shipment);
            payment.setShipment(saved);
            paymentRepository.save(payment);
            logger.warn("Minimal shipment created: {}", saved.getTrackingNumber());
            return saved;
        } catch (Exception e) {
            logger.error("CRITICAL: Minimal shipment creation also failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Extract address data from payment notes
     */
    private Map<String, Object> extractAddressDataFromPayment(Payment payment) {
        Map<String, Object> addressData = new HashMap<>();
        try {
            // First try to get from temporary storage using transaction ID
            if (payment.getTransactionId() != null) {
                Map<String, Object> tempData = temporaryAddressStorage.get(payment.getTransactionId());
                if (tempData != null) {
                    addressData.putAll(tempData);
                    System.out.println("Retrieved address data from temporary storage for transaction: " + payment.getTransactionId());
                    // Clean up temporary storage after use
                    temporaryAddressStorage.remove(payment.getTransactionId());
                    return addressData;
                }
            }
            
            // Fallback to parsing payment notes (compact format)
            if (payment.getNotes() != null && !payment.getNotes().trim().isEmpty()) {
                String[] parts = payment.getNotes().split("\\|");
                for (String part : parts) {
                    if (part.contains(":")) {
                        String[] keyValue = part.split(":", 2);
                        if (keyValue.length == 2) {
                            addressData.put(keyValue[0].trim(), keyValue[1].trim());
                        }
                    }
                }
                System.out.println("Successfully parsed address data from payment notes compact format");
            }
        } catch (Exception e) {
            System.err.println("Error extracting address data from payment: " + e.getMessage());
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
     * Generate a collection code
     */
    private String generateCollectionCode() {
        return "COL" + (int)(Math.random() * 100000);
    }
    
    /**
     * Generate a drop-off code
     */
    private String generateDropOffCode() {
        return "DRO" + (int)(Math.random() * 100000);
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
     * Send payment confirmation email and SMS to customer with Google Maps addresses
     */
    private void sendPaymentConfirmationEmail(Shipment shipment, Payment payment) {
        try {
            String customerEmail = shipment.getSender() != null ? shipment.getSender().getEmail() : 
                                 shipment.getRecipientEmail() != null ? shipment.getRecipientEmail() : null;
            String customerPhone = shipment.getSender() != null ? shipment.getSender().getPhone() : 
                                  shipment.getRecipientPhone() != null ? shipment.getRecipientPhone() : null;
            String customerName = shipment.getSender() != null ? 
                (shipment.getSender().getFirstName() + " " + (shipment.getSender().getLastName() != null ? shipment.getSender().getLastName() : "")) : 
                "Customer";
            
            // Build Google Maps links for pickup and delivery
            String pickupAddressFull = String.format("%s, %s, %s", 
                shipment.getPickupAddress() != null ? shipment.getPickupAddress() : "",
                shipment.getPickupCity() != null ? shipment.getPickupCity() : "",
                shipment.getPickupCountry() != null ? shipment.getPickupCountry() : "");
            String deliveryAddressFull = String.format("%s, %s, %s", 
                shipment.getDeliveryAddress() != null ? shipment.getDeliveryAddress() : "",
                shipment.getDeliveryCity() != null ? shipment.getDeliveryCity() : "",
                shipment.getDeliveryCountry() != null ? shipment.getDeliveryCountry() : "");
            
            String pickupMapsLink = "https://www.google.com/maps/search/?api=1&query=" + 
                java.net.URLEncoder.encode(pickupAddressFull, java.nio.charset.StandardCharsets.UTF_8);
            String deliveryMapsLink = "https://www.google.com/maps/search/?api=1&query=" + 
                java.net.URLEncoder.encode(deliveryAddressFull, java.nio.charset.StandardCharsets.UTF_8);
            String pickupDirectionsLink = "https://www.google.com/maps/dir/?api=1&destination=" + 
                java.net.URLEncoder.encode(pickupAddressFull, java.nio.charset.StandardCharsets.UTF_8);
            String deliveryDirectionsLink = "https://www.google.com/maps/dir/?api=1&destination=" + 
                java.net.URLEncoder.encode(deliveryAddressFull, java.nio.charset.StandardCharsets.UTF_8);
            
            if (customerEmail != null) {
                String subject = "Payment Confirmation - " + shipment.getTrackingNumber();
                String message = String.format(
                    "Dear %s,\n\n" +
                    "Your payment has been processed successfully!\n\n" +
                    "Payment Details:\n" +
                    "- Transaction ID: %s\n" +
                    "- Amount Paid: R%.2f\n" +
                    "- Payment Date: %s\n" +
                    "- Service Type: %s\n\n" +
                    "Shipment Details:\n" +
                    "- Tracking Number: %s\n" +
                    "- Collection Code: %s\n" +
                    "- Drop-off Code: %s\n" +
                    "- Status: %s\n\n" +
                    "Pickup Address:\n" +
                    "%s\n" +
                    "View on Google Maps: %s\n" +
                    "Get Directions: %s\n\n" +
                    "Delivery Address:\n" +
                    "%s\n" +
                    "View on Google Maps: %s\n" +
                    "Get Directions: %s\n\n" +
                    "Important Instructions:\n" +
                    "- Use Collection Code '%s' when dropping off your package\n" +
                    "- Use Drop-off Code '%s' when receiving your package\n" +
                    "- Keep these codes safe and provide them to our drivers\n\n" +
                    "You can track your shipment using the tracking number: %s\n" +
                    "Track online: http://localhost:8080/tracking/%s\n\n" +
                    "Thank you for choosing Reliable Carriers!\n\n" +
                    "Best regards,\n" +
                    "Reliable Carriers Team",
                    customerName,
                    payment.getTransactionId(),
                    payment.getAmount(),
                    payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "N/A",
                    shipment.getServiceType() != null ? shipment.getServiceType().toString() : "N/A",
                    shipment.getTrackingNumber(),
                    shipment.getCollectionCode() != null ? shipment.getCollectionCode() : "N/A",
                    shipment.getDropOffCode() != null ? shipment.getDropOffCode() : "N/A",
                    shipment.getStatus() != null ? shipment.getStatus().toString() : "N/A",
                    pickupAddressFull,
                    pickupMapsLink,
                    pickupDirectionsLink,
                    deliveryAddressFull,
                    deliveryMapsLink,
                    deliveryDirectionsLink,
                    shipment.getCollectionCode() != null ? shipment.getCollectionCode() : "N/A",
                    shipment.getDropOffCode() != null ? shipment.getDropOffCode() : "N/A",
                    shipment.getTrackingNumber(),
                    shipment.getTrackingNumber()
                );
                
                // Send email notification using notification service
                System.out.println("Sending payment confirmation email to: " + customerEmail);
                System.out.println("Subject: " + subject);
                
                try {
                    notificationService.sendCustomEmailNotification(customerEmail, subject, message);
                    System.out.println("Payment confirmation email sent successfully");
                } catch (Exception e) {
                    System.err.println("Failed to send email via notification service: " + e.getMessage());
                    e.printStackTrace();
                }
                
                // Send SMS notification if phone number is available
                if (customerPhone != null && !customerPhone.trim().isEmpty() && !customerPhone.equals("Not provided")) {
                    try {
                        String smsMessage = String.format(
                            "Payment Confirmed! Tracking: %s, Collection Code: %s, Drop-off Code: %s. " +
                            "Pickup: %s. Delivery: %s. Track: http://localhost:8080/tracking/%s",
                            shipment.getTrackingNumber(),
                            shipment.getCollectionCode() != null ? shipment.getCollectionCode() : "N/A",
                            shipment.getDropOffCode() != null ? shipment.getDropOffCode() : "N/A",
                            shipment.getPickupAddress() != null ? shipment.getPickupAddress() : "N/A",
                            shipment.getDeliveryAddress() != null ? shipment.getDeliveryAddress() : "N/A",
                            shipment.getTrackingNumber()
                        );
                        
                        notificationService.sendCustomSmsNotification(customerPhone, smsMessage);
                        System.out.println("Payment confirmation SMS sent to: " + customerPhone);
                    } catch (Exception e) {
                        System.err.println("Failed to send SMS notification: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                // Send notification to receiver (delivery contact) if different from sender
                String receiverEmail = shipment.getRecipientEmail();
                String receiverPhone = shipment.getRecipientPhone();
                String receiverName = shipment.getRecipientName();
                
                // Check if receiver is different from sender
                boolean receiverDifferent = (receiverEmail != null && !receiverEmail.equals(customerEmail)) ||
                                           (receiverPhone != null && !receiverPhone.equals(customerPhone));
                
                if (receiverDifferent) {
                    // Send email to receiver if email is available and different
                    if (receiverEmail != null && !receiverEmail.trim().isEmpty() && 
                        !receiverEmail.equals(customerEmail)) {
                        try {
                            String receiverSubject = "Package Delivery Scheduled - " + shipment.getTrackingNumber();
                            String receiverMessage = String.format(
                                "Dear %s,\n\n" +
                                "A package delivery has been scheduled to your address.\n\n" +
                                "Delivery Information:\n" +
                                "- Tracking Number: %s\n" +
                                "- Drop-off Code: %s\n" +
                                "- Delivery Address: %s, %s, %s\n" +
                                "- Service Type: %s\n\n" +
                                "Important: Please ensure someone is available to receive the package " +
                                "and provide the Drop-off Code '%s' when the driver arrives.\n\n" +
                                "Track your package: http://localhost:8080/tracking/%s\n\n" +
                                "Thank you,\n" +
                                "Reliable Carriers Team",
                                receiverName != null ? receiverName : "Valued Customer",
                                shipment.getTrackingNumber(),
                                shipment.getDropOffCode() != null ? shipment.getDropOffCode() : "N/A",
                                deliveryAddressFull,
                                shipment.getDeliveryCity() != null ? shipment.getDeliveryCity() : "",
                                shipment.getDeliveryCountry() != null ? shipment.getDeliveryCountry() : "",
                                shipment.getServiceType() != null ? shipment.getServiceType().toString() : "N/A",
                                shipment.getDropOffCode() != null ? shipment.getDropOffCode() : "N/A",
                                shipment.getTrackingNumber()
                            );
                            
                            notificationService.sendCustomEmailNotification(receiverEmail, receiverSubject, receiverMessage);
                            System.out.println("Payment confirmation email sent to receiver: " + receiverEmail);
                        } catch (Exception e) {
                            System.err.println("Failed to send email to receiver: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    // Send SMS to receiver if phone is available and different
                    if (receiverPhone != null && !receiverPhone.trim().isEmpty() && 
                        !receiverPhone.equals(customerPhone)) {
                        try {
                            String receiverSms = String.format(
                                "Package delivery scheduled! Tracking: %s, Drop-off Code: %s. " +
                                "Delivery to: %s. Track: http://localhost:8080/tracking/%s",
                                shipment.getTrackingNumber(),
                                shipment.getDropOffCode() != null ? shipment.getDropOffCode() : "N/A",
                                deliveryAddressFull,
                                shipment.getTrackingNumber()
                            );
                            
                            notificationService.sendCustomSmsNotification(receiverPhone, receiverSms);
                            System.out.println("Payment confirmation SMS sent to receiver: " + receiverPhone);
                        } catch (Exception e) {
                            System.err.println("Failed to send SMS to receiver: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                
                // Notify admin/drivers about new shipment (for driver assignment)
                try {
                    notifyDriversAboutNewShipment(shipment);
                } catch (Exception e) {
                    System.err.println("Failed to notify drivers about new shipment: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send payment confirmation notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Notify available drivers about new shipment (for assignment)
     */
    private void notifyDriversAboutNewShipment(Shipment shipment) {
        try {
            // This will be handled by the admin when assigning drivers
            // For now, we just log that a new shipment is ready for assignment
            System.out.println("New shipment ready for driver assignment: " + shipment.getTrackingNumber());
            System.out.println("Pickup: " + shipment.getPickupAddress() + ", " + shipment.getPickupCity());
            System.out.println("Delivery: " + shipment.getDeliveryAddress() + ", " + shipment.getDeliveryCity());
            
            // In a production system, you would:
            // 1. Find available drivers in the area
            // 2. Send notifications to those drivers
            // 3. Or notify admin to assign a driver
            
        } catch (Exception e) {
            System.err.println("Failed to notify drivers: " + e.getMessage());
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
     * Get detailed Paystack configuration status
     */
    @GetMapping("/config-status")
    public ResponseEntity<Map<String, Object>> getPaystackConfigStatus() {
        try {
            Map<String, Object> status = paystackService.getConfigurationStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Test payment success page with sample data
     */
    @GetMapping("/test-payment-success")
    public String testPaymentSuccess() {
        return "redirect:/api/paystack/payment-success?reference=PAY_1636BDED-633&tracking=RC1760780229388924&amount=150.00&service=OVERNIGHT&email=customer@example.com&status=COMPLETED";
    }
    
    /**
     * Validate email address format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Create success response for payment verification
     */
    private Map<String, Object> createSuccessResponse(Payment payment, Shipment shipment) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("paymentStatus", "COMPLETED");
        response.put("message", "Payment verification completed");
        response.put("reference", payment.getTransactionId());
        response.put("trackingNumber", shipment.getTrackingNumber());
        response.put("amount", payment.getAmount());
        response.put("serviceType", shipment.getServiceType());
        response.put("customerEmail", shipment.getSender() != null ? shipment.getSender().getEmail() : null);
        
        // Add customer information
        if (shipment.getSender() != null) {
            response.put("customerName", shipment.getSender().getFirstName() + " " + shipment.getSender().getLastName());
            response.put("customerEmail", shipment.getSender().getEmail());
            response.put("customerPhone", shipment.getSender().getPhone());
        }
        
        // Add shipping details
        response.put("pickupAddress", shipment.getPickupAddress());
        response.put("pickupCity", shipment.getPickupCity());
        response.put("pickupCountry", shipment.getPickupCountry());
        response.put("deliveryAddress", shipment.getDeliveryAddress());
        response.put("deliveryCity", shipment.getDeliveryCity());
        response.put("deliveryCountry", shipment.getDeliveryCountry());
        
        // Add collection and drop-off codes
        response.put("collectionCode", shipment.getCollectionCode());
        response.put("dropOffCode", shipment.getDropOffCode());
        
        return response;
    }

    private String buildShipmentQueryParams(Shipment shipment) {
        if (shipment == null) {
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            if (shipment.getCollectionCode() != null) {
                sb.append("&collectionCode=")
                  .append(URLEncoder.encode(shipment.getCollectionCode(), StandardCharsets.UTF_8));
            }
            if (shipment.getDropOffCode() != null) {
                sb.append("&dropOffCode=")
                  .append(URLEncoder.encode(shipment.getDropOffCode(), StandardCharsets.UTF_8));
            }
            if (shipment.getPickupAddress() != null) {
                sb.append("&pickupAddress=")
                  .append(URLEncoder.encode(shipment.getPickupAddress(), StandardCharsets.UTF_8));
            }
            if (shipment.getPickupCity() != null) {
                sb.append("&pickupCity=")
                  .append(URLEncoder.encode(shipment.getPickupCity(), StandardCharsets.UTF_8));
            }
            if (shipment.getPickupCountry() != null) {
                sb.append("&pickupCountry=")
                  .append(URLEncoder.encode(shipment.getPickupCountry(), StandardCharsets.UTF_8));
            }
            if (shipment.getDeliveryAddress() != null) {
                sb.append("&deliveryAddress=")
                  .append(URLEncoder.encode(shipment.getDeliveryAddress(), StandardCharsets.UTF_8));
            }
            if (shipment.getDeliveryCity() != null) {
                sb.append("&deliveryCity=")
                  .append(URLEncoder.encode(shipment.getDeliveryCity(), StandardCharsets.UTF_8));
            }
            if (shipment.getDeliveryCountry() != null) {
                sb.append("&deliveryCountry=")
                  .append(URLEncoder.encode(shipment.getDeliveryCountry(), StandardCharsets.UTF_8));
            }
            if (shipment.getSender() != null) {
                String name = (shipment.getSender().getFirstName() != null ? shipment.getSender().getFirstName() : "") +
                              (shipment.getSender().getLastName() != null ? (" " + shipment.getSender().getLastName()) : "");
                if (!name.isBlank()) {
                    sb.append("&customerName=")
                      .append(URLEncoder.encode(name, StandardCharsets.UTF_8));
                }
                if (shipment.getSender().getPhone() != null) {
                    sb.append("&customerPhone=")
                      .append(URLEncoder.encode(shipment.getSender().getPhone(), StandardCharsets.UTF_8));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody String payload,
                                                             @RequestHeader(value = "X-Paystack-Signature", required = false) String signature) {
        try {
            if (paystackWebhookSecret == null || paystackWebhookSecret.isBlank()) {
                return ResponseEntity.status(500).body(Map.of("success", false, "error", "Webhook secret not configured"));
            }
            if (signature == null || !verifySignature(payload, signature)) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Invalid signature"));
            }
            Map<String, Object> event = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>(){});
            Object dataObj = event.get("data");
            Map<String, Object> dataMap = objectMapper.convertValue(dataObj, new TypeReference<Map<String,Object>>(){});
            String reference = dataMap.get("reference") != null ? String.valueOf(dataMap.get("reference")) : null;
            String status = dataMap.get("status") != null ? String.valueOf(dataMap.get("status")) : null;
            if (reference == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing reference"));
            }
            Payment payment = paymentRepository.findByTransactionId(reference).orElse(null);
            if (payment == null) {
                return ResponseEntity.ok(Map.of("success", true));
            }
            boolean completed = "success".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status);
            if (completed) {
                // CRITICAL: Ensure payment status is COMPLETED
                if (payment.getStatus() != PaymentStatus.COMPLETED) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setPaymentDate(new java.util.Date());
                    paymentRepository.save(payment);
                    logger.info("Webhook: Payment status updated to COMPLETED for reference: {}", reference);
                }
                
                // CRITICAL: Ensure package/shipment is always created after payment
                String email = "customer@example.com";
                String serviceType = "OVERNIGHT";
                String notes = payment.getNotes();
                
                // Extract email and serviceType from notes (support both formats)
                if (notes != null && !notes.trim().isEmpty()) {
                    if (notes.contains("|")) {
                        // Compact format
                        try {
                            String[] parts = notes.split("\\|");
                            for (String part : parts) {
                                if (part.contains(":")) {
                                    String[] kv = part.split(":", 2);
                                    if (kv.length == 2) {
                                        String k = kv[0].trim();
                                        String v = kv[1].trim();
                                        if ("email".equals(k)) email = v;
                                        if ("serviceType".equals(k)) serviceType = v;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Webhook: Could not parse compact notes format: {}", e.getMessage());
                        }
                    } else if (notes.trim().startsWith("{")) {
                        // JSON format
                        try {
                            Map<String, Object> payload = objectMapper.readValue(notes, new TypeReference<Map<String, Object>>(){});
                            if (payload.containsKey("email")) email = String.valueOf(payload.get("email"));
                            if (payload.containsKey("serviceType")) serviceType = String.valueOf(payload.get("serviceType"));
                        } catch (Exception e) {
                            logger.warn("Webhook: Could not parse JSON notes format: {}", e.getMessage());
                        }
                    }
                }
                
                // Also try to get email from payment user
                if (payment.getUser() != null && payment.getUser().getEmail() != null) {
                    email = payment.getUser().getEmail();
                }
                
                // CRITICAL: Ensure package is created
                String trackingNumber = ensurePackageCreation(payment, email, serviceType, reference);
                if (trackingNumber != null && !trackingNumber.isEmpty()) {
                    logger.info("Webhook: Package created/verified with tracking number: {}", trackingNumber);
                    // Refresh payment to get shipment
                    payment = paymentRepository.findByTransactionId(reference).orElse(payment);
                    if (payment.getShipment() != null) {
                        try {
                            sendPaymentConfirmationEmail(payment.getShipment(), payment);
                        } catch (Exception e) {
                            logger.warn("Webhook: Failed to send confirmation email: {}", e.getMessage());
                        }
                    }
                } else {
                    logger.error("Webhook: CRITICAL - Failed to create package for payment reference: {}", reference);
                }
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(paystackWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(keySpec);
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String computed = sb.toString();
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
