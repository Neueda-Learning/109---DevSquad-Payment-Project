package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.Frequency;
import com.devsquad.payment_processing.model.Schedule;
import com.devsquad.payment_processing.model.ScheduleStatus;
import com.devsquad.payment_processing.repository.AccountRepository;
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
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private CatalogService catalogService;

    @Mock
    private ScheduleExecutionService executionService;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void createScheduleAppliesDefaultsAndPersists() {
        Schedule request = buildValidSchedule();
        request.setDescription("  Monthly rent  ");

        stubValidDependencies(request);
        when(scheduleRepo.createSchedule(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule created = scheduleService.createSchedule(request);

        assertNull(created.getScheduleId());
        assertEquals(request.getStartDate(), created.getNextRunDate());
        assertNull(created.getLastRunDate());
        assertEquals(ScheduleStatus.ACTIVE, created.getStatus());
        assertEquals("Monthly rent", created.getDescription());
        verify(scheduleRepo).createSchedule(request);
    }

    @Test
    void createScheduleRejectsPastStartDate() {
        Schedule request = buildValidSchedule();
        request.setStartDate(Date.valueOf(LocalDate.now().minusDays(1)));

        stubValidDependencies(request);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> scheduleService.createSchedule(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("INVALID_START_DATE: start date cannot be in the past", exception.getReason());
        verify(scheduleRepo, never()).createSchedule(any(Schedule.class));
    }

    @Test
    void createScheduleRejectsInvalidEndDate() {
        Schedule request = buildValidSchedule();
        request.setEndDate(request.getStartDate());

        stubValidDependencies(request);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> scheduleService.createSchedule(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("INVALID_END_DATE: end date must be after start date", exception.getReason());
        verify(scheduleRepo, never()).createSchedule(any(Schedule.class));
    }

    @Test
    void createScheduleRejectsUnknownPaymentMethod() {
        Schedule request = buildValidSchedule();

        when(accountRepo.getAccountBalance(request.getSenderAccountNumber())).thenReturn(new BigDecimal("50000.00"));
        when(accountRepo.getAccountBalance(request.getReceiverAccountNumber())).thenReturn(new BigDecimal("75000.00"));
        when(scheduleRepo.paymentMethodExists(request.getPaymentModeId())).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> scheduleService.createSchedule(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("PAYMENT_MODE_NOT_FOUND: 1", exception.getReason());
        verify(scheduleRepo, never()).createSchedule(any(Schedule.class));
    }

    @Test
    void createScheduleRejectsUnknownSenderAccount() {
        Schedule request = buildValidSchedule();

        when(accountRepo.getAccountBalance(request.getSenderAccountNumber())).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> scheduleService.createSchedule(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("ACCOUNT_NOT_FOUND: Sender account 100000001 does not exist", exception.getReason());
        verify(scheduleRepo, never()).createSchedule(any(Schedule.class));
    }

    @Test
    void createScheduleRejectsUnknownCurrency() {
        Schedule request = buildValidSchedule();
        request.setCurrencyId(99);

        stubValidDependencies(request);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> scheduleService.createSchedule(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("CURRENCY_NOT_FOUND: 99", exception.getReason());
        verify(scheduleRepo, never()).createSchedule(any(Schedule.class));
    }

    @Test
    void createScheduleRejectsNonPositiveAmount() {
        Schedule request = buildValidSchedule();
        request.setAmount(BigDecimal.ZERO);

        when(accountRepo.getAccountBalance(request.getSenderAccountNumber())).thenReturn(new BigDecimal("50000.00"));
        when(accountRepo.getAccountBalance(request.getReceiverAccountNumber())).thenReturn(new BigDecimal("75000.00"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> scheduleService.createSchedule(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("INVALID_AMOUNT: amount must be greater than zero", exception.getReason());
        verify(scheduleRepo, never()).createSchedule(any(Schedule.class));
    }

    private void stubValidDependencies(Schedule request) {
        when(accountRepo.getAccountBalance(request.getSenderAccountNumber())).thenReturn(new BigDecimal("50000.00"));
        when(accountRepo.getAccountBalance(request.getReceiverAccountNumber())).thenReturn(new BigDecimal("75000.00"));
        when(scheduleRepo.paymentMethodExists(request.getPaymentModeId())).thenReturn(true);
        when(catalogService.getAllCurrencies()).thenReturn(buildCurrencies());
    }

    private Schedule buildValidSchedule() {
        return new Schedule(
                999,
                100000001L,
                100000002L,
                new BigDecimal("2500.00"),
                1,
                1,
                "Rent",
                Frequency.MONTHLY,
                Date.valueOf(LocalDate.now().plusDays(1)),
                Time.valueOf("09:30:00"),
                Date.valueOf(LocalDate.now().plusMonths(6)),
                null,
                null,
                null
        );
    }

    private List<Currency> buildCurrencies() {
        return List.of(
                new Currency(1, "United States", "$", "USD"),
                new Currency(2, "India", "₹", "INR"),
                new Currency(3, "United Kingdom", "£", "GBP"),
                new Currency(4, "European Union", "€", "EUR"),
                new Currency(5, "Japan", "¥", "JPY")
        );
    }
}




