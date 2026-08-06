package com.devsquad.payment_processing.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class BatchPaymentRequest {

    @NotNull(message = "Sender account number is required")
    private Long senderAccountNumber;

    @NotNull(message = "Payment mode ID is required")
    private Integer paymentModeId;

    private String description;

    // Optional. When present, batch should be executed by scheduler on this date.
    private LocalDate scheduledDate;

    @NotEmpty(message = "Recipients list cannot be empty")
    @Valid
    private List<BatchPaymentRecipient> recipients;

    public BatchPaymentRequest() {}

    public BatchPaymentRequest(Long senderAccountNumber, Integer paymentModeId, Integer currencyId,
                               String description, List<BatchPaymentRecipient> recipients) {
        this.senderAccountNumber = senderAccountNumber;
        this.paymentModeId = paymentModeId;
        this.description = description;
        this.recipients = recipients;
    }

    public Long getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(Long senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }

    public Integer getPaymentModeId() { return paymentModeId; }
    public void setPaymentModeId(Integer paymentModeId) { this.paymentModeId = paymentModeId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public List<BatchPaymentRecipient> getRecipients() { return recipients; }
    public void setRecipients(List<BatchPaymentRecipient> recipients) { this.recipients = recipients; }
}

