package com.usc.centavo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.usc.centavo.model.Transaction;
import com.usc.centavo.utils.OperationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionRepository {
    private static volatile TransactionRepository instance;
    private final FirebaseFirestore db;

    private final MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<OperationStatus> operationStatusLiveData = new MutableLiveData<>();

    private TransactionRepository() {
        db = FirebaseFirestore.getInstance();
        operationStatusLiveData.setValue(OperationStatus.IDLE);
    }

    public static TransactionRepository getInstance() {
        if (instance == null) {
            synchronized (TransactionRepository.class) {
                if (instance == null) {
                    instance = new TransactionRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Transaction>> getTransactionsLiveData() {
        return transactionsLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<OperationStatus> getOperationStatusLiveData() {
        return operationStatusLiveData;
    }

    public void addTransaction(Transaction transaction) {
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(aVoid -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error adding transaction");
                });
    }

    public void getTransactionsForUser(String userId) {
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("transactionDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        errorMessageLiveData.postValue("Error fetching transactions");
                        return;
                    }

                    if (value != null) {
                        List<Transaction> transactions = value.toObjects(Transaction.class);
                        transactionsLiveData.postValue(transactions);
                    }
                });
    }

    public LiveData<Transaction> getTransactionById(String transactionId) {
        MutableLiveData<Transaction> transactionLiveData = new MutableLiveData<>();
        db.collection("transactions").document(transactionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Transaction transaction = documentSnapshot.toObject(Transaction.class);
                        if (transaction != null) {
                            transaction.setTransactionId(documentSnapshot.getId());
                            transactionLiveData.postValue(transaction);
                        }
                    } else {
                        transactionLiveData.postValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Error fetching transaction");
                    transactionLiveData.postValue(null);
                });
        return transactionLiveData;
    }

    public void updateTransaction(Transaction transaction) {
        if (transaction == null) {
            operationStatusLiveData.postValue(OperationStatus.FAILURE);
            errorMessageLiveData.postValue("Transaction data is null.");
            return;
        }
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
            operationStatusLiveData.postValue(OperationStatus.FAILURE);
            errorMessageLiveData.postValue("Transaction ID is missing.");
            return;
        }

        operationStatusLiveData.postValue(OperationStatus.LOADING);
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoryId", transaction.getCategoryId());
        updates.put("accountId", transaction.getAccountId());
        updates.put("amount", transaction.getAmount());
        updates.put("description", transaction.getDescription());
        updates.put("transactionDate", transaction.getTransactionDate());
        updates.put("type", transaction.getType());
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("transactions").document(transaction.getTransactionId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error updating transaction");
                });
    }

    public void deleteTransaction(String transactionId) {
        operationStatusLiveData.setValue(OperationStatus.LOADING);
        db.collection("transactions").document(transactionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error deleting transaction");
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
        transactionsLiveData.setValue(null);
        errorMessageLiveData.setValue(null);
    }
}