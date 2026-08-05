package com.devsquad.payment_processing.model;

import java.sql.Date;
import java.sql.Time;

public class Payment {

    public enum Status {
        CREATED,
        VALIDATED,
        SENT,
        COMPLETED,
        FAILED
    }

    private Integer paymentId;
    private String invoiceNumber;
    private Long senderAccountNumber;
    private Long receiverAccountNumber;
    private Double amount;
    private Integer currencyId;
    private Integer paymentModeId;
    private Date paymentDate;
    private Time paymentTime;
    private String description;
    private Integer scheduleId;        // nullable — links to Schedule if this was auto-executed
    private Status status;

    public Payment(Integer paymentId, String invoiceNumber, Long senderAccountNumber, Long receiverAccountNumber,
                   Double amount, Integer currencyId, Integer paymentModeId, Date paymentDate, Time paymentTime,
                   String description, Integer scheduleId, Status status) {
        this.paymentId = paymentId;
        this.invoiceNumber = invoiceNumber;
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.currencyId = currencyId;
        this.paymentModeId = paymentModeId;
        this.paymentDate = paymentDate;
        this.paymentTime = paymentTime;
        this.description = description;
        this.scheduleId = scheduleId;
        this.status = status;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
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

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Time getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Time paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
