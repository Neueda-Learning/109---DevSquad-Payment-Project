package com.devsquad.payment_processing.model;

import java.math.BigDecimal;

public class Account {

    private Long accountNumber;

    private Long userId;

    private String bankName;
    private AccountType accountType;

    private BigDecimal balance;

    private String ifsc;
    private String bankAddress;
    private String country;

    private boolean active;
    private String notActiveReason;

    public Account() {
    }

    public Account(Long accountNumber,
                   Long userId,
                   String bankName,
                   AccountType accountType,
                   BigDecimal balance,
                   String ifsc,
                   String bankAddress,
                   String country,
                   boolean active,
                   String notActiveReason) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.bankName = bankName;
        this.accountType = accountType;
        this.balance = balance;
        this.ifsc = ifsc;
        this.bankAddress = bankAddress;
        this.country = country;
        this.active = active;
        this.notActiveReason = notActiveReason;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(String bankAddress) {
        this.bankAddress = bankAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNotActiveReason() {
        return notActiveReason;
    }

    public void setNotActiveReason(String notActiveReason) {
        this.notActiveReason = notActiveReason;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber=" + accountNumber +
                ", bankName='" + bankName + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                ", active=" + active +
                '}';
    }
}