package com.usc.centavo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.usc.centavo.model.Goal;
import com.usc.centavo.repository.GoalRepository;
import com.usc.centavo.utils.OperationStatus;
import java.util.List;

public class GoalViewModel extends AndroidViewModel {
    private final GoalRepository repository;
    private final LiveData<List<Goal>> goalsLiveData;
    private final LiveData<OperationStatus> statusLiveData;

    public GoalViewModel(@NonNull Application application) {
        super(application);
        repository = new GoalRepository();
        goalsLiveData = repository.getGoalsLiveData();
        statusLiveData = repository.getStatusLiveData();
    }

    public LiveData<List<Goal>> getGoalsLiveData() {
        return goalsLiveData;
    }

    public LiveData<OperationStatus> getStatusLiveData() {
        return statusLiveData;
    }

    public void getGoalsForUser(String userId) {
        repository.getGoalsForUser(userId);
    }

    public void addGoal(Goal goal) {
        repository.addGoal(goal);
    }

    public void updateGoal(Goal goal) {
        repository.updateGoal(goal);
    }

    public void deleteGoal(String goalId) {
        repository.deleteGoal(goalId);
    }

    public void contributeToGoal(String goalId, double amount) {
        repository.contributeToGoal(goalId, amount);
    }
} 