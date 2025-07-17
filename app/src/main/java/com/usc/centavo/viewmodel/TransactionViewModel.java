package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.usc.centavo.model.Transaction;
import com.usc.centavo.repository.TransactionRepository;
import com.usc.centavo.utils.OperationStatus;

import java.util.List;

public class TransactionViewModel extends ViewModel {
    private final TransactionRepository repository;

    public TransactionViewModel() {
        repository = TransactionRepository.getInstance();
    }

    public LiveData<List<Transaction>> getTransactionsLiveData() {
        return repository.getTransactionsLiveData();
    }

    public LiveData<String> getErrorMessageLiveData() {
        return repository.getErrorMessageLiveData();
    }

    public LiveData<OperationStatus> getOperationStatusLiveData() {
        return repository.getOperationStatusLiveData();
    }

    public void onErrorHandled() {
        repository.onErrorHandled();
    }

    public void onSuccessHandled() {
        repository.onSuccessHandled();
    }

    public void getTransactionsForUser(String userId) {
        repository.getTransactionsForUser(userId);
    }

    public void addTransaction(Transaction transaction) {
        repository.addTransaction(transaction);
    }

    public LiveData<Transaction> getTransactionById(String transactionId) {
        return repository.getTransactionById(transactionId);
    }

    public void updateTransaction(Transaction transaction) {
        repository.updateTransaction(transaction);
    }

    public void deleteTransaction(String transactionId) {
        repository.deleteTransaction(transactionId);
    }

    public void getTransactionsForUserByCategoryAndDate(String userId, String categoryId, java.util.Date startDate, java.util.Date endDate) {
        repository.getTransactionsForUserByCategoryAndDate(userId, categoryId, startDate, endDate);
    }

    public LiveData<List<Transaction>> getFilteredTransactionsLiveData() {
        return repository.getFilteredTransactionsLiveData();
    }
}