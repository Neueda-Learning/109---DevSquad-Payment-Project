package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Frequency;
import com.devsquad.payment_processing.model.Payment;
import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.model.ScheduleStatus;
import com.devsquad.payment_processing.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleExecutionServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private ScheduleRepository scheduleRepo;

    @InjectMocks
    private ScheduleExecutionService scheduleExecutionService;

    @Test
    void executeScheduleSkipsWhenExecutionTimeNotReachedToday() {
        Schedule schedule = buildSchedule(
                Date.valueOf(LocalDate.now()),
                Time.valueOf(LocalTime.now().plusMinutes(5).withNano(0))
        );

        scheduleExecutionService.executeSchedule(schedule);

        verify(paymentService, never()).createPayment(any(Payment.class));
        verify(scheduleRepo, never()).updateNextExecution(anyLong(), any(Date.class), any(Date.class));
        verify(scheduleRepo, never()).markCompleted(anyLong());
    }

    @Test
    void executeScheduleRunsWhenExecutionTimeReachedToday() {
        Schedule schedule = buildSchedule(
                Date.valueOf(LocalDate.now()),
                Time.valueOf(LocalTime.now().minusMinutes(1).withNano(0))
        );

        Payment completedPayment = new Payment();
        completedPayment.setPaymentId(101);
        completedPayment.setStatus(Payment.Status.COMPLETED);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(completedPayment);

        scheduleExecutionService.executeSchedule(schedule);

        verify(paymentService).createPayment(any(Payment.class));
        verify(scheduleRepo).updateNextExecution(anyLong(), any(Date.class), any(Date.class));
        verify(scheduleRepo, never()).markCompleted(anyLong());
    }

    private Schedule buildSchedule(Date nextRunDate, Time executionTime) {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(1);
        schedule.setSenderAccountNumber(100000001L);
        schedule.setReceiverAccountNumber(100000002L);
        schedule.setAmount(new BigDecimal("100.00"));
        schedule.setCurrencyId(1);
        schedule.setPaymentModeId(1);
        schedule.setDescription("Test schedule");
        schedule.setFrequency(Frequency.DAILY);
        schedule.setStartDate(Date.valueOf(LocalDate.now().minusDays(1)));
        schedule.setScheduledTime(executionTime);
        schedule.setEndDate(Date.valueOf(LocalDate.now().plusDays(10)));
        schedule.setNextRunDate(nextRunDate);
        schedule.setStatus(ScheduleStatus.ACTIVE);
        return schedule;
    }
}

