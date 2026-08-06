package com.devsquad.payment_processing.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

public class Schedule {

    private Integer scheduleId;

    @NotNull(message = "Sender account number is required")
    private Long senderAccountNumber;

    @NotNull(message = "Receiver account number is required")
    private Long receiverAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    @Positive(message = "Currency ID must be positive")
    private Integer currencyId;

    @NotNull(message = "Payment mode is required")
    @Positive(message = "Payment mode ID must be positive")
    private Integer paymentModeId;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    @NotNull(message = "Start date is required")
    private Date startDate;
    @NotNull(message = "Scheduled time is required")
    private Time scheduledTime;
    private Date endDate;           // nullable
    private Date nextRunDate;
    private Date lastRunDate;       // nullable — null until first trigger
    private ScheduleStatus status;

    public Schedule() {
    }

    public Schedule(Integer scheduleId, Long senderAccountNumber, Long receiverAccountNumber,
                    BigDecimal amount, Integer currencyId, Integer paymentModeId,
                    String description, Frequency frequency,
                    Date startDate, Time scheduledTime, Date endDate,
                    Date nextRunDate, Date lastRunDate,
                    ScheduleStatus status) {
        this.scheduleId = scheduleId;
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.currencyId = currencyId;
        this.paymentModeId = paymentModeId;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
        this.scheduledTime = scheduledTime;
        this.endDate = endDate;
        this.nextRunDate = nextRunDate;
        this.lastRunDate = lastRunDate;
        this.status = status;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(Long senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public Long getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(Long receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public Integer getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(Integer paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Time getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Time scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getNextRunDate() {
        return nextRunDate;
    }

    public void setNextRunDate(Date nextRunDate) {
        this.nextRunDate = nextRunDate;
    }

    public Date getLastRunDate() {
        return lastRunDate;
    }

    public void setLastRunDate(Date lastRunDate) {
        this.lastRunDate = lastRunDate;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }
}

