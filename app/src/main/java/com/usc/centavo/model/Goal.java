package com.usc.centavo.model;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;
import java.util.Objects;

public class Goal {
    @DocumentId
    private String goalId;
    private String userId;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private Date deadline;
    private boolean isCompleted;

    public Goal() {
        // Public no-arg constructor needed for Firestore
    }

    public Goal(String userId, String goalName, double targetAmount, Date deadline) {
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
        this.currentAmount = 0.0;
        this.isCompleted = false;
    }

    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal goal = (Goal) o;
        return Double.compare(goal.targetAmount, targetAmount) == 0 &&
                Double.compare(goal.currentAmount, currentAmount) == 0 &&
                isCompleted == goal.isCompleted &&
                Objects.equals(goalId, goal.goalId) &&
                Objects.equals(userId, goal.userId) &&
                Objects.equals(goalName, goal.goalName) &&
                Objects.equals(deadline, goal.deadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goalId, userId, goalName, targetAmount, currentAmount, deadline, isCompleted);
    }
} 