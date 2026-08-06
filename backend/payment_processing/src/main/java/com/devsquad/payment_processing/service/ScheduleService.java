package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.model.ScheduleStatus;
import com.devsquad.payment_processing.repository.AccountRepository;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ScheduleExecutionService executionService;

    // Create Schedule
    public Schedule createSchedule(Schedule schedule) {
        validateCreateSchedule(schedule);

        schedule.setScheduleId(null);
        schedule.setNextRunDate(schedule.getStartDate());
        schedule.setLastRunDate(null);
        schedule.setStatus(ScheduleStatus.ACTIVE);

        if (schedule.getDescription() != null) {
            String description = schedule.getDescription().trim();
            schedule.setDescription(description.isEmpty() ? null : description);
        }

        return scheduleRepo.createSchedule(schedule);
    }

    // Get Schedule by ID
    public Schedule getScheduleById(Integer scheduleId) {
        return scheduleRepo.getScheduleById(scheduleId);
    }

    // Update Schedule
    public Schedule updateSchedule(Long scheduleId, Schedule schedule) {
        return scheduleRepo.updateSchedule(scheduleId, schedule);
    }

    // Delete Schedule
    public void deleteSchedule(Integer scheduleId) {
        scheduleRepo.deleteSchedule(scheduleId);
    }

    /**
     * Manual trigger — reuses the exact same execution logic as the background scheduler.
     * No code duplication.
     */
    public void triggerSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepo.getScheduleById(scheduleId.intValue());
        if (schedule == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND: " + scheduleId);
        }
        executionService.executeSchedule(schedule);
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepo.getAllSchedules();
    }

    private void validateCreateSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: request body is required");
        }

        validateAccountExists(schedule.getSenderAccountNumber(), "Sender");
        validateAccountExists(schedule.getReceiverAccountNumber(), "Receiver");

        if (schedule.getSenderAccountNumber() != null
                && schedule.getSenderAccountNumber().equals(schedule.getReceiverAccountNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: sender and receiver accounts must be different");
        }

        if (schedule.getAmount() == null || schedule.getAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "INVALID_AMOUNT: amount must be greater than zero");
        }

        if (schedule.getPaymentModeId() == null || !scheduleRepo.paymentMethodExists(schedule.getPaymentModeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "PAYMENT_MODE_NOT_FOUND: " + schedule.getPaymentModeId());
        }

        if (!isValidCurrencyId(schedule.getCurrencyId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "CURRENCY_NOT_FOUND: " + schedule.getCurrencyId());
        }

        if (schedule.getStartDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: start date is required");
        }

        if (schedule.getScheduledTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: scheduled time is required");
        }

        LocalDate startDate = schedule.getStartDate().toLocalDate();
        if (startDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "INVALID_START_DATE: start date cannot be in the past");
        }

        if (schedule.getEndDate() != null && !schedule.getEndDate().toLocalDate().isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "INVALID_END_DATE: end date must be after start date");
        }
    }

    private void validateAccountExists(Long accountNumber, String label) {
        if (accountNumber == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: " + label.toLowerCase() + " account number is required");
        }

        if (accountRepo.getAccountBalance(accountNumber) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "ACCOUNT_NOT_FOUND: " + label + " account " + accountNumber + " does not exist");
        }
    }

    private boolean isValidCurrencyId(Integer currencyId) {
        if (currencyId == null || currencyId <= 0) {
            return false;
        }

        List<Currency> currencies = catalogService.getAllCurrencies();
        return currencyId <= currencies.size();
    }

    // Returns schedule execution details.
    // Payment history is in the Payments table (via schedule_id link).
//    public Object getScheduleExecution(Long scheduleId) {
//        return scheduleRepo.getScheduleExecution(scheduleId);
//    }
}