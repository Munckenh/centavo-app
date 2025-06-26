package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.usc.centavo.repository.UserRepository;

public class LoginViewModel extends ViewModel {
    private final UserRepository repository;
    private final LiveData<FirebaseUser> userLiveData;
    private final LiveData<String> errorLiveData;

    public LoginViewModel() {
        repository = UserRepository.getInstance();
        userLiveData = repository.getUserLiveData();
        errorLiveData = repository.getErrorLiveData();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void login(String email, String password) {
        repository.login(email, password);
    }
}
