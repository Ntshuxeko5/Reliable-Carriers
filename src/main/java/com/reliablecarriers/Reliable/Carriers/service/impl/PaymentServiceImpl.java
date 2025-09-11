package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentMethod;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.PaymentRepository;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        // Generate transaction ID if not provided
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            payment.setTransactionId(generateTransactionId());
        }
        
        // Set default status if not provided
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        }
        
        // Set payment date if not provided
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(new Date());
        }
        
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        Payment existingPayment = getPaymentById(id);
        
        // Update fields
        existingPayment.setAmount(payment.getAmount());
        existingPayment.setPaymentMethod(payment.getPaymentMethod());
        existingPayment.setNotes(payment.getNotes());
        
        // Don't update status, transaction ID, or payment date here
        // Those should be updated through specific methods
        
        return paymentRepository.save(existingPayment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction ID: " + transactionId));
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public List<Payment> getPaymentsByShipment(Shipment shipment) {
        return paymentRepository.findByShipment(shipment);
    }

    @Override
    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findByUser(user);
    }

    @Override
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    public List<Payment> getPaymentsByMethod(PaymentMethod paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod);
    }

    @Override
    public List<Payment> getPaymentsByDateRange(Date startDate, Date endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate);
    }

    @Override
    public List<Payment> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        List<Payment> minPayments = paymentRepository.findByAmountGreaterThanEqual(minAmount);
        return minPayments.stream()
                .filter(payment -> payment.getAmount().compareTo(maxAmount) <= 0)
                .toList();
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }

    @Override
    public Payment processPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        
        // Only process pending payments
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Cannot process payment with status: " + payment.getStatus());
        }
        
        // In a real application, you would integrate with a payment gateway here
        // For now, we'll just simulate a successful payment
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(new Date());
        
        return paymentRepository.save(payment);
    }

    @Override
    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = getPaymentById(paymentId);
        
        // Only refund completed payments
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Cannot refund payment with status: " + payment.getStatus());
        }
        
        // In a real application, you would integrate with a payment gateway here
        // For now, we'll just simulate a successful refund
        payment.setStatus(PaymentStatus.REFUNDED);
        
        // Update notes with refund reason
        String updatedNotes = payment.getNotes() != null ? 
                payment.getNotes() + "\nRefund reason: " + reason : 
                "Refund reason: " + reason;
        payment.setNotes(updatedNotes);
        
        return paymentRepository.save(payment);
    }
    
    // Helper methods
    private String generateTransactionId() {
        // Generate a unique transaction ID
        return "TXN" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}