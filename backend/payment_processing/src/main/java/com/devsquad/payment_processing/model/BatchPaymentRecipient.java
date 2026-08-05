package com.devsquad.payment_processing.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BatchPaymentRecipient {

    @NotNull(message = "Receiver account number is required")
    private Long receiverAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;  // Optional per-recipient description

    public BatchPaymentRecipient() {}

    public BatchPaymentRecipient(Long receiverAccountNumber, BigDecimal amount, String description) {
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.description = description;
    }

    public Long getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(Long receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

