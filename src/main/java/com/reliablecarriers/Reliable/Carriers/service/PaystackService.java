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
    PaystackRequest createPaymentRequest(Payment payment, String callbackUrl);
    
    /**
     * Process payment verification and update payment status
     */
    Payment processPaymentVerification(String reference);
    
    /**
     * Generate a unique reference for payment
     */
    String generatePaymentReference();
    
    /**
     * Convert amount to kobo (Paystack expects amount in kobo)
     */
    BigDecimal convertToKobo(BigDecimal amount);
    
    /**
     * Convert amount from kobo to naira
     */
    BigDecimal convertFromKobo(Long amountInKobo);
    
    /**
     * Get Paystack public key for frontend
     */
    String getPaystackPublicKey();
}
