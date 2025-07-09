package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.usc.centavo.model.User;
import com.usc.centavo.repository.UserRepository;

public class RegisterViewModel extends ViewModel {
    private final UserRepository repository;

    public RegisterViewModel() {
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

    public void register(String email, String password, String firstName, String lastName) {
        repository.register(email, password, firstName, lastName);
    }
}
