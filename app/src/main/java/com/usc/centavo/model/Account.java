package com.usc.centavo.model;

import com.google.firebase.firestore.DocumentId;

public class Account {
    @DocumentId
    private String accountId;
    private String userId;
    private String name;
    private double balance;

    public Account() {}

    public Account(String userId, String name, double balance) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
} 