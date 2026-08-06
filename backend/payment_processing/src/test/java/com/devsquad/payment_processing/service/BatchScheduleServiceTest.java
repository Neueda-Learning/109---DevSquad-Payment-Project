package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.BatchPaymentRecipient;
import com.devsquad.payment_processing.model.BatchPaymentRequest;
import com.devsquad.payment_processing.model.BatchPaymentResponse;
import com.devsquad.payment_processing.model.BatchSchedule;
import com.devsquad.payment_processing.model.BatchScheduleRecipient;
import com.devsquad.payment_processing.model.BatchScheduleStatus;
import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.repository.AccountRepository;
import com.devsquad.payment_processing.repository.BatchScheduleRepository;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchScheduleServiceTest {

    @Mock
    private BatchScheduleRepository batchScheduleRepo;

    @Mock
    private PaymentService paymentService;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private ScheduleRepository scheduleRepo;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private BatchScheduleService batchScheduleService;

    @Test
    void createBatchScheduleStoresHeaderAndRecipients() {
        BatchPaymentRequest request = buildRequest(LocalDate.now().plusDays(1));

        when(accountRepo.getAccountBalance(100000001L)).thenReturn(new BigDecimal("100000.00"));
        when(accountRepo.getAccountBalance(100000002L)).thenReturn(new BigDecimal("5000.00"));
        when(scheduleRepo.paymentMethodExists(1)).thenReturn(true);
        when(catalogService.getAllCurrencies()).thenReturn(List.of(
                new Currency(1, "United States", "$", "USD")
        ));
        when(batchScheduleRepo.createBatchSchedule(any(BatchSchedule.class))).thenReturn(10L);

        Map<String, Object> response = batchScheduleService.createBatchSchedule(request);

        assertEquals(10L, response.get("batchScheduleId"));
        assertEquals("SCHEDULED", response.get("status"));
        assertEquals(request.getScheduledDate(), response.get("scheduledDate"));
        verify(batchScheduleRepo).addRecipients(eq(10L), any(List.class));
    }

    @Test
    void createBatchScheduleRejectsPastDate() {
        BatchPaymentRequest request = buildRequest(LocalDate.now().minusDays(1));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> batchScheduleService.createBatchSchedule(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("INVALID_SCHEDULED_DATE: scheduledDate cannot be in the past", ex.getReason());
    }

    @Test
    void processDueBatchSchedulesUsesExistingBatchExecutionAndMarksPartialFailure() {
        BatchSchedule schedule = new BatchSchedule(
                11L,
                "BATCH-123",
                100000001L,
                1,
                "Monthly payout",
                Date.valueOf(LocalDate.now()),
                BatchScheduleStatus.SCHEDULED
        );

        when(batchScheduleRepo.getDueBatchSchedules()).thenReturn(List.of(schedule));
        when(batchScheduleRepo.getRecipients(11L)).thenReturn(List.of(
                new BatchScheduleRecipient(100000002L, new BigDecimal("500.00"), 1, "Vendor A")
        ));

        BatchPaymentResponse executionResponse = new BatchPaymentResponse("BATCH-123");
        executionResponse.setTotalPayments(1);
        executionResponse.setSuccessfulPayments(0);
        executionResponse.setFailedPayments(1);

        when(paymentService.createBatchPaymentWithBatchId(any(BatchPaymentRequest.class), eq("BATCH-123")))
                .thenReturn(executionResponse);

        batchScheduleService.processDueBatchSchedules();

        verify(batchScheduleRepo).markProcessing(11L);
        verify(paymentService).createBatchPaymentWithBatchId(any(BatchPaymentRequest.class), eq("BATCH-123"));
        verify(batchScheduleRepo).markCompleted(11L, BatchScheduleStatus.FAILED);
    }

    private BatchPaymentRequest buildRequest(LocalDate scheduledDate) {
        BatchPaymentRequest request = new BatchPaymentRequest();
        request.setSenderAccountNumber(100000001L);
        request.setPaymentModeId(1);
        request.setDescription("Team payout");
        request.setScheduledDate(scheduledDate);
        request.setRecipients(List.of(
                new BatchPaymentRecipient(100000002L, new BigDecimal("1500.00"), "Salary", 1)
        ));
        return request;
    }
}

