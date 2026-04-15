package com.futurecoders.smartexchange.data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "accounts",
        indices = {@Index(value = {"userId", "currency"}, unique = true)}
)
public class AccountEntity {

    @PrimaryKey(autoGenerate = true)
    private int accountId;

    private int userId;
    private String currency;
    private double balance;

    public AccountEntity(int userId, String currency, double balance) {
        this.userId = userId;
        this.currency = currency;
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public String getCurrency() {
        return currency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
