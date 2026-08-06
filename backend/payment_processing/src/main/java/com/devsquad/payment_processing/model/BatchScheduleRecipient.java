package com.devsquad.payment_processing.model;

import java.math.BigDecimal;

public class BatchScheduleRecipient {
    private Long receiverAccountNumber;
    private BigDecimal amount;
    private Integer currencyId;
    private String description;

    public BatchScheduleRecipient() {
    }

    public BatchScheduleRecipient(Long receiverAccountNumber, BigDecimal amount, Integer currencyId, String description) {
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.currencyId = currencyId;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

