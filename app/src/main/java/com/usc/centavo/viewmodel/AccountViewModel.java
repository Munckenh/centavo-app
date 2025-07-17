package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.centavo.model.Account;
import com.usc.centavo.repository.AccountRepository;

import java.util.List;

public class AccountViewModel extends ViewModel {
    private final AccountRepository repository;
    private final String userId;

    public AccountViewModel() {
        repository = AccountRepository.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadAccounts();
        } else {
            userId = null;
        }
    }

    public LiveData<List<Account>> getAccountsLiveData() {
        return repository.getAccountsLiveData();
    }

    public LiveData<String> getErrorMessageLiveData() {
        return repository.getErrorMessageLiveData();
    }

    public void onErrorHandled() {
        repository.onErrorHandled();
    }

    public void loadAccounts() {
        if (userId != null) {
            repository.getAccountsForUser(userId);
        }
    }

    public void addAccount(String name, double balance) {
        if (userId != null) {
            Account newAccount = new Account(userId, name, balance);
            repository.addAccount(newAccount);
        }
    }

    public void updateAccount(Account account) {
        repository.updateAccount(account);
    }

    public void deleteAccount(String accountId) {
        repository.deleteAccount(accountId);
    }
} 