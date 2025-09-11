package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

@Service
public class PaystackServiceImpl implements PaystackService {
    
    @Value("${paystack.secret.key}")
    private String paystackSecretKey;
    
    @Value("${paystack.public.key}")
    private String paystackPublicKey;
    
    @Value("${paystack.base.url:https://api.paystack.co}")
    private String paystackBaseUrl;
    
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
            // Convert amount to kobo
            request.setAmount(convertToKobo(request.getAmount()));
            
            return getWebClient().post()
                    .uri("/transaction/initialize")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaystackResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public PaystackResponse verifyPayment(String reference) {
        try {
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
    public PaystackRequest createPaymentRequest(Payment payment, String callbackUrl) {
        PaystackRequest request = new PaystackRequest();
        request.setAmount(payment.getAmount());
        request.setEmail(payment.getUser().getEmail());
        request.setReference(payment.getTransactionId());
        request.setCallbackUrl(callbackUrl);
        
        // Add metadata
        PaystackMetadata metadata = new PaystackMetadata();
        metadata.addCustomField("payment_id", payment.getId().toString());
        metadata.addCustomField("shipment_id", payment.getShipment().getId().toString());
        metadata.addCustomField("user_id", payment.getUser().getId().toString());
        metadata.addCustomField("service_type", payment.getShipment().getServiceType().toString());
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
    public BigDecimal convertToKobo(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100"));
    }
    
    @Override
    public BigDecimal convertFromKobo(Long amountInKobo) {
        return new BigDecimal(amountInKobo).divide(new BigDecimal("100"));
    }
    
    // Getter for public key (used in frontend)
    public String getPaystackPublicKey() {
        return paystackPublicKey;
    }
}
