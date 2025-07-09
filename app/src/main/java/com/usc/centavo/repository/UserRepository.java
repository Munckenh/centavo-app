package com.usc.centavo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usc.centavo.model.User;

public class UserRepository {
    private static volatile UserRepository instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(task -> {
                    fetchCurrentUser();
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
                        errorMessageLiveData.postValue("Invalid email or password");
                    } else {
                        errorMessageLiveData.postValue("Login failed. Please try again.");
                    }
                });
    }

    public void register(String email, String password, String firstName, String lastName) {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            errorMessageLiveData.postValue("First and last name cannot be empty");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(task -> {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        User newUser = new User(email, firstName, lastName);
                        db.collection("users").document(uid)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    userLiveData.postValue(newUser);
                                })
                                .addOnFailureListener(e -> {
                                    errorMessageLiveData.postValue("Error saving user data");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessageLiveData.postValue("Registration failed");
                });
    }

    public void logout() {
        mAuth.signOut();
        clear();
        TransactionRepository.getInstance().clear();
        CategoryRepository.getInstance().clear();
    }

    public void fetchCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            fetchCurrentUser(uid);
        } else {
            errorMessageLiveData.postValue("No user is currently logged in");
        }
    }

    public void fetchCurrentUser(String uid) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    userLiveData.postValue(user);
                } else {
                    errorMessageLiveData.postValue("User not found");
                    logout();
                }
            })
            .addOnFailureListener(e -> {
                errorMessageLiveData.postValue("Error fetching user");
            });
    }

    public void onErrorHandled() {
        errorMessageLiveData.postValue(null);
    }

    public void clear() {
        userLiveData.postValue(null);
        errorMessageLiveData.postValue(null);
    }
}
