package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Frequency;
import com.devsquad.payment_processing.model.Payment;
import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.model.ScheduleStatus;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Core scheduled payment execution service.
 * Shared by both background scheduler and manual trigger API.
 * Delegates actual payment execution to PaymentService.
 */
@Service
public class ScheduleExecutionService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ScheduleRepository scheduleRepo;

    /**
     * Executes a schedule: validates, executes payment via PaymentService, advances schedule.
     * All payment execution goes through PaymentService - no direct Payment table access.
     *
     * @param schedule The schedule to execute
     */
    @Transactional
    public void executeSchedule(Schedule schedule) {
        try {
            // 1. Guard: only execute ACTIVE schedules
            if (schedule.getStatus() != ScheduleStatus.ACTIVE) {
                System.out.println("[ScheduleExecutionService] Skipping schedule " + schedule.getScheduleId()
                        + " - status is " + schedule.getStatus());
                return;
            }

            // 2. Execute payment via PaymentService (validates accounts, balances, debits/credits)
            Payment payment = paymentService.executeScheduledPayment(schedule);
            System.out.println("[ScheduleExecutionService] Payment " + payment.getPaymentId()
                    + " executed successfully for schedule " + schedule.getScheduleId());

            // 3. Advance schedule dates
            LocalDate today = LocalDate.now();
            Date lastRun = Date.valueOf(today);
            Date nextRun = computeNextRunDate(today, schedule.getFrequency());

            boolean scheduleExpired = schedule.getEndDate() != null
                    && !nextRun.toLocalDate().isBefore(schedule.getEndDate().toLocalDate());

            if (scheduleExpired) {
                scheduleRepo.markCompleted(schedule.getScheduleId().longValue());
                System.out.println("[ScheduleExecutionService] Schedule " + schedule.getScheduleId()
                        + " marked COMPLETED (end date reached)");
            } else {
                scheduleRepo.updateNextExecution(schedule.getScheduleId().longValue(), lastRun, nextRun);
                System.out.println("[ScheduleExecutionService] Schedule " + schedule.getScheduleId()
                        + " next run updated to " + nextRun);
            }

        } catch (Exception ex) {
            // Log failure but do NOT delete or cancel the schedule
            System.err.println("[ScheduleExecutionService] Failed to execute schedule "
                    + schedule.getScheduleId() + ": " + ex.getMessage());
            ex.printStackTrace();
            // Schedule remains ACTIVE and will be retried on next scheduler run
        }
    }

    /**
     * Computes next run date based on frequency.
     * Single source of truth for date advancement logic.
     */
    private Date computeNextRunDate(LocalDate from, Frequency frequency) {
        if (frequency == null) {
            return Date.valueOf(from.plusMonths(1));
        }
        LocalDate next = switch (frequency) {
            case DAILY  -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case YEARLY -> from.plusYears(1);
            case MONTHLY -> from.plusMonths(1);
        };
        return Date.valueOf(next);
    }
}
