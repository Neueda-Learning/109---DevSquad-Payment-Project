package com.devsquad.payment_processing.model;

public class Currency {

    private Integer currencyId;
    private String currencyCountry;
    private String currencySymbol;
    private String currencyName;

    // Constructor
    public Currency(Integer currencyId, String currencyCountry,
                    String currencySymbol, String currencyName) {
        this.currencyId = currencyId;
        this.currencyCountry = currencyCountry;
        this.currencySymbol = currencySymbol;
        this.currencyName = currencyName;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrencyCountry() {
        return currencyCountry;
    }

    public void setCurrencyCountry(String currencyCountry) {
        this.currencyCountry = currencyCountry;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }
}