package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.BatchPaymentRecipient;
import com.devsquad.payment_processing.model.BatchPaymentRequest;
import com.devsquad.payment_processing.model.BatchPaymentResponse;
import com.devsquad.payment_processing.model.Payment;
import com.devsquad.payment_processing.repository.AccountRepository;
import com.devsquad.payment_processing.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test to verify batch payment response consistency.
 * 
 * Validates that:
 * - successfulPayments + failedPayments = totalPayments
 * - Each recipient has exactly one result
 */
@ActiveProfiles("test")
public class BatchPaymentResponseValidationTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test Case: 2 Recipients, 1 Success + 1 Failure
     * 
     * Expected behavior:
     * - totalPayments = 2
     * - successfulPayments = 1
     * - failedPayments = 1
     * - successfulPayments + failedPayments = totalPayments = 2
     */
    @Test
    public void testBatchPaymentResponseConsistency_OneSuccessOneFailed() {
        // Arrange: Setup mock accounts and payments
        when(accountRepository.getAccountBalance(100000001L))
                .thenReturn(new BigDecimal("10000.00"));  // Sender has sufficient balance
        
        when(accountRepository.getAccountBalance(100000002L))
                .thenReturn(new BigDecimal("5000.00"));   // Receiver 1 exists
        
        when(accountRepository.getAccountBalance(123L))
                .thenReturn(null);  // Receiver 2 does NOT exist - payment will fail

        // Mock successful transfer for first recipient
        when(paymentRepository.createPayment(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setPaymentId(1);
                    payment.setInvoiceNumber("INV-001");
                    payment.setStatus(Payment.Status.CREATED);
                    return payment;
                });

        // Arrange: Create batch requests with 2 recipients
        BatchPaymentRequest request = new BatchPaymentRequest();
        request.setSenderAccountNumber(100000001L);
        request.setPaymentModeId(1);
        request.setDescription("Test Batch");

        BatchPaymentRecipient recipient1 = new BatchPaymentRecipient();
        recipient1.setReceiverAccountNumber(100000002L);
        recipient1.setAmount(new BigDecimal("1.2"));

        BatchPaymentRecipient recipient2 = new BatchPaymentRecipient();
        recipient2.setReceiverAccountNumber(123L);
        recipient2.setAmount(new BigDecimal("2.0"));

        request.setRecipients(java.util.List.of(recipient1, recipient2));

        // Act: Create batch payment
        BatchPaymentResponse response = paymentService.createBatchPayment(request);

        // Assert: Verify consistency
        System.out.println("\n=== BATCH PAYMENT RESPONSE ===");
        System.out.println("Total Payments: " + response.getTotalPayments());
        System.out.println("Successful Payments: " + response.getSuccessfulPayments());
        System.out.println("Failed Payments: " + response.getFailedPayments());
        System.out.println("Results Count: " + response.getResults().size());
        
        assertEquals(2, response.getTotalPayments(), 
                "Total payments must match recipient count");
        
        // THIS IS THE KEY VALIDATION - MUST ALWAYS BE TRUE
        assertEquals(
                response.getTotalPayments(),
                response.getSuccessfulPayments() + response.getFailedPayments(),
                "successfulPayments + failedPayments must equal totalPayments"
        );
        
        assertEquals(response.getTotalPayments(), response.getResults().size(),
                "Number of result objects must match total payments");
        
        // Print individual results
        System.out.println("\nIndividual Results:");
        for (int i = 0; i < response.getResults().size(); i++) {
            BatchPaymentResponse.PaymentResult result = response.getResults().get(i);
            System.out.println("  [" + i + "] Receiver: " + result.getReceiverAccountNumber() + 
                             ", Status: " + result.getStatus() + 
                             ", Error: " + result.getErrorMessage());
        }
        System.out.println("=== END BATCH PAYMENT RESPONSE ===\n");
    }

    /**
     * Test Case: 2 Recipients, Both Successful
     */
    @Test
    public void testBatchPaymentResponseConsistency_AllSuccessful() {
        // Arrange: Both recipients exist and have valid accounts
        when(accountRepository.getAccountBalance(100000001L))
                .thenReturn(new BigDecimal("10000.00"));
        
        when(accountRepository.getAccountBalance(100000002L))
                .thenReturn(new BigDecimal("5000.00"));
        
        when(accountRepository.getAccountBalance(100000003L))
                .thenReturn(new BigDecimal("3000.00"));

        when(paymentRepository.createPayment(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setPaymentId((int)(Math.random() * 1000));
                    payment.setInvoiceNumber("INV-" + System.currentTimeMillis());
                    payment.setStatus(Payment.Status.CREATED);
                    return payment;
                });

        // Arrange
        BatchPaymentRequest request = new BatchPaymentRequest();
        request.setSenderAccountNumber(100000001L);
        request.setPaymentModeId(1);

        request.setRecipients(java.util.List.of(
                createRecipient(100000002L, new BigDecimal("100")),
                createRecipient(100000003L, new BigDecimal("200"))
        ));

        // Act
        BatchPaymentResponse response = paymentService.createBatchPayment(request);

        // Assert
        assertEquals(2, response.getTotalPayments());
        assertEquals(response.getTotalPayments(), 
                response.getSuccessfulPayments() + response.getFailedPayments(),
                "All 2 payments should be counted: successfulPayments + failedPayments = 2");
        assertEquals(2, response.getResults().size());
    }

    private BatchPaymentRecipient createRecipient(Long receiverAccountNumber, BigDecimal amount) {
        BatchPaymentRecipient recipient = new BatchPaymentRecipient();
        recipient.setReceiverAccountNumber(receiverAccountNumber);
        recipient.setAmount(amount);
        return recipient;
    }
}


