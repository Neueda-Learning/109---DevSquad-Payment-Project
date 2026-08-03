package com.devsquad.payment_processing.model;

import java.time.LocalDate;

public class CreditCard {

    private String cardNumber;
    private String bank;
    private String cvv;
    private LocalDate expiryDate;
    private String holderName;

    private Long userId;
    private User user;

    public CreditCard() {
    }

    public CreditCard(String cardNumber, String bank, String cvv,
                      LocalDate expiryDate, String holderName,
                      Long userId) {
        this.cardNumber = cardNumber;
        this.bank = bank;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.holderName = holderName;
        this.userId = userId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "CreditCard{" +
                "cardNumber='" + cardNumber + '\'' +
                ", bank='" + bank + '\'' +
                ", holderName='" + holderName + '\'' +
                ", expiryDate=" + expiryDate +
                ", userId=" + userId +
                '}';
    }
}