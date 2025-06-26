package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.usc.centavo.repository.UserRepository;

public class RegisterViewModel extends ViewModel {
    private final UserRepository repository;
    private final LiveData<FirebaseUser> userLiveData;
    private final LiveData<String> errorLiveData;

    public RegisterViewModel() {
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

    public void register(String email, String password, String firstName, String lastName) {
        repository.register(email, password, firstName, lastName);
    }
}
