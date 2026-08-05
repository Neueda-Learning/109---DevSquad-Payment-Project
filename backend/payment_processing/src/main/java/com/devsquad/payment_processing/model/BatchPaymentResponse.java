package com.devsquad.payment_processing.model;

import java.util.ArrayList;
import java.util.List;

public class BatchPaymentResponse {

    private String batchId;
    private int totalPayments;
    private int successfulPayments;
    private int failedPayments;
    private List<PaymentResult> results;

    public BatchPaymentResponse() {
        this.results = new ArrayList<>();
    }

    public BatchPaymentResponse(String batchId) {
        this.batchId = batchId;
        this.results = new ArrayList<>();
    }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public int getTotalPayments() { return totalPayments; }
    public void setTotalPayments(int totalPayments) { this.totalPayments = totalPayments; }

    public int getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(int successfulPayments) { this.successfulPayments = successfulPayments; }

    public int getFailedPayments() { return failedPayments; }
    public void setFailedPayments(int failedPayments) { this.failedPayments = failedPayments; }

    public List<PaymentResult> getResults() { return results; }
    public void setResults(List<PaymentResult> results) { this.results = results; }

    // Nested class for individual payment result
    public static class PaymentResult {
        private Long receiverAccountNumber;
        private Double amount;
        private Integer paymentId;
        private String status;       // "SUCCESS" or "FAILED"
        private String errorMessage; // Only for failed payments

        public PaymentResult() {}

        public PaymentResult(Long receiverAccountNumber, Double amount, Integer paymentId,
                             String status, String errorMessage) {
            this.receiverAccountNumber = receiverAccountNumber;
            this.amount = amount;
            this.paymentId = paymentId;
            this.status = status;
            this.errorMessage = errorMessage;
        }

        public Long getReceiverAccountNumber() { return receiverAccountNumber; }
        public void setReceiverAccountNumber(Long receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public Integer getPaymentId() { return paymentId; }
        public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

