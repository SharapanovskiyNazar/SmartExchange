package com.futurecoders.smartexchange.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "currency_rates")
public class CurrencyRateEntity {

    @PrimaryKey(autoGenerate = true)
    private int rateId;
    private String currency;
    private double buyRate;
    private double sellRate;
    private String rateDate;

    public CurrencyRateEntity(String currency, double buyRate, double sellRate, String rateDate) {
        this.currency = currency;
        this.buyRate = buyRate;
        this.sellRate = sellRate;
        this.rateDate = rateDate;
    }

    public int getRateId() {
        return rateId;
    }

    public void setRateId(int rateId) {
        this.rateId = rateId;
    }

    public String getCurrency() {
        return currency;
    }

    public double getBuyRate() {
        return buyRate;
    }

    public double getSellRate() {
        return sellRate;
    }

    public String getRateDate() {
        return rateDate;
    }
}
