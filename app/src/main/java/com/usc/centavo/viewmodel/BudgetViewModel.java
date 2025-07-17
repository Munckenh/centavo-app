package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.usc.centavo.model.Budget;
import com.usc.centavo.repository.BudgetRepository;
import com.usc.centavo.utils.OperationStatus;

import java.util.List;

public class BudgetViewModel extends ViewModel {
    private final BudgetRepository budgetRepository;

    public BudgetViewModel() {
        budgetRepository = BudgetRepository.getInstance();
    }

    public LiveData<List<Budget>> getBudgetsLiveData() {
        return budgetRepository.getBudgetsLiveData();
    }

    public LiveData<String> getErrorMessageLiveData() {
        return budgetRepository.getErrorMessageLiveData();
    }

    public LiveData<OperationStatus> getOperationStatusLiveData() {
        return budgetRepository.getOperationStatusLiveData();
    }

    public void addBudget(Budget budget) {
        budgetRepository.addBudget(budget);
    }

    public void getBudgetsForUser(String userId) {
        budgetRepository.getBudgetsForUser(userId);
    }

    public void updateBudget(Budget budget) {
        budgetRepository.updateBudget(budget);
    }

    public void deleteBudget(String budgetId) {
        budgetRepository.deleteBudget(budgetId);
    }

    public void onSuccessHandled() {
        budgetRepository.onSuccessHandled();
    }

    public void onErrorHandled() {
        budgetRepository.onErrorHandled();
    }

    public void clear() {
        budgetRepository.clear();
    }
} 