package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.usc.centavo.model.User;
import com.usc.centavo.repository.UserRepository;

public class ProfileViewModel extends ViewModel {
    private final UserRepository repository;
    private final LiveData<User> userLiveData;

    public ProfileViewModel() {
        repository = UserRepository.getInstance();
        userLiveData = repository.getUserLiveData();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void logout() {
        repository.logout();
    }
}