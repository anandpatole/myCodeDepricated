package com.cheep.model;

public class RateCardModel

{
    private String product;
    private String rate;

    public String getRate_unit() {
        return rate_unit;
    }

    public void setRate_unit(String rate_unit) {
        this.rate_unit = rate_unit;
    }

    private String rate_unit;
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
