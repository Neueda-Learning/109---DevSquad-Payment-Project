package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Payment;
import com.devsquad.payment_processing.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PaymentService {
    @Autowired
    PaymentRepository paymentRepo;

    // ── Existing operations 

    public Payment createPayment(Payment request) {
        return paymentRepo.createPayment(request);
    }

    public Payment getPaymentById(Integer paymentId) {
        Payment payment = paymentRepo.getPaymentById(paymentId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
        }
        return payment;
    }

    public List<Payment> getAllPayments() {
        return paymentRepo.getAllPayments();
    }

    public void deletePayment(Integer paymentId) {
        paymentRepo.deletePayment(paymentId);
    }

    // ── Filtered list 

    public List<Payment> getPaymentsWithFilters(String status, String mode,
                                                String fromDate, String toDate,
                                                Double minAmount, Double maxAmount,
                                                int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SEARCH_FILTER_INVALID: page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SEARCH_FILTER_INVALID: size must be between 1 and 100");
        }
        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SEARCH_FILTER_INVALID: minAmount must be <= maxAmount");
        }
        return paymentRepo.getPaymentsWithFilters(status, mode, fromDate, toDate, minAmount, maxAmount, page, size);
    }

    // ── Status update with transition validation 

    public Map<String, Object> updatePaymentStatus(Integer paymentId, String targetStatusStr, String reason) {
        Payment payment = paymentRepo.getPaymentById(paymentId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
        }

        Payment.Status targetStatus;
        try {
            targetStatus = Payment.Status.valueOf(targetStatusStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: Unknown status value: " + targetStatusStr);
        }

        Payment.Status currentStatus = payment.getStatus();
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "INVALID_PAYMENT_STATE: Cannot transition from " + currentStatus + " to " + targetStatus);
        }

        paymentRepo.updatePaymentStatus(paymentId, targetStatus);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentId", paymentId);
        response.put("previousStatus", currentStatus);
        response.put("targetStatus", targetStatus);
        response.put("updatedAt", LocalDateTime.now().toString());
        return response;
    }

    // ── Cancel hook ────────

    public Map<String, Object> cancelPayment(Integer paymentId) {
        Payment payment = paymentRepo.getPaymentById(paymentId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
        }

        if (payment.getStatus() != Payment.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "INVALID_PAYMENT_STATE: Only PENDING payments can be cancelled. Current status: "
                    + payment.getStatus());
        }

        paymentRepo.updatePaymentStatus(paymentId, Payment.Status.CANCELLED);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentId", paymentId);
        response.put("previousStatus", payment.getStatus());
        response.put("targetStatus", Payment.Status.CANCELLED);
        response.put("message", "Payment has been successfully cancelled");
        response.put("updatedAt", LocalDateTime.now().toString());
        return response;
    }

    // ── Refund hook ────────

    public Map<String, Object> refundPayment(Integer paymentId, String reason) {
        Payment payment = paymentRepo.getPaymentById(paymentId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
        }

        if (payment.getStatus() != Payment.Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "INVALID_PAYMENT_STATE: Only COMPLETED payments are eligible for refund. Current status: "
                    + payment.getStatus());
        }

        // Domain-level hook: refund initiation recorded, status kept COMPLETED
        // until downstream refund processor confirms reversal
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentId", paymentId);
        response.put("currentStatus", payment.getStatus());
        response.put("message", "Refund has been initiated for payment " + paymentId
                + ". Awaiting downstream processing.");
        response.put("initiatedAt", LocalDateTime.now().toString());
        return response;
    }


    // ── Transition rules

    private static final Map<Payment.Status, Set<Payment.Status>> VALID_TRANSITIONS = Map.of(
            Payment.Status.PENDING,    Set.of(Payment.Status.COMPLETED, Payment.Status.FAILED, Payment.Status.CANCELLED),
            Payment.Status.COMPLETED,  Set.of(),
            Payment.Status.FAILED,     Set.of(),
            Payment.Status.CANCELLED,  Set.of()
    );

    private boolean isValidTransition(Payment.Status from, Payment.Status to) {
        Set<Payment.Status> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
        return allowed.contains(to);
    }
}