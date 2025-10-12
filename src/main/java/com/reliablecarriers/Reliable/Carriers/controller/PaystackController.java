package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentMethod;
import com.reliablecarriers.Reliable.Carriers.service.PaystackService;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
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
    private final CustomerPackageService customerPackageService;
    
    @Autowired
    public PaystackController(PaystackService paystackService, PaymentService paymentService, CustomerPackageService customerPackageService) {
        this.paystackService = paystackService;
        this.paymentService = paymentService;
        this.customerPackageService = customerPackageService;
    }
    
    /**
     * Initialize payment for a shipment
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializePayment(@RequestBody Map<String, Object> request) {
        try {
            // Extract data from request
            String quoteId = request.get("shipmentId").toString(); // Using quoteId as shipmentId for now
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String email = request.get("email").toString();
            String serviceType = request.get("serviceType").toString();
            
            // Create payment record
            Payment payment = new Payment();
            payment.setTransactionId(paystackService.generatePaymentReference());
            payment.setAmount(amount);
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            
            // Store quote information in payment notes for later use
            String notes = String.format("quoteId:%s,email:%s,serviceType:%s", quoteId, email, serviceType);
            payment.setNotes(notes);
            
            // Save payment record
            payment = paymentService.createPayment(payment);
            
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
            result.put("paymentId", payment.getId());
            
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
    public String verifyPayment(@RequestParam String reference, Model model) {
        try {
            Payment payment = paystackService.processPaymentVerification(reference);
            
            // If payment is successful, create shipment from quote
            if (payment.getStatus().toString().equals("COMPLETED") && payment.getNotes() != null) {
                try {
                    // Parse notes to extract quote information
                    String[] parts = payment.getNotes().split(",");
                    String quoteId = parts[0].split(":")[1];
                    String email = parts[1].split(":")[1];
                    String serviceType = parts[2].split(":")[1];
                    
                    // Create shipment from quote
                    CustomerPackageRequest shipmentRequest = new CustomerPackageRequest();
                    shipmentRequest.setSenderEmail(email);
                    shipmentRequest.setServiceType(serviceType);
                    // Add other required fields as needed
                    
                    Shipment shipment = customerPackageService.createShipmentFromQuote(quoteId, shipmentRequest);
                    
                    // Redirect to success page with tracking number
                    return "redirect:/payment-success?reference=" + reference + 
                           "&tracking=" + shipment.getTrackingNumber() + 
                           "&amount=" + payment.getAmount() + 
                           "&service=" + serviceType;
                    
                } catch (Exception e) {
                    // Redirect to success page with error info
                    return "redirect:/payment-success?reference=" + reference + 
                           "&error=" + e.getMessage();
                }
            }
            
            // Payment not completed or no metadata
            return "redirect:/payment-success?reference=" + reference + "&error=Payment not completed";
            
        } catch (Exception e) {
            return "redirect:/payment-success?reference=" + reference + "&error=" + e.getMessage();
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
