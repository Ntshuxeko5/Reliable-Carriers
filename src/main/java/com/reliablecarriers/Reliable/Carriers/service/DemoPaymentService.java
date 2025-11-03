package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Demo Payment Service for client demonstrations
 * Provides realistic payment simulation without requiring real payment processing
 */
@Service
public class DemoPaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${demo.mode.enabled:true}")
    private boolean demoModeEnabled;

    @Value("${paystack.public.key:pk_test_demo}")
    private String paystackPublicKey;

    /**
     * Initialize payment for demo mode
     * Creates a realistic payment flow without actual charges
     */
    public PaystackResponse initializeDemoPayment(PaystackRequest request) {
        try {
            // Create demo payment response
            PaystackResponse response = new PaystackResponse();
            response.setStatus(true);
            response.setMessage("Demo payment initialized successfully");

            // Create demo data object
            PaystackResponse.PaystackData data = new PaystackResponse.PaystackData();
            data.setAuthorizationUrl(generateDemoAuthorizationUrl(request));
            data.setAccessCode("demo_access_" + UUID.randomUUID().toString().substring(0, 8));
            data.setReference(request.getReference());
            
            response.setData(data);
            
            return response;
        } catch (Exception e) {
            PaystackResponse errorResponse = new PaystackResponse();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Demo payment initialization failed: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Verify demo payment
     * Simulates successful payment verification for demo purposes
     */
    public PaystackResponse verifyDemoPayment(String reference) {
        try {
            PaystackResponse response = new PaystackResponse();
            response.setStatus(true);
            response.setMessage("Demo payment verified successfully");

            // Create demo verification data
            PaystackResponse.PaystackData data = new PaystackResponse.PaystackData();
            data.setReference(reference);
            data.setStatus("success");
            data.setGatewayResponse("Demo payment successful");
            
            // Find the payment by reference and update it
            Payment payment = paymentRepository.findByReference(reference).orElse(null);
            if (payment != null) {
                data.setAmount(payment.getAmount().multiply(new BigDecimal("100")).longValue()); // Convert to cents
                data.setCurrency("ZAR");
                // Note: Customer data would be set in a real implementation
                
                // Update payment status
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setUpdatedAt(new Date());
                paymentRepository.save(payment);
            }
            
            response.setData(data);
            return response;
        } catch (Exception e) {
            PaystackResponse errorResponse = new PaystackResponse();
            errorResponse.setStatus(false);
            errorResponse.setMessage("Demo payment verification failed: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Check if demo mode is enabled
     */
    public boolean isDemoMode() {
        return demoModeEnabled || paystackPublicKey.contains("demo") || paystackPublicKey.contains("test");
    }

    /**
     * Get demo configuration status
     */
    public Map<String, Object> getDemoConfigurationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("demoMode", isDemoMode());
        status.put("paystackConfigured", !paystackPublicKey.isEmpty() && !paystackPublicKey.contains("demo"));
        status.put("message", isDemoMode() ? "Running in demo mode - no real payments will be processed" : "Live payment mode");
        return status;
    }

    /**
     * Create demo payment request for testing
     */
    public PaystackRequest createDemoPaymentRequest(BigDecimal amount, String email, String reference) {
        PaystackRequest request = new PaystackRequest();
        request.setAmount(amount);
        request.setEmail(email);
        request.setReference(reference);
        request.setCurrency("ZAR");
        request.setCallbackUrl("http://localhost:8080/api/paystack/verify");
        return request;
    }

    // Helper methods
    private String generateDemoAuthorizationUrl(PaystackRequest request) {
        // Create a demo payment URL that simulates Paystack's payment page
        return String.format("http://localhost:8080/demo-payment?reference=%s&amount=%s&email=%s",
                request.getReference(),
                request.getAmount().toString(),
                request.getEmail());
    }

    @SuppressWarnings("unused")
    private Map<String, Object> createDemoCustomer(Payment payment) {
        Map<String, Object> customer = new HashMap<>();
        customer.put("id", "demo_customer_" + (payment.getUser() != null ? payment.getUser().getId() : "guest"));
        customer.put("email", payment.getUser() != null ? payment.getUser().getEmail() : "demo@example.com");
        customer.put("first_name", payment.getUser() != null ? payment.getUser().getFirstName() : "Demo");
        customer.put("last_name", payment.getUser() != null ? payment.getUser().getLastName() : "User");
        return customer;
    }

    /**
     * Generate demo payment reference
     */
    public String generateDemoReference() {
        return "DEMO_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Simulate payment processing delay for realistic demo
     */
    public void simulateProcessingDelay() {
        try {
            Thread.sleep(2000); // 2 second delay to simulate real payment processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
