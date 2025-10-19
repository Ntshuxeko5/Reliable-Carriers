package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.model.Payment;

import java.math.BigDecimal;

public interface PaystackService {
    
    /**
     * Initialize a payment transaction with Paystack
     */
    PaystackResponse initializePayment(PaystackRequest request);
    
    /**
     * Verify a payment transaction
     */
    PaystackResponse verifyPayment(String reference);
    
    /**
     * Create a payment request from existing payment
     */
    PaystackRequest createPaymentRequest(Payment payment, String callbackUrl, String redirectUrl);
    
    /**
     * Process payment verification and update payment status
     */
    Payment processPaymentVerification(String reference);
    
    /**
     * Generate a unique reference for payment
     */
    String generatePaymentReference();
    
    /**
     * Convert amount to cents (Paystack expects amount in cents for ZAR)
     */
    BigDecimal convertToCents(BigDecimal amount);
    
    /**
     * Convert amount from cents to ZAR
     */
    BigDecimal convertFromCents(Long amountInCents);
    
    /**
     * Get Paystack public key for frontend
     */
    String getPaystackPublicKey();
}
