package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.usc.centavo.model.User;
import com.usc.centavo.repository.UserRepository;

public class LoginViewModel extends ViewModel {
    private final UserRepository repository;

    public LoginViewModel() {
        repository = UserRepository.getInstance();
    }

    public LiveData<User> getUserLiveData() {
        return repository.getUserLiveData();
    }

    public LiveData<String> getErrorMessageLiveData() {
        return repository.getErrorMessageLiveData();
    }

    public void onErrorHandled() {
        repository.onErrorHandled();
    }

    public void login(String email, String password) {
        repository.login(email, password);
    }
}
