package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.centavo.model.Category;
import com.usc.centavo.repository.CategoryRepository;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository repository;
    private final String userId;

    public CategoryViewModel() {
        repository = CategoryRepository.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadCategories();
        } else {
            userId = null;
        }
    }

    public LiveData<List<Category>> getCategoriesLiveData() {
        return repository.getCategoriesLiveData();
    }

    public LiveData<String> getErrorMessageLiveData() {
        return repository.getErrorMessageLiveData();
    }

    public void onErrorHandled() {
        repository.onErrorHandled();
    }

    public void loadCategories() {
        if (userId != null) {
            repository.getCategoriesForUser(userId);
        }
    }

    public void addCategory(String name, String color) {
        if (userId != null) {
            Category newCategory = new Category(userId, name, color);
            repository.addCategory(newCategory);
        }
    }

    public void updateCategory(Category category) {
        repository.updateCategory(category);
    }

    public void deleteCategory(String categoryId) {
        repository.deleteCategory(categoryId);
    }
}