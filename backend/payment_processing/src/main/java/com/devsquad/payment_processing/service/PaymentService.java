package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.BatchPaymentRecipient;
import com.devsquad.payment_processing.model.BatchPaymentRequest;
import com.devsquad.payment_processing.model.BatchPaymentResponse;
import com.devsquad.payment_processing.model.Payment;
import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.repository.AccountRepository;
import com.devsquad.payment_processing.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PaymentService {
    @Autowired
    PaymentRepository paymentRepo;

    @Autowired
    AccountRepository accountRepo;
    
    @Autowired
    CatalogService catalogService;

    // ── Existing operations 

    @Transactional
    public Payment createPayment(Payment request) {
        // Force auto-generation: ignore paymentId and invoiceNumber from request body
        request.setPaymentId(null);  // Always null - database will auto-generate
        request.setInvoiceNumber(null);  // Always null - repository will auto-generate
        
        // Auto-set current date/time if not provided
        if (request.getPaymentDate() == null) {
            request.setPaymentDate(Date.valueOf(LocalDate.now()));
        }
        if (request.getPaymentTime() == null) {
            request.setPaymentTime(Time.valueOf(LocalTime.now()));
        }
        
        // Set initial status to CREATED (first stage of workflow)
        request.setStatus(Payment.Status.CREATED);
        
        try {
            // Insert payment into database
            Payment savedPayment = paymentRepo.createPayment(request);

            // Process payment - returns COMPLETED or FAILED payment
            return processPayment(savedPayment.getPaymentId());

        } catch (Exception e) {
            // If database insert fails, return failed payment without DB record
            request.setPaymentId(-1);
            request.setInvoiceNumber("FAILED-" + System.currentTimeMillis());
            request.setStatus(Payment.Status.FAILED);
            request.setPaymentLog("DATABASE_ERROR: " + e.getMessage());
            return request;
        }
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

//    public Map<String, Object> cancelPayment(Integer paymentId) {
//        Payment payment = paymentRepo.getPaymentById(paymentId);
//        if (payment == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
//        }
//
//        if (payment.getStatus() != Payment.Status.CREATED) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT,
//                    "INVALID_PAYMENT_STATE: Only CREATED payments can be cancelled. Current status: "
//                    + payment.getStatus());
//        }
//
//        paymentRepo.updatePaymentStatus(paymentId, Payment.Status.FAILED);
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("paymentId", paymentId);
//        response.put("previousStatus", payment.getStatus());
//        response.put("targetStatus", Payment.Status.FAILED);
//        response.put("message", "Payment has been successfully cancelled");
//        response.put("updatedAt", LocalDateTime.now().toString());
//        return response;
//    }

    // ── Refund hook

//    public Map<String, Object> refundPayment(Integer paymentId, String reason) {
//        Payment payment = paymentRepo.getPaymentById(paymentId);
//        if (payment == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
//        }
//
//        if (payment.getStatus() != Payment.Status.COMPLETED) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT,
//                    "INVALID_PAYMENT_STATE: Only COMPLETED payments are eligible for refund. Current status: "
//                    + payment.getStatus());
//        }
//
//        // Domain-level hook: refund initiation recorded, status kept COMPLETED
//        // until downstream refund processor confirms reversal
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("paymentId", paymentId);
//        response.put("currentStatus", payment.getStatus());
//        response.put("message", "Refund has been initiated for payment " + paymentId
//                + ". Awaiting downstream processing.");
//        response.put("initiatedAt", LocalDateTime.now().toString());
//        return response;
//    }


    // ── Transition rules

    private static final Map<Payment.Status, Set<Payment.Status>> VALID_TRANSITIONS = Map.of(
            Payment.Status.CREATED,      Set.of(Payment.Status.VALIDATED, Payment.Status.FAILED),
            Payment.Status.VALIDATED,   Set.of(Payment.Status.COMPLETED, Payment.Status.FAILED),
            Payment.Status.COMPLETED,    Set.of(),  // Terminal state
            Payment.Status.FAILED,       Set.of()  // Terminal state
    );

    private boolean isValidTransition(Payment.Status from, Payment.Status to) {
        Set<Payment.Status> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
        return allowed.contains(to);
    }

    // ── Complete Payment Workflow ─────────────────────────────────────────────

    /**
     * Processes a payment through the complete workflow:
     * CREATED → VALIDATING → COMPLETED or FAILED
     * 
     * This method handles all validation checks and account operations.
     * Returns the payment object with final status (never throws exceptions).
     */
    @Transactional
    public Payment processPayment(Integer paymentId) {
        Payment payment = paymentRepo.getPaymentById(paymentId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND");
        }

        // Verify payment is in CREATED status
        if (payment.getStatus() != Payment.Status.CREATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "INVALID_PAYMENT_STATE: Can only process payments in CREATED status. Current: " + payment.getStatus());
        }
        
        // Stage 1: CREATED → VALIDATING
        paymentRepo.updatePaymentStatus(paymentId, Payment.Status.VALIDATED);
        payment.setStatus(Payment.Status.VALIDATED);

        // Run validation checks - returns error message or null if valid
        String validationError = validatePayment(payment);
        System.out.println("Validation result for payment " + paymentId + ": " + validationError);
        if (validationError != null) {
            // Validation failed - mark as FAILED
            paymentRepo.updatePaymentStatus(paymentId, Payment.Status.FAILED);
            paymentRepo.updatePaymentLog(paymentId, validationError);
            payment.setStatus(Payment.Status.FAILED);
            payment.setPaymentLog(validationError);
            return payment;
        }

        // Execute the actual payment transfer
        String transferError = executePaymentTransfer(payment);
        if (transferError != null) {
            // Transfer failed - mark as FAILED
            paymentRepo.updatePaymentStatus(paymentId, Payment.Status.FAILED);
            paymentRepo.updatePaymentLog(paymentId, transferError);
            payment.setStatus(Payment.Status.FAILED);
            payment.setPaymentLog(transferError);
            return payment;
        }

        // Stage 2: VALIDATING → COMPLETED
        paymentRepo.updatePaymentStatus(paymentId, Payment.Status.COMPLETED);
        paymentRepo.updatePaymentLog(paymentId, "Payment completed successfully");
        payment.setStatus(Payment.Status.COMPLETED);
        payment.setPaymentLog("Payment completed successfully");
        return payment;
    }

    /**
     * Validates payment details and account balances
     * Returns error message if validation fails, null if valid
     */
    private String validatePayment(Payment payment) {
        // 1. Validate sender account exists and is active
        BigDecimal senderBalance = accountRepo.getAccountBalance(payment.getSenderAccountNumber());
        if (senderBalance == null) {
            return "ACCOUNT_NOT_FOUND: Sender account " + payment.getSenderAccountNumber() + " does not exist";
        }

        // 2. Validate receiver account exists
        BigDecimal receiverBalance = accountRepo.getAccountBalance(payment.getReceiverAccountNumber());
        if (receiverBalance == null) {
            return "ACCOUNT_NOT_FOUND: Receiver account " + payment.getReceiverAccountNumber() + " does not exist";
        }

        // 3. Validate sufficient balance
        if (senderBalance.compareTo(payment.getAmount()) < 0) {
            return "INSUFFICIENT_BALANCE: Required " + payment.getAmount() + ", available " + senderBalance;
        }

        // 4. Validate amount is positive
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "INVALID_AMOUNT: Amount must be positive";
        }

        return null;  // All validations passed
    }

    /**
     * Executes the actual money transfer between accounts
     * Returns error message if transfer fails, null if successful
     */
    private String executePaymentTransfer(Payment payment) {
        try {
            // Debit sender account
            BigDecimal amount = catalogService.convertCurrency(payment.getCurrencyId(), 1, payment.getAmount());
            accountRepo.debitAccount(payment.getSenderAccountNumber(), amount);

            // Credit receiver account
            accountRepo.creditAccount(payment.getReceiverAccountNumber(), amount);

            return null;  // Transfer successful
        } catch (Exception e) {
            return "TRANSFER_ERROR: " + e.getMessage();
        }
    }

    // ── Scheduled Payment Execution (Complete Transactional Flow)

    /**
     * Executes a scheduled payment with full validation and money transfer.
     * This is the ONLY entry point for scheduled payment execution.
     * 
     * @Transactional ensures atomicity:
     *   - Validates accounts
     *   - Validates balance
     *   - Debits sender
     *   - Credits receiver
     *   - Creates payment record
     *   - Marks payment COMPLETED
     *   - Rolls back everything on failure
     */
    @Transactional
    public Payment executeScheduledPayment(Schedule schedule) {
        // 1. Validate accounts exist and are active
        BigDecimal senderBalance = accountRepo.getAccountBalance(schedule.getSenderAccountNumber());
        BigDecimal receiverBalance = accountRepo.getAccountBalance(schedule.getReceiverAccountNumber());

        if (senderBalance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ACCOUNT_NOT_FOUND: Sender account " + schedule.getSenderAccountNumber());
        }
        if (receiverBalance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ACCOUNT_NOT_FOUND: Receiver account " + schedule.getReceiverAccountNumber());
        }

        // 2. Validate sufficient balance
        if (senderBalance.compareTo(schedule.getAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "INSUFFICIENT_BALANCE: Required " + schedule.getAmount() + ", available " + senderBalance);
        }

        // 3. Debit sender and credit receiver
        accountRepo.debitAccount(schedule.getSenderAccountNumber(), schedule.getAmount());
        accountRepo.creditAccount(schedule.getReceiverAccountNumber(), schedule.getAmount());

        // 4. Create Payment record with CREATED status
        String invoiceNumber = "SCH-" + schedule.getScheduleId() + "-" + System.currentTimeMillis();

        Payment payment = new Payment(
                null,
                invoiceNumber,
                schedule.getSenderAccountNumber(),
                schedule.getReceiverAccountNumber(),
                schedule.getAmount(),
                schedule.getCurrencyId(),
                schedule.getPaymentModeId(),
                Date.valueOf(LocalDate.now()),
                Time.valueOf(LocalTime.now()),
                schedule.getDescription() != null ? schedule.getDescription() : "Scheduled Payment",
                schedule.getScheduleId(),  // Link to originating schedule
                null,  // batchId - not part of a batch
                Payment.Status.CREATED,
                null  // paymentLog - will be set after completion
        );

        Payment savedPayment = paymentRepo.createPayment(payment);

        // 5. Mark payment as COMPLETED (scheduled payments auto-complete after successful transfer)
        paymentRepo.updatePaymentStatus(savedPayment.getPaymentId(), Payment.Status.COMPLETED);
        paymentRepo.updatePaymentLog(savedPayment.getPaymentId(), "Payment completed successfully");
        savedPayment.setStatus(Payment.Status.COMPLETED);
        savedPayment.setPaymentLog("Payment completed successfully");

        return savedPayment;
    }

    // ── Batch Payment Execution ──────────────────────────────────────────────

    /**
     * Processes a batch payment by creating individual payments for each recipient.
     * REUSES createPayment() for each recipient - no duplicate logic.
     * 
     * Partial Success Strategy:
     * - Each payment is independent
     * - If one fails, others continue
     * - Returns detailed results for each payment
     * 
     * @param request Batch payment request with sender and multiple recipients
     * @return BatchPaymentResponse with summary and individual results
     */
    public BatchPaymentResponse createBatchPayment(BatchPaymentRequest request) {
        String batchId = "BATCH-" + System.currentTimeMillis();
        return createBatchPaymentWithBatchId(request, batchId);
    }

    public BatchPaymentResponse createBatchPaymentWithBatchId(BatchPaymentRequest request, String batchId) {
        // 1. Initialize response
        BatchPaymentResponse response = new BatchPaymentResponse(batchId);
        response.setTotalPayments(request.getRecipients().size());
        
        int successCount = 0;
        int failedCount = 0;
        
        // 2. Process each recipient independently
        for (BatchPaymentRecipient recipient : request.getRecipients()) {
            BatchPaymentResponse.PaymentResult result = new BatchPaymentResponse.PaymentResult();
            result.setReceiverAccountNumber(recipient.getReceiverAccountNumber());
            result.setAmount(recipient.getAmount());
            
            try {
                // 3. Build Payment object
                Payment payment = new Payment(
                        null,  // paymentId - auto-generated
                        null,  // invoiceNumber - auto-generated
                        request.getSenderAccountNumber(),
                        recipient.getReceiverAccountNumber(),
                        recipient.getAmount(),
                        recipient.getCurrencyId(),
                        request.getPaymentModeId(),
                        null,  // paymentDate - auto-set
                        null,  // paymentTime - auto-set
                        recipient.getDescription() != null 
                                ? recipient.getDescription() 
                                : request.getDescription(),
                        null,  // scheduleId - not from schedule
                        batchId,
                        null,
                        null
                );
                
                // 4. REUSE existing createPayment() - follows full workflow
                // Attach batch-level tags to each payment
                if (request.getTags() != null && !request.getTags().isEmpty()) {
                    payment.setTags(request.getTags());
                }
                Payment savedPayment = createPayment(payment);
                
                // 6. Check payment status to determine success/failure
                if (savedPayment.getStatus() == Payment.Status.COMPLETED) {
                    result.setPaymentId(savedPayment.getPaymentId());
                    result.setStatus("SUCCESS");
                    result.setErrorMessage(null);
                    successCount++;
                } else {
                    // Payment failed - get error from paymentLog
                    result.setPaymentId(savedPayment.getPaymentId());
                    result.setStatus("FAILED");
                    result.setErrorMessage(savedPayment.getPaymentLog());
                    failedCount++;
                }

            } catch (ResponseStatusException e) {
                // 6. Record failure but continue processing
                result.setPaymentId(null);
                result.setStatus("FAILED");
                result.setErrorMessage(e.getReason());
                failedCount++;

            } catch (Exception e) {
                // Catch any unexpected errors
                result.setPaymentId(null);
                result.setStatus("FAILED");
                result.setErrorMessage("UNEXPECTED_ERROR: " + e.getMessage());
                failedCount++;
            }
            
            response.getResults().add(result);
        }
        
        // 7. Set summary
        response.setSuccessfulPayments(successCount);
        response.setFailedPayments(failedCount);
        
        // 8. Validate consistency
        int totalExpected = response.getTotalPayments();
        int totalCounted = successCount + failedCount;
        int resultCount = response.getResults().size();
        
        if (totalCounted != totalExpected || resultCount != totalExpected) {
            System.err.println("BATCH RESPONSE INCONSISTENCY DETECTED:");
            System.err.println("  Total Recipients: " + totalExpected);
            System.err.println("  Counted (success + failed): " + totalCounted + " (success=" + successCount + ", failed=" + failedCount + ")");
            System.err.println("  Result Objects: " + resultCount);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "BATCH_CALCULATION_ERROR: Response counts are inconsistent");
        }
        
        return response;
    }
}
