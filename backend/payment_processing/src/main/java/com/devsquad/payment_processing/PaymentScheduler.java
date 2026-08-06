package com.devsquad.payment_processing;

import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import com.devsquad.payment_processing.service.BatchScheduleService;
import com.devsquad.payment_processing.service.ScheduleExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentScheduler {

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private ScheduleExecutionService executionService;

    @Autowired
    private BatchScheduleService batchScheduleService;

    /**
     * Runs every minute by default.
     * Override interval via scheduler.payment.fixed-delay-ms in application.properties.
     */
    @Scheduled(fixedDelayString = "${scheduler.payment.fixed-delay-ms:60000}")
    public void processScheduledPayments() {
        List<Schedule> dueSchedules = scheduleRepo.getDueSchedules();

        if (dueSchedules.isEmpty()) {
            return;
        }

        System.out.println("[PaymentScheduler] Processing " + dueSchedules.size() + " due schedule(s).");

        for (Schedule schedule : dueSchedules) {
            executionService.executeSchedule(schedule);
        }

        batchScheduleService.processDueBatchSchedules();
    }
}

