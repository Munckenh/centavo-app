package com.usc.centavo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.usc.centavo.model.Budget;
import com.usc.centavo.utils.OperationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetRepository {
    private static volatile BudgetRepository instance;
    private final FirebaseFirestore db;

    private final MutableLiveData<List<Budget>> budgetsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<OperationStatus> operationStatusLiveData = new MutableLiveData<>();

    private BudgetRepository() {
        db = FirebaseFirestore.getInstance();
        operationStatusLiveData.setValue(OperationStatus.IDLE);
    }

    public static BudgetRepository getInstance() {
        if (instance == null) {
            synchronized (BudgetRepository.class) {
                if (instance == null) {
                    instance = new BudgetRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Budget>> getBudgetsLiveData() {
        return budgetsLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<OperationStatus> getOperationStatusLiveData() {
        return operationStatusLiveData;
    }

    public void addBudget(Budget budget) {
        operationStatusLiveData.setValue(OperationStatus.LOADING);
        db.collection("budgets")
                .add(budget)
                .addOnSuccessListener(documentReference -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error adding budget");
                });
    }

    public void getBudgetsForUser(String userId) {
        db.collection("budgets")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        errorMessageLiveData.postValue("Error fetching budgets");
                        return;
                    }
                    if (value != null) {
                        List<Budget> budgets = value.toObjects(Budget.class);
                        budgetsLiveData.postValue(budgets);
                    }
                });
    }

    public void updateBudget(Budget budget) {
        if (budget == null) {
            operationStatusLiveData.postValue(OperationStatus.FAILURE);
            errorMessageLiveData.postValue("Budget is null");
            return;
        }
        if (budget.getBudgetId() == null || budget.getBudgetId().isEmpty()) {
            operationStatusLiveData.postValue(OperationStatus.FAILURE);
            errorMessageLiveData.postValue("Budget ID cannot be null or empty");
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoryId", budget.getCategoryId());
        updates.put("amount", budget.getAmount());
        updates.put("period", budget.getPeriod());
        updates.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("budgets")
                .document(budget.getBudgetId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error updating budget");
                });
    }

    public void deleteBudget(String budgetId) {
        operationStatusLiveData.setValue(OperationStatus.LOADING);
        db.collection("budgets")
                .document(budgetId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error deleting budget");
                });
    }

    public void onSuccessHandled() {
        operationStatusLiveData.setValue(OperationStatus.IDLE);
    }

    public void onErrorHandled() {
        operationStatusLiveData.setValue(OperationStatus.IDLE);
        errorMessageLiveData.setValue(null);
    }

    public void clear() {
        budgetsLiveData.setValue(null);
        errorMessageLiveData.setValue(null);
    }
} 