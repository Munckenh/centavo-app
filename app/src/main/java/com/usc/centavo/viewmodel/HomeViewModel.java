package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.usc.centavo.model.User;
import com.usc.centavo.repository.UserRepository;

public class HomeViewModel extends ViewModel {
    private final UserRepository repository;
    private final LiveData<FirebaseUser> userLiveData;
    private final LiveData<String> errorLiveData;
    private final LiveData<User> userDetailsLiveData;

    public HomeViewModel() {
        repository = UserRepository.getInstance();
        userLiveData = repository.getUserLiveData();
        errorLiveData = repository.getErrorLiveData();
        userDetailsLiveData = repository.getUserDetailsLiveData();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<User> getUserDetailsLiveData() {
        return userDetailsLiveData;
    }

    public void logout() {
        repository.logout();
    }
}
