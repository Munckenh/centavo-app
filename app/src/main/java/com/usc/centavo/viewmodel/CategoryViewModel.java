package com.usc.centavo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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

    public void deleteCategoryAndTransactions(String categoryId) {
        // Delete all transactions with this categoryId
        FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().delete();
                }
                // Now delete the category
                repository.deleteCategory(categoryId);
            });
    }

    public void deleteCategory(String categoryId) {
        deleteCategoryAndTransactions(categoryId);
    }
}