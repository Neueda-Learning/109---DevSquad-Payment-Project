package com.devsquad.payment_processing.model;

import java.sql.Date;

public class BatchSchedule {
    private Long batchScheduleId;
    private String batchId;
    private Long senderAccountNumber;
    private Integer paymentModeId;
    private String description;
    private Date scheduledDate;
    private BatchScheduleStatus status;

    public BatchSchedule() {
    }

    public BatchSchedule(Long batchScheduleId, String batchId, Long senderAccountNumber,
                         Integer paymentModeId, String description, Date scheduledDate,
                         BatchScheduleStatus status) {
        this.batchScheduleId = batchScheduleId;
        this.batchId = batchId;
        this.senderAccountNumber = senderAccountNumber;
        this.paymentModeId = paymentModeId;
        this.description = description;
        this.scheduledDate = scheduledDate;
        this.status = status;
    }

    public Long getBatchScheduleId() {
        return batchScheduleId;
    }

    public void setBatchScheduleId(Long batchScheduleId) {
        this.batchScheduleId = batchScheduleId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Long getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(Long senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
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

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public BatchScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(BatchScheduleStatus status) {
        this.status = status;
    }
}

