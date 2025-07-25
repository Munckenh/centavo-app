package com.usc.centavo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usc.centavo.model.Goal;
import com.usc.centavo.utils.OperationStatus;
import java.util.List;

public class GoalRepository {
    private static final String GOALS_COLLECTION = "goals";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Goal>> goalsLiveData = new MutableLiveData<>();
    private final MutableLiveData<OperationStatus> statusLiveData = new MutableLiveData<>();

    public LiveData<List<Goal>> getGoalsLiveData() {
        return goalsLiveData;
    }

    public LiveData<OperationStatus> getStatusLiveData() {
        return statusLiveData;
    }

    public void getGoalsForUser(String userId) {
        db.collection(GOALS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("deadline")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        statusLiveData.postValue(OperationStatus.FAILURE);
                        return;
                    }
                    if (snapshots != null) {
                        goalsLiveData.postValue(snapshots.toObjects(Goal.class));
                    }
                });
    }

    public void addGoal(Goal goal) {
        db.collection(GOALS_COLLECTION)
                .add(goal)
                .addOnSuccessListener(documentReference -> statusLiveData.postValue(OperationStatus.SUCCESS))
                .addOnFailureListener(e -> statusLiveData.postValue(OperationStatus.FAILURE));
    }

    public void updateGoal(Goal goal) {
        db.collection(GOALS_COLLECTION)
                .document(goal.getGoalId())
                .set(goal)
                .addOnSuccessListener(aVoid -> statusLiveData.postValue(OperationStatus.SUCCESS))
                .addOnFailureListener(e -> statusLiveData.postValue(OperationStatus.FAILURE));
    }

    public void deleteGoal(String goalId) {
        db.collection(GOALS_COLLECTION)
                .document(goalId)
                .delete()
                .addOnSuccessListener(aVoid -> statusLiveData.postValue(OperationStatus.SUCCESS))
                .addOnFailureListener(e -> statusLiveData.postValue(OperationStatus.FAILURE));
    }

    public void contributeToGoal(String goalId, double amount) {
        db.collection(GOALS_COLLECTION).document(goalId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Goal goal = documentSnapshot.toObject(Goal.class);
                    if (goal != null) {
                        double newAmount = goal.getCurrentAmount() + amount;
                        goal.setCurrentAmount(newAmount);
                        if (newAmount >= goal.getTargetAmount()) {
                            goal.setCompleted(true);
                        }
                        updateGoal(goal);
                    }
                } else {
                    statusLiveData.postValue(OperationStatus.FAILURE);
                }
            })
            .addOnFailureListener(e -> statusLiveData.postValue(OperationStatus.FAILURE));
    }
} 