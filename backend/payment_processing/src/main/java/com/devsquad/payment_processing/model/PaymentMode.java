package com.devsquad.payment_processing.model;

public class PaymentMode {

    private Integer paymentModeId;
    private String modeName;
    private String description;
    private Boolean active;

    public PaymentMode(Integer paymentModeId,
                       String modeName,
                       String description,
                       Boolean active) {

        this.paymentModeId = paymentModeId;
        this.modeName = modeName;
        this.description = description;
        this.active = active;
    }

    public Integer getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(Integer paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}