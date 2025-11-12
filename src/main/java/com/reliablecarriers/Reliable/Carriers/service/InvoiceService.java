package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Payment;

/**
 * Service for generating invoices and receipts
 */
public interface InvoiceService {
    
    /**
     * Generate PDF invoice for a payment
     */
    byte[] generateInvoicePDF(Payment payment) throws Exception;
    
    /**
     * Generate invoice number
     */
    String generateInvoiceNumber(Long paymentId);
    
    /**
     * Generate receipt PDF (simpler version)
     */
    byte[] generateReceiptPDF(Payment payment) throws Exception;
}

