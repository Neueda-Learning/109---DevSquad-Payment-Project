package com.devsquad.payment_processing.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BatchPaymentRecipient {

    @NotNull(message = "Receiver account number is required")
    private Long receiverAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String description;  // Optional per-recipient description

    public BatchPaymentRecipient() {}

    public BatchPaymentRecipient(Long receiverAccountNumber, Double amount, String description) {
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.description = description;
    }

    public Long getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(Long receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

