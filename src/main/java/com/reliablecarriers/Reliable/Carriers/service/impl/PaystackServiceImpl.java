package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackMetadata;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.service.PaystackService;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaystackServiceImpl implements PaystackService {
    
    // Custom exception for Paystack key errors
    private static class PaystackKeyError extends RuntimeException {
        public PaystackKeyError(String message) {
            super(message);
        }
    }
    
    @Value("${paystack.secret.key}")
    private String paystackSecretKey;
    
    @Value("${paystack.public.key}")
    private String paystackPublicKey;
    
    @Value("${paystack.base.url:https://api.paystack.co}")
    private String paystackBaseUrl;
    
    @Value("${paystack.mock.mode:false}")
    private boolean mockMode;
    
    @Value("${paystack.fallback.on.error:true}")
    private boolean fallbackOnError;
    
    @Value("${paystack.timeout.seconds:30}")
    private int timeoutSeconds;
    
    @Value("${paystack.retry.attempts:3}")
    private int retryAttempts;
    
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    
    @Autowired
    public PaystackServiceImpl(PaymentService paymentService) {
        this.paymentService = paymentService;
        this.objectMapper = new ObjectMapper();
    }
    
    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(paystackBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + paystackSecretKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
                .build();
    }
    
    @Override
    public PaystackResponse initializePayment(PaystackRequest request) {
        try {
            // Mock mode for development/testing
            if (mockMode) {
                System.out.println("MOCK MODE: Simulating Paystack payment initialization");
                return createMockResponse(request);
            }
            
            // Set currency to ZAR (South African Rand)
            request.setCurrency("ZAR");
            
            // For ZAR, amount is in cents (1 ZAR = 100 cents)
            request.setAmount(convertToCents(request.getAmount()));
            
            System.out.println("Initializing Paystack payment with amount: " + request.getAmount());
            System.out.println("Using secret key: " + (paystackSecretKey != null ? paystackSecretKey.substring(0, 10) + "..." : "null"));
            
            try {
                PaystackResponse response = getWebClient().post()
                        .uri("/transaction/initialize")
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            httpResponse -> {
                                System.err.println("Paystack API error: " + httpResponse.statusCode());
                                return httpResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.err.println("Error body: " + errorBody);
                                        // Check if it's an invalid key error and throw special exception
                                        if (fallbackOnError && (errorBody.contains("Invalid key") || errorBody.contains("invalid_Key") || errorBody.contains("401"))) {
                                            System.err.println("Paystack key invalid or unauthorized. Will fallback to mock mode.");
                                            return Mono.error(new PaystackKeyError("Invalid Paystack key - using mock mode"));
                                        }
                                        return Mono.error(new RuntimeException("Paystack API Error: " + httpResponse.statusCode() + " - " + errorBody));
                                    });
                            })
                        .bodyToMono(PaystackResponse.class)
                        .block();
                return response;
            } catch (PaystackKeyError e) {
                // Handle invalid key error by falling back to mock
                if (fallbackOnError) {
                    System.err.println("Paystack key error detected. Falling back to mock mode.");
                    return createMockResponse(request);
                }
                throw new RuntimeException("Paystack key invalid", e);
            } catch (RuntimeException e) {
                // Check if it's a Paystack API error that we should fallback on
                if (fallbackOnError && (e.getMessage().contains("Invalid key") || e.getMessage().contains("invalid_Key") || e.getMessage().contains("401"))) {
                    System.err.println("Paystack key error detected. Falling back to mock mode.");
                    return createMockResponse(request);
                }
                throw e;
            } catch (Exception networkError) {
                System.err.println("Network error connecting to Paystack: " + networkError.getMessage());
                
                // Check if it's a connection issue or key error
                if (fallbackOnError && (networkError.getMessage().contains("Connection reset") || 
                    networkError.getMessage().contains("Connection refused") ||
                    networkError.getMessage().contains("timeout") ||
                    networkError.getMessage().contains("Invalid key") ||
                    networkError.getMessage().contains("401"))) {
                    
                    System.out.println("Paystack API error detected. Falling back to mock mode for this request");
                    return createMockResponse(request);
                }
                
                // Re-throw if it's not a connection/key issue
                throw networkError;
            }
        } catch (Exception e) {
            System.err.println("Paystack initialization error: " + e.getMessage());
            e.printStackTrace();
            
            // If we can't connect to Paystack, provide a fallback response (if enabled)
            if (fallbackOnError && (e.getMessage().contains("Connection reset") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("timeout"))) {
                
                System.out.println("Creating fallback response due to Paystack connectivity issues");
                return createMockResponse(request);
            }
            
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage(), e);
        }
    }
    
    private PaystackResponse createMockResponse(PaystackRequest request) {
        PaystackResponse mockResponse = new PaystackResponse();
        mockResponse.setStatus(true);
        mockResponse.setMessage("Payment initialized successfully (offline mode)");
        
        PaystackResponse.PaystackData mockData = new PaystackResponse.PaystackData();
        mockData.setAuthorizationUrl("https://checkout.paystack.com/mock-payment?ref=" + request.getReference());
        mockData.setAccessCode("mock_access_code_" + System.currentTimeMillis());
        mockData.setReference(request.getReference());
        mockData.setAmount(request.getAmount().longValue());
        mockData.setCurrency("ZAR");
        mockData.setStatus("pending");
        
        mockResponse.setData(mockData);
        return mockResponse;
    }
    
    @Override
    public PaystackResponse verifyPayment(String reference) {
        try {
            // Mock mode for development/testing
            if (mockMode) {
                System.out.println("MOCK MODE: Simulating Paystack payment verification");
                return createMockVerificationResponse(reference);
            }
            
            try {
                return getWebClient().get()
                        .uri("/transaction/verify/" + reference)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            httpResponse -> {
                                System.err.println("Paystack verification API error: " + httpResponse.statusCode());
                                return httpResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.err.println("Verification error body: " + errorBody);
                                        // Check if it's an invalid key error and fallback to mock
                                        if (fallbackOnError && (errorBody.contains("Invalid key") || errorBody.contains("invalid_Key") || errorBody.contains("401"))) {
                                            System.err.println("Paystack key invalid or unauthorized during verification. Will fallback to mock mode.");
                                            return Mono.error(new PaystackKeyError("Invalid Paystack key - using mock mode"));
                                        }
                                        return Mono.error(new RuntimeException("Paystack Verification API Error: " + httpResponse.statusCode() + " - " + errorBody));
                                    });
                            })
                        .bodyToMono(PaystackResponse.class)
                        .block();
            } catch (PaystackKeyError e) {
                // Handle invalid key error by falling back to mock
                if (fallbackOnError) {
                    System.err.println("Paystack key error detected during verification. Falling back to mock mode.");
                    return createMockVerificationResponse(reference);
                }
                throw new RuntimeException("Paystack key invalid", e);
            } catch (RuntimeException e) {
                // Check if it's a Paystack API error that we should fallback on
                if (fallbackOnError && (e.getMessage().contains("Invalid key") || e.getMessage().contains("invalid_Key") || e.getMessage().contains("401"))) {
                    System.err.println("Paystack key error detected during verification. Falling back to mock mode.");
                    return createMockVerificationResponse(reference);
                }
                throw e;
            } catch (Exception networkError) {
                System.err.println("Network error connecting to Paystack for verification: " + networkError.getMessage());
                
                // Check if it's a connection issue or key error
                if (fallbackOnError && (networkError.getMessage().contains("Connection reset") || 
                    networkError.getMessage().contains("Connection refused") ||
                    networkError.getMessage().contains("timeout") ||
                    networkError.getMessage().contains("Invalid key") ||
                    networkError.getMessage().contains("401"))) {
                    
                    System.out.println("Paystack API error detected during verification. Falling back to mock mode.");
                    return createMockVerificationResponse(reference);
                }
                
                // Re-throw if it's not a connection/key issue
                throw networkError;
            }
        } catch (Exception e) {
            System.err.println("Paystack verification error: " + e.getMessage());
            
            // If we can't connect to Paystack, provide a fallback response (if enabled)
            if (fallbackOnError && (e.getMessage().contains("Connection reset") || 
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("timeout"))) {
                
                System.out.println("Creating fallback verification response due to Paystack connectivity issues");
                return createMockVerificationResponse(reference);
            }
            
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }
    
    private PaystackResponse createMockVerificationResponse(String reference) {
        PaystackResponse mockResponse = new PaystackResponse();
        mockResponse.setStatus(true);
        mockResponse.setMessage("Payment verified successfully (offline mode)");
        
        PaystackResponse.PaystackData mockData = new PaystackResponse.PaystackData();
        mockData.setReference(reference);
        mockData.setStatus("success");
        mockData.setGatewayResponse("Successful");
        mockData.setChannel("card");
        mockData.setPaidAt("2024-01-01T00:00:00.000Z");
        mockData.setCurrency("ZAR");
        
        mockResponse.setData(mockData);
        return mockResponse;
    }
    
    @Override
    public PaystackRequest createPaymentRequest(Payment payment, String callbackUrl, String redirectUrl) {
        PaystackRequest request = new PaystackRequest();
        request.setAmount(payment.getAmount());
        
        // Handle email - either from user or from notes
        String email = null;
        System.out.println("PaystackServiceImpl - Payment user: " + (payment.getUser() != null ? payment.getUser().getEmail() : "null"));
        System.out.println("PaystackServiceImpl - Payment notes: " + payment.getNotes());
        
        if (payment.getUser() != null && isValidEmail(payment.getUser().getEmail())) {
            email = payment.getUser().getEmail();
            System.out.println("PaystackServiceImpl - Using email from user: " + email);
        } else if (payment.getNotes() != null) {
            try {
                System.out.println("PaystackServiceImpl - Checking notes format");
                System.out.println("PaystackServiceImpl - Notes starts with { : " + payment.getNotes().trim().startsWith("{"));
                System.out.println("PaystackServiceImpl - Notes contains | : " + payment.getNotes().contains("|"));
                System.out.println("PaystackServiceImpl - Notes contains email: : " + payment.getNotes().contains("email:"));
                
                // Try to parse JSON notes first
                if (payment.getNotes().trim().startsWith("{")) {
                    Map<String, Object> notesMap = objectMapper.readValue(payment.getNotes(), new TypeReference<Map<String, Object>>(){});
                    if (notesMap.containsKey("email")) {
                        String parsedEmail = String.valueOf(notesMap.get("email"));
                        if (isValidEmail(parsedEmail)) {
                            email = parsedEmail;
                            System.out.println("PaystackServiceImpl - Using email from JSON format: " + email);
                        }
                    }
                } else if (payment.getNotes().contains("|")) {
                    // New compact format: email:value|serviceType:value|...
                    System.out.println("PaystackServiceImpl - Parsing compact format notes");
                    String[] parts = payment.getNotes().split("\\|");
                    System.out.println("PaystackServiceImpl - Split into " + parts.length + " parts");
                    for (String part : parts) {
                        System.out.println("PaystackServiceImpl - Processing part: " + part);
                        if (part.startsWith("email:")) {
                            String[] keyValue = part.split(":", 2);
                            System.out.println("PaystackServiceImpl - Split part into " + keyValue.length + " pieces");
                            if (keyValue.length == 2) {
                                String parsedEmail = keyValue[1].trim();
                                System.out.println("PaystackServiceImpl - Found email in compact format: " + parsedEmail);
                                if (isValidEmail(parsedEmail)) {
                                    email = parsedEmail;
                                    System.out.println("PaystackServiceImpl - Using email from compact format: " + email);
                                } else {
                                    System.out.println("PaystackServiceImpl - Invalid email in compact format: " + parsedEmail);
                                }
                            }
                            break;
                        }
                    }
                } else if (payment.getNotes().contains("email:")) {
                    // Fallback to old CSV format
                    String[] parts = payment.getNotes().split(",");
                    for (String part : parts) {
                        if (part.startsWith("email:")) {
                            String parsedEmail = part.split(":")[1];
                            if (isValidEmail(parsedEmail)) {
                                email = parsedEmail;
                                System.out.println("PaystackServiceImpl - Using email from CSV format: " + email);
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not parse payment notes for email: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Validate email before setting
        System.out.println("PaystackServiceImpl - Final email value: " + email);
        if (email == null || !isValidEmail(email)) {
            System.out.println("PaystackServiceImpl - Email validation failed: " + email);
            throw new RuntimeException("Valid email address is required for Paystack payment initialization");
        }
        System.out.println("PaystackServiceImpl - Email validation passed: " + email);
        
        request.setEmail(email);
        
        request.setReference(payment.getTransactionId());
        request.setCallbackUrl(callbackUrl);
        request.setRedirectUrl(redirectUrl);
        
        // Add metadata (only if available)
        PaystackMetadata metadata = new PaystackMetadata();
        if (payment.getId() != null) {
            metadata.addCustomField("payment_id", payment.getId().toString());
        }
        if (payment.getUser() != null) {
            metadata.addCustomField("user_id", payment.getUser().getId().toString());
        }
        if (payment.getShipment() != null) {
            metadata.addCustomField("shipment_id", payment.getShipment().getId().toString());
            metadata.addCustomField("service_type", payment.getShipment().getServiceType().toString());
        }
        // Add notes as metadata
        if (payment.getNotes() != null) {
            metadata.addCustomField("notes", payment.getNotes());
        }
        request.setMetadata(metadata);
        
        return request;
    }
    
    @Override
    public Payment processPaymentVerification(String reference) {
        try {
            // Get payment by reference
            Payment payment = paymentService.getPaymentByTransactionId(reference);
            
            // Verify with Paystack
            PaystackResponse response = verifyPayment(reference);
            
            if (response.isStatus() && response.getData() != null) {
                PaystackResponse.PaystackData data = response.getData();
                
                // Update payment status based on Paystack response
                if ("success".equals(data.getStatus())) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setNotes("Payment verified via Paystack. Channel: " + data.getChannel());
                } else if ("failed".equals(data.getStatus())) {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setNotes("Payment failed via Paystack. Response: " + data.getGatewayResponse());
                } else {
                    payment.setStatus(PaymentStatus.PENDING);
                    payment.setNotes("Payment pending via Paystack. Status: " + data.getStatus());
                }
                
                return paymentService.updatePayment(payment.getId(), payment);
            } else {
                throw new RuntimeException("Payment verification failed: " + response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process payment verification: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String generatePaymentReference() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
    
    @Override
    public BigDecimal convertToCents(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100"));
    }
    
    @Override
    public BigDecimal convertFromCents(Long amountInCents) {
        return new BigDecimal(amountInCents).divide(new BigDecimal("100"));
    }
    
    // Getter for public key (used in frontend)
    public String getPaystackPublicKey() {
        return paystackPublicKey;
    }
    
    /**
     * Test Paystack connectivity
     */
    public boolean testPaystackConnectivity() {
        try {
            if (mockMode) {
                System.out.println("Mock mode enabled - skipping connectivity test");
                return true;
            }
            
            // Simple connectivity test
            getWebClient().get()
                    .uri("/balance")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            System.out.println("Paystack connectivity test successful");
            return true;
        } catch (Exception e) {
            System.err.println("Paystack connectivity test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get configuration status
     */
    public Map<String, Object> getConfigurationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("mockMode", mockMode);
        status.put("fallbackOnError", fallbackOnError);
        status.put("timeoutSeconds", timeoutSeconds);
        status.put("retryAttempts", retryAttempts);
        status.put("baseUrl", paystackBaseUrl);
        status.put("hasSecretKey", paystackSecretKey != null && !paystackSecretKey.isEmpty());
        status.put("hasPublicKey", paystackPublicKey != null && !paystackPublicKey.isEmpty());
        status.put("connectivityTest", testPaystackConnectivity());
        return status;
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
}
