package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentMethod;
import com.reliablecarriers.Reliable.Carriers.service.PaystackService;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/paystack")
@CrossOrigin(origins = "*")
public class PaystackController {
    
    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;
    
    private final PaystackService paystackService;
    private final PaymentService paymentService;
    private final AuthService authService;
    
    @Autowired
    public PaystackController(PaystackService paystackService, PaymentService paymentService, AuthService authService) {
        this.paystackService = paystackService;
        this.paymentService = paymentService;
        this.authService = authService;
    }
    
    /**
     * Initialize payment for a shipment
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializePayment(@RequestBody Map<String, Object> request) {
        try {
            // Extract data from request
            Long shipmentId = Long.valueOf(request.get("shipmentId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String email = request.get("email").toString();
            
            // Create payment record
            Payment payment = new Payment();
            payment.setTransactionId(paystackService.generatePaymentReference());
            payment.setAmount(amount);
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD); // Default for Paystack
            
            // Set user and shipment (you'll need to implement this based on your auth system)
            // For now, we'll create a basic payment record
            
            // Create Paystack request
            String callbackUrl = appBaseUrl + "/api/paystack/verify";
            PaystackRequest paystackRequest = paystackService.createPaymentRequest(payment, callbackUrl);
            
            // Initialize payment with Paystack
            PaystackResponse response = paystackService.initializePayment(paystackRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("authorizationUrl", response.getData().getAuthorizationUrl());
            result.put("reference", response.getData().getReference());
            result.put("accessCode", response.getData().getAccessCode());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Verify payment callback from Paystack
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestParam String reference) {
        try {
            Payment payment = paystackService.processPaymentVerification(reference);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("paymentStatus", payment.getStatus());
            result.put("message", "Payment verification completed");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
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
}
