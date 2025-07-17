package com.usc.centavo.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Budget {
    @DocumentId
    private String budgetId;
    private String userId;
    private String categoryId;
    private double amount;
    private String period; // "daily", "weekly", "monthly"
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    public Budget() {}

    public Budget(String userId, String categoryId, double amount, String period) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
    }

    public String getBudgetId() { return budgetId; }
    public void setBudgetId(String budgetId) { this.budgetId = budgetId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
} 