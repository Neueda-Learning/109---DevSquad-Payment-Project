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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BatchScheduleService {

    private final BatchScheduleRepository batchScheduleRepo;
    private final PaymentService paymentService;
    private final AccountRepository accountRepo;
    private final ScheduleRepository scheduleRepo;
    private final CatalogService catalogService;

    public BatchScheduleService(BatchScheduleRepository batchScheduleRepo,
                                PaymentService paymentService,
                                AccountRepository accountRepo,
                                ScheduleRepository scheduleRepo,
                                CatalogService catalogService) {
        this.batchScheduleRepo = batchScheduleRepo;
        this.paymentService = paymentService;
        this.accountRepo = accountRepo;
        this.scheduleRepo = scheduleRepo;
        this.catalogService = catalogService;
    }

    @Transactional
    public Map<String, Object> createBatchSchedule(BatchPaymentRequest request) {
        validateBatchScheduleRequest(request);

        String batchId = "BATCH-" + System.currentTimeMillis();

        BatchSchedule schedule = new BatchSchedule(
                null,
                batchId,
                request.getSenderAccountNumber(),
                request.getPaymentModeId(),
                request.getDescription(),
                Date.valueOf(request.getScheduledDate()),
                BatchScheduleStatus.SCHEDULED
        );

        Long batchScheduleId = batchScheduleRepo.createBatchSchedule(schedule);
        if (batchScheduleId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "BATCH_SCHEDULE_CREATE_FAILED");
        }

        List<BatchScheduleRecipient> recipients = request.getRecipients().stream()
                .map(recipient -> new BatchScheduleRecipient(
                        recipient.getReceiverAccountNumber(),
                        recipient.getAmount(),
                        recipient.getCurrencyId(),
                        recipient.getDescription()
                ))
                .toList();

        batchScheduleRepo.addRecipients(batchScheduleId, recipients);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("batchScheduleId", batchScheduleId);
        response.put("batchId", batchId);
        response.put("scheduledDate", request.getScheduledDate());
        response.put("status", BatchScheduleStatus.SCHEDULED.name());
        response.put("totalPayments", request.getRecipients().size());
        return response;
    }

    public List<Map<String, Object>> getAllBatchSchedules() {
        return batchScheduleRepo.getAllBatchScheduleSummaries();
    }

    public Map<String, Object> getBatchScheduleDetails(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: batchId is required");
        }

        Map<String, Object> details = batchScheduleRepo.getBatchScheduleDetailsByBatchId(batchId);
        if (details == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "BATCH_SCHEDULE_NOT_FOUND: " + batchId);
        }

        return details;
    }

    public void processDueBatchSchedules() {
        List<BatchSchedule> dueSchedules = batchScheduleRepo.getDueBatchSchedules();

        if (dueSchedules.isEmpty()) {
            return;
        }

        System.out.println("[BatchScheduleService] Processing " + dueSchedules.size() + " due batch schedule(s).");

        for (BatchSchedule schedule : dueSchedules) {
            executeDueBatchSchedule(schedule);
        }
    }

    private void executeDueBatchSchedule(BatchSchedule schedule) {
        try {
            batchScheduleRepo.markProcessing(schedule.getBatchScheduleId());

            List<BatchScheduleRecipient> recipients = batchScheduleRepo.getRecipients(schedule.getBatchScheduleId());
            List<BatchPaymentRecipient> batchRecipients = recipients.stream()
                    .map(recipient -> new BatchPaymentRecipient(
                            recipient.getReceiverAccountNumber(),
                            recipient.getAmount(),
                            recipient.getDescription(),
                            recipient.getCurrencyId()))
                    .toList();

            BatchPaymentRequest executionRequest = new BatchPaymentRequest();
            executionRequest.setSenderAccountNumber(schedule.getSenderAccountNumber());
            executionRequest.setPaymentModeId(schedule.getPaymentModeId());
            executionRequest.setDescription(schedule.getDescription());
            executionRequest.setRecipients(batchRecipients);

            BatchPaymentResponse response = paymentService.createBatchPaymentWithBatchId(
                    executionRequest,
                    schedule.getBatchId()
            );

            BatchScheduleStatus finalStatus = resolveBatchStatus(response);
            batchScheduleRepo.markCompleted(schedule.getBatchScheduleId(), finalStatus);

        } catch (Exception ex) {
            System.err.println("[BatchScheduleService] Failed batch schedule "
                    + schedule.getBatchScheduleId() + ": " + ex.getMessage());
            batchScheduleRepo.markFailed(schedule.getBatchScheduleId(), ex.getMessage());
        }
    }

    private BatchScheduleStatus resolveBatchStatus(BatchPaymentResponse response) {
        if (response.getFailedPayments() == 0) {
            return BatchScheduleStatus.COMPLETED;
        }
        if (response.getSuccessfulPayments() > 0) {
            return BatchScheduleStatus.PARTIAL_FAILED;
        }
        return BatchScheduleStatus.FAILED;
    }

    private void validateBatchScheduleRequest(BatchPaymentRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: request body is required");
        }

        if (request.getScheduledDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: scheduledDate is required");
        }

        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "INVALID_SCHEDULED_DATE: scheduledDate cannot be in the past");
        }

        if (request.getSenderAccountNumber() == null
                || accountRepo.getAccountBalance(request.getSenderAccountNumber()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ACCOUNT_NOT_FOUND: Sender account " + request.getSenderAccountNumber() + " does not exist");
        }

        if (request.getPaymentModeId() == null || !scheduleRepo.paymentMethodExists(request.getPaymentModeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "PAYMENT_MODE_NOT_FOUND: " + request.getPaymentModeId());
        }

        List<Currency> currencies = catalogService.getAllCurrencies();

        for (BatchPaymentRecipient recipient : request.getRecipients()) {
            if (recipient.getReceiverAccountNumber() == null
                    || accountRepo.getAccountBalance(recipient.getReceiverAccountNumber()) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ACCOUNT_NOT_FOUND: Receiver account " + recipient.getReceiverAccountNumber() + " does not exist");
            }

            if (recipient.getCurrencyId() != null && recipient.getCurrencyId() > 0
                    && recipient.getCurrencyId() > currencies.size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "CURRENCY_NOT_FOUND: " + recipient.getCurrencyId());
            }
        }
    }
}

