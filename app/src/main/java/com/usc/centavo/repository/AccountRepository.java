package com.usc.centavo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.usc.centavo.model.Account;

import java.util.List;

public class AccountRepository {
    private static volatile AccountRepository instance;
    private final FirebaseFirestore db;

    private final MutableLiveData<List<Account>> accountsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public AccountRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static AccountRepository getInstance() {
        if (instance == null) {
            synchronized (AccountRepository.class) {
                if (instance == null) {
                    instance = new AccountRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Account>> getAccountsLiveData() {
        return accountsLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void addAccount(Account account) {
        db.collection("accounts")
                .add(account)
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Error adding account");
                });
    }

    public void getAccountsForUser(String userId) {
        db.collection("accounts")
                .whereEqualTo("userId", userId)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        errorMessageLiveData.postValue("Error fetching accounts");
                        return;
                    }
                    if (value != null) {
                        List<Account> accounts = value.toObjects(Account.class);
                        accountsLiveData.postValue(accounts);
                    }
                });
    }

    public void updateAccount(Account account) {
        if (account == null) {
            errorMessageLiveData.postValue("Account is null");
            return;
        }
        if (account.getAccountId() == null || account.getAccountId().isEmpty()) {
            errorMessageLiveData.postValue("Account ID cannot be null or empty");
            return;
        }
        db.collection("accounts")
                .document(account.getAccountId())
                .set(account)
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Error updating account");
                });
    }

    public void deleteAccount(String accountId) {
        db.collection("accounts")
                .document(accountId)
                .delete()
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Error deleting account");
                });
    }

    public void deleteAccountAndNullTransactions(String accountId) {
        db.collection("transactions")
            .whereEqualTo("accountId", accountId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                    updates.put("accountId", null);
                    doc.getReference().update(updates);
                }
                // Now delete the account
                deleteAccount(accountId);
            });
    }

    public void onErrorHandled() {
        errorMessageLiveData.setValue(null);
    }

    public void clear() {
        accountsLiveData.postValue(null);
        errorMessageLiveData.postValue(null);
    }
} 