package com.devsquad.payment_processing.api;

import com.devsquad.payment_processing.model.BatchPaymentRequest;
import com.devsquad.payment_processing.model.BatchPaymentResponse;
import com.devsquad.payment_processing.model.Payment;
import com.devsquad.payment_processing.service.BatchScheduleService;
import com.devsquad.payment_processing.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BatchScheduleService batchScheduleService;

    /**
     * POST /api/v1/payments/create
     * Creates and automatically processes a payment in one step.
     * 
     * Workflow: CREATED → VALIDATING → COMPLETED or FAILED
     * 
     * Returns payment with final status (COMPLETED or FAILED)
     * and proper error messages if validation fails.
     */
    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    /**
     * POST /api/v1/payments/batch
     * Creates multiple payments in a single batch.
     * 
     * Each payment is processed independently:
     * - Success/failure is tracked per recipient
     * - One failure does NOT stop others
     * - All payments share the same batchId
     * 
     * Returns summary with individual results.
     * Returns HTTP 207 Multi-Status if partial failures occur.
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchPaymentResponse> createBatchPayment(
            @Valid @RequestBody BatchPaymentRequest request) {
        System.out.println("Received batch payment request: " + request.getRecipients().size() + " recipients");
        BatchPaymentResponse response = paymentService.createBatchPayment(request);

        // Return 207 Multi-Status if there are partial failures
        if (response.getFailedPayments() > 0 && response.getSuccessfulPayments() > 0) {
            return ResponseEntity.status(207).body(response);  // HTTP 207 Multi-Status
        }
        
        // Return 201 if all succeeded
        if (response.getFailedPayments() == 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        
        // Return 400 if all failed
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * POST /api/v1/payments/batch/scheduled
     * Creates a scheduled batch payment that executes on scheduledDate.
     */
    @PostMapping("/batch/scheduled")
    public ResponseEntity<Map<String, Object>> createScheduledBatchPayment(
            @Valid @RequestBody BatchPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(batchScheduleService.createBatchSchedule(request));
    }

    // Get Payment By Id
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer id) {
        // Service throws 404 ResponseStatusException when not found
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // Get All Payments
    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // ── List Payments with Filters + Pagination

    // @GetMapping
    // public ResponseEntity<List<Payment>> getPaymentsWithFilters(
    //         @RequestParam(required = false) String status,
    //         @RequestParam(required = false) String mode,
    //         @RequestParam(required = false) String fromDate,
    //         @RequestParam(required = false) String toDate,
    //         @RequestParam(required = false) Double minAmount,
    //         @RequestParam(required = false) Double maxAmount,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "20") int size) {

    //     return ResponseEntity.ok(
    //             paymentService.getPaymentsWithFilters(status, mode, fromDate, toDate,
    //                     minAmount, maxAmount, page, size));
    // }

    // ── Update Payment Status
    /**
     * PATCH /api/v1/payments/{id}/status
     * Body: { "targetStatus": "COMPLETED|FAILED|CANCELLED", "reason": "optional" }
     * Valid transitions: PENDING -> COMPLETED | FAILED | CANCELLED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(id, request.get("targetStatus"), request.get("reason")));
    }

    // ── Cancel Payment Hook

    /**
     * POST /api/v1/payments/{id}/cancel
     * Transitions a PENDING payment to CANCELLED.
    //  */
    // @PostMapping("/{id}/cancel")
    // public ResponseEntity<Map<String, Object>> cancelPayment(@PathVariable Integer id) {
    //     return ResponseEntity.ok(paymentService.cancelPayment(id));
    // }

    // ── Refund Payment Hook

    /**
     * POST /api/v1/payments/{id}/refund
     * Body (optional): { "reason": "..." }
     * Validates the payment is COMPLETED and records refund initiation.
     */
    // @PostMapping("/{id}/refund")
    // public ResponseEntity<Map<String, Object>> refundPayment(
    //         @PathVariable Integer id,
    //         @RequestBody(required = false) Map<String, String> request) {

    //     String reason = request != null ? request.get("reason") : null;
    //     return ResponseEntity.ok(paymentService.refundPayment(id, reason));
    // }


    // ── Delete Payment

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Integer id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok("Payment deleted successfully");
    }

}