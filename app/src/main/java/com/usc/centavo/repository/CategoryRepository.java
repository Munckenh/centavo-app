package com.usc.centavo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.usc.centavo.model.Category;

import java.util.List;

public class CategoryRepository {

    private static volatile CategoryRepository instance;
    private final FirebaseFirestore db;

    private final MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public CategoryRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static CategoryRepository getInstance() {
        if (instance == null) {
            synchronized (CategoryRepository.class) {
                if (instance == null) {
                    instance = new CategoryRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Category>> getCategoriesLiveData() {
        return categoriesLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void addCategory(Category category) {
        db.collection("categories")
                .add(category)
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Error adding category");
                });
    }

    public void getCategoriesForUser(String userId) {
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        errorMessageLiveData.postValue("Error fetching categories");
                        return;
                    }

                    if (value != null) {
                        List<Category> categories = value.toObjects(Category.class);
                        categoriesLiveData.postValue(categories);
                    }
                });
    }

    public void updateCategory(Category category) {
        if (category == null) {
            errorMessageLiveData.postValue("Category is null");
            return;
        }
        if (category.getCategoryId() == null || category.getCategoryId().isEmpty()) {
            errorMessageLiveData.postValue("Category ID cannot be null or empty");
            return;
        }

        db.collection("categories")
                .document(category.getCategoryId())
                .set(category)
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Error updating category");
                });
    }

    public void deleteCategory(String categoryId) {
        db.collection("categories")
                .document(categoryId)
                .delete()
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Error deleting category");
                });
    }

    public void onErrorHandled() {
        errorMessageLiveData.setValue(null);
    }

    public void clear() {
        categoriesLiveData.postValue(null);
        errorMessageLiveData.postValue(null);
    }

    public void createDefaultCategoriesIfNone(String userId) {
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        addCategory(new Category(userId, "Food & Drink", "#FF9800")); // Orange
                        addCategory(new Category(userId, "Transport", "#2196F3")); // Blue
                        addCategory(new Category(userId, "Self-Care", "#E91E63")); // Pink
                        addCategory(new Category(userId, "Shopping", "#4CAF50")); // Green
                        addCategory(new Category(userId, "Health", "#F44336")); // Red
                    }
                });
    }
}