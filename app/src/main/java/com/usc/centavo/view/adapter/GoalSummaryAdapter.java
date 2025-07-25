package com.usc.centavo.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.usc.centavo.R;
import com.usc.centavo.model.Goal;
import java.util.Objects;

public class GoalSummaryAdapter extends ListAdapter<Goal, GoalSummaryAdapter.GoalSummaryViewHolder> {

    public GoalSummaryAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public GoalSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_summary, parent, false);
        return new GoalSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalSummaryViewHolder holder, int position) {
        Goal goal = getItem(position);
        holder.bind(goal);
    }

    static class GoalSummaryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewGoalName;
        private final TextView textViewGoalProgress;
        private final LinearProgressIndicator progressGoal;

        GoalSummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoalName = itemView.findViewById(R.id.textViewGoalName);
            textViewGoalProgress = itemView.findViewById(R.id.textViewGoalProgress);
            progressGoal = itemView.findViewById(R.id.progressGoal);
        }

        void bind(Goal goal) {
            textViewGoalName.setText(goal.getGoalName());

            int progress = 0;
            if (goal.getTargetAmount() > 0) {
                progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
            }
            textViewGoalProgress.setText(String.format("%d%%", progress));
            progressGoal.setProgress(progress);
        }
    }

    private static final DiffUtil.ItemCallback<Goal> DIFF_CALLBACK = new DiffUtil.ItemCallback<Goal>() {
        @Override
        public boolean areItemsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {
            return Objects.equals(oldItem.getGoalId(), newItem.getGoalId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {
            return oldItem.equals(newItem);
        }
    };
} 