package com.usc.centavo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.usc.centavo.model.Transaction;
import com.usc.centavo.utils.OperationStatus;
import com.usc.centavo.repository.AccountRepository;
import com.usc.centavo.model.Account;

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
                .addOnSuccessListener(documentReference -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                    // Update account balance if needed
                    if (transaction.getAccountId() != null && !transaction.getAccountId().isEmpty()) {
                        updateAccountBalanceAfterAdd(transaction);
                    }
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error adding transaction");
                });
    }

    private void updateAccountBalanceAfterAdd(Transaction transaction) {
        db.collection("accounts").document(transaction.getAccountId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Account account = documentSnapshot.toObject(Account.class);
                    if (account != null) {
                        double newBalance = account.getBalance();
                        if ("Income".equalsIgnoreCase(transaction.getType())) {
                            newBalance += transaction.getAmount();
                        } else {
                            newBalance -= transaction.getAmount();
                        }
                        account.setBalance(newBalance);
                        db.collection("accounts").document(account.getAccountId()).set(account);
                    }
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

        // Fetch the old transaction to adjust balances
        db.collection("transactions").document(transaction.getTransactionId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Transaction oldTransaction = documentSnapshot.toObject(Transaction.class);
                    // Proceed with update
                    updateTransactionAndBalances(transaction, oldTransaction);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error updating transaction");
                });
    }

    private void updateTransactionAndBalances(Transaction newTx, Transaction oldTx) {
        // Update Firestore transaction document
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoryId", newTx.getCategoryId());
        updates.put("accountId", newTx.getAccountId());
        updates.put("amount", newTx.getAmount());
        updates.put("description", newTx.getDescription());
        updates.put("transactionDate", newTx.getTransactionDate());
        updates.put("type", newTx.getType());
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("transactions").document(newTx.getTransactionId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    operationStatusLiveData.postValue(OperationStatus.SUCCESS);
                    // Adjust balances if needed
                    updateAccountBalanceAfterEdit(newTx, oldTx);
                })
                .addOnFailureListener(e -> {
                    operationStatusLiveData.postValue(OperationStatus.FAILURE);
                    errorMessageLiveData.postValue("Error updating transaction");
                });
    }

    private void updateAccountBalanceAfterEdit(Transaction newTx, Transaction oldTx) {
        String oldAccountId = oldTx != null ? oldTx.getAccountId() : null;
        String newAccountId = newTx.getAccountId();
        double oldAmount = oldTx != null ? oldTx.getAmount() : 0;
        double newAmount = newTx.getAmount();
        String oldType = oldTx != null ? oldTx.getType() : null;
        String newType = newTx.getType();

        if (oldAccountId != null && !oldAccountId.isEmpty() && oldAccountId.equals(newAccountId)) {
            // Same account: revert old, apply new in one update
            db.collection("accounts").document(newAccountId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Account account = documentSnapshot.toObject(Account.class);
                    if (account != null) {
                        double balance = account.getBalance();
                        // Revert old
                        if ("Income".equalsIgnoreCase(oldType)) {
                            balance -= oldAmount;
                        } else {
                            balance += oldAmount;
                        }
                        // Apply new
                        if ("Income".equalsIgnoreCase(newType)) {
                            balance += newAmount;
                        } else {
                            balance -= newAmount;
                        }
                        account.setBalance(balance);
                        db.collection("accounts").document(account.getAccountId()).set(account);
                    }
                });
        } else {
            // Different accounts: revert old on old account, apply new on new account
            if (oldAccountId != null && !oldAccountId.isEmpty()) {
                db.collection("accounts").document(oldAccountId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Account account = documentSnapshot.toObject(Account.class);
                        if (account != null) {
                            double balance = account.getBalance();
                            if ("Income".equalsIgnoreCase(oldType)) {
                                balance -= oldAmount;
                            } else {
                                balance += oldAmount;
                            }
                            account.setBalance(balance);
                            db.collection("accounts").document(account.getAccountId()).set(account);
                        }
                    });
            }
            if (newAccountId != null && !newAccountId.isEmpty()) {
                db.collection("accounts").document(newAccountId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Account account = documentSnapshot.toObject(Account.class);
                        if (account != null) {
                            double balance = account.getBalance();
                            if ("Income".equalsIgnoreCase(newType)) {
                                balance += newAmount;
                            } else {
                                balance -= newAmount;
                            }
                            account.setBalance(balance);
                            db.collection("accounts").document(account.getAccountId()).set(account);
                        }
                    });
            }
        }
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