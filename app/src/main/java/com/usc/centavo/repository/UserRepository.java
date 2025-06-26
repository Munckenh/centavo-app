package com.usc.centavo.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usc.centavo.model.User;

public class UserRepository {
    public static final String TAG = "UserRepository";
    private static volatile UserRepository instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> userDetailsLiveData = new MutableLiveData<>();

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

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<User> getUserDetailsLiveData() {
        return userDetailsLiveData;
    }

    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(task -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Log.d(TAG, "User logged in successfully: " + uid);
                                        User userDetails = documentSnapshot.toObject(User.class);
                                        userDetailsLiveData.postValue(userDetails);
                                    } else {
                                        Log.w(TAG, "No user details found for UID: " + uid);
                                        errorLiveData.postValue("User details not found");
                                        logout();
                                    }
                                    userLiveData.postValue(user);
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error fetching user details: ", e);
                                    errorLiveData.postValue(e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Login failed: ", e);
                    errorLiveData.postValue(e.getMessage());
                });
    }

    public void register(String email, String password, String firstName, String lastName) {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            errorLiveData.postValue("First and last name cannot be empty");
            Log.w(TAG, "First and last name cannot be empty");
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
                                    Log.d(TAG, "User registered successfully: " + uid);
                                    userDetailsLiveData.postValue(newUser);
                                    userLiveData.postValue(firebaseUser);
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error saving user data: ", e);
                                    errorLiveData.postValue(e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Registration failed: ", e);
                    errorLiveData.postValue(e.getMessage());
                });
    }

    public void logout() {
        mAuth.signOut();
        userLiveData.postValue(null);
        userDetailsLiveData.postValue(null);
        Log.d(TAG, "User logged out successfully");
    }

    public void fetchUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "User details fetched successfully for UID: " + uid);
                        User userDetails = documentSnapshot.toObject(User.class);
                        userDetailsLiveData.postValue(userDetails);
                    } else {
                        Log.w(TAG, "No user details found for UID: " + uid);
                        errorLiveData.postValue("User details not found");
                        logout();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching user details: ", e);
                    errorLiveData.postValue(e.getMessage());
                });
        } else {
            Log.w(TAG, "No user is currently logged in");
            userDetailsLiveData.postValue(null);
        }
    }
}
