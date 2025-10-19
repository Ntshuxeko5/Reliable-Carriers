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
import java.util.Map;
import java.util.UUID;

@Service
public class PaystackServiceImpl implements PaystackService {
    
    @Value("${paystack.secret.key}")
    private String paystackSecretKey;
    
    @Value("${paystack.public.key}")
    private String paystackPublicKey;
    
    @Value("${paystack.base.url:https://api.paystack.co}")
    private String paystackBaseUrl;
    
    @Value("${paystack.mock.mode:false}")
    private boolean mockMode;
    
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
                .build();
    }
    
    @Override
    public PaystackResponse initializePayment(PaystackRequest request) {
        try {
            // Mock mode for development/testing
            if (mockMode) {
                System.out.println("MOCK MODE: Simulating Paystack payment initialization");
                PaystackResponse mockResponse = new PaystackResponse();
                mockResponse.setStatus(true);
                mockResponse.setMessage("Mock payment initialized successfully");
                
                PaystackResponse.PaystackData mockData = new PaystackResponse.PaystackData();
                mockData.setAuthorizationUrl("https://checkout.paystack.com/mock-payment");
                mockData.setAccessCode("mock_access_code_" + System.currentTimeMillis());
                mockData.setReference(request.getReference());
                mockData.setAmount(request.getAmount().longValue());
                mockData.setCurrency("ZAR");
                mockData.setStatus("pending");
                
                mockResponse.setData(mockData);
                return mockResponse;
            }
            
            // Set currency to ZAR (South African Rand)
            request.setCurrency("ZAR");
            
            // For ZAR, amount is in cents (1 ZAR = 100 cents)
            request.setAmount(convertToCents(request.getAmount()));
            
            System.out.println("Initializing Paystack payment with amount: " + request.getAmount());
            System.out.println("Using secret key: " + (paystackSecretKey != null ? paystackSecretKey.substring(0, 10) + "..." : "null"));
            
            return getWebClient().post()
                    .uri("/transaction/initialize")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            System.err.println("Paystack API error: " + response.statusCode());
                            return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("Error body: " + errorBody);
                                    return Mono.error(new RuntimeException("Paystack API Error: " + response.statusCode() + " - " + errorBody));
                                });
                        })
                    .bodyToMono(PaystackResponse.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Paystack initialization error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PaystackResponse verifyPayment(String reference) {
        try {
            // Mock mode for development/testing
            if (mockMode) {
                System.out.println("MOCK MODE: Simulating Paystack payment verification");
                PaystackResponse mockResponse = new PaystackResponse();
                mockResponse.setStatus(true);
                mockResponse.setMessage("Mock payment verified successfully");
                
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
            
            return getWebClient().get()
                    .uri("/transaction/verify/" + reference)
                    .retrieve()
                    .bodyToMono(PaystackResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PaystackRequest createPaymentRequest(Payment payment, String callbackUrl, String redirectUrl) {
        PaystackRequest request = new PaystackRequest();
        request.setAmount(payment.getAmount());
        
        // Handle email - either from user or from notes
        String email = "customer@example.com";
        if (payment.getUser() != null) {
            email = payment.getUser().getEmail();
        } else if (payment.getNotes() != null) {
            try {
                // Try to parse JSON notes first
                if (payment.getNotes().trim().startsWith("{")) {
                    Map<String, Object> notesMap = objectMapper.readValue(payment.getNotes(), new TypeReference<Map<String, Object>>(){});
                    if (notesMap.containsKey("email")) {
                        email = String.valueOf(notesMap.get("email"));
                    }
                } else if (payment.getNotes().contains("email:")) {
                    // Fallback to old CSV format
                    String[] parts = payment.getNotes().split(",");
                    for (String part : parts) {
                        if (part.startsWith("email:")) {
                            email = part.split(":")[1];
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not parse payment notes for email: " + e.getMessage());
            }
        }
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
}
