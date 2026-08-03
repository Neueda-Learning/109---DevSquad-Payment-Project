package com.devsquad.payment_processing.model;

import java.util.ArrayList;

public class User {

    private Long userId;
    private String name;
    private String mobile;

    private ArrayList<Account> accounts;
    private ArrayList<CreditCard> creditCards;

    public User() {
        this.accounts = new ArrayList<>();
        this.creditCards = new ArrayList<>();
    }

    public User(Long userId, String name, String mobile) {
        this.userId = userId;
        this.name = name;
        this.mobile = mobile;
        this.accounts = new ArrayList<>();
        this.creditCards = new ArrayList<>();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }

    public ArrayList<CreditCard> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(ArrayList<CreditCard> creditCards) {
        this.creditCards = creditCards;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}