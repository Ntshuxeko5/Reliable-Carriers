package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentMethod;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface PaymentService {
    
    Payment createPayment(Payment payment);
    
    Payment updatePayment(Long id, Payment payment);
    
    Payment getPaymentById(Long id);
    
    Payment getPaymentByTransactionId(String transactionId);
    
    List<Payment> getAllPayments();
    
    List<Payment> getPaymentsByShipment(Shipment shipment);
    
    List<Payment> getPaymentsByUser(User user);
    
    List<Payment> getPaymentsByStatus(PaymentStatus status);
    
    List<Payment> getPaymentsByMethod(PaymentMethod paymentMethod);
    
    List<Payment> getPaymentsByDateRange(Date startDate, Date endDate);
    
    List<Payment> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);
    
    void deletePayment(Long id);
    
    Payment processPayment(Long paymentId);
    
    Payment refundPayment(Long paymentId, String reason);
}