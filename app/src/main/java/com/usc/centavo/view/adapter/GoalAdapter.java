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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class GoalAdapter extends ListAdapter<Goal, GoalAdapter.GoalViewHolder> {

    private OnGoalClickListener clickListener;
    private OnGoalLongClickListener longClickListener;

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
    }

    public interface OnGoalLongClickListener {
        void onGoalLongClick(Goal goal);
    }

    public GoalAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnGoalClickListener(OnGoalClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnGoalLongClickListener(OnGoalLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = getItem(position);
        holder.bind(goal, clickListener, longClickListener);
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewGoalName;
        private final TextView textViewGoalDeadline;
        private final LinearProgressIndicator progressGoal;
        private final TextView textViewCurrentAmount;
        private final TextView textViewTargetAmount;
        private final SimpleDateFormat deadlineFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoalName = itemView.findViewById(R.id.textViewGoalName);
            textViewGoalDeadline = itemView.findViewById(R.id.textViewGoalDeadline);
            progressGoal = itemView.findViewById(R.id.progressGoal);
            textViewCurrentAmount = itemView.findViewById(R.id.textViewCurrentAmount);
            textViewTargetAmount = itemView.findViewById(R.id.textViewTargetAmount);
        }

        void bind(Goal goal, OnGoalClickListener clickListener, OnGoalLongClickListener longClickListener) {
            textViewGoalName.setText(goal.getGoalName());
            if (goal.getDeadline() != null) {
                textViewGoalDeadline.setText("by " + deadlineFormat.format(goal.getDeadline()));
            }
            textViewCurrentAmount.setText(currencyFormat.format(goal.getCurrentAmount()));
            textViewTargetAmount.setText(currencyFormat.format(goal.getTargetAmount()));

            int progress = 0;
            if (goal.getTargetAmount() > 0) {
                progress = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
            }
            progressGoal.setProgress(progress);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onGoalClick(goal);
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onGoalLongClick(goal);
                    return true;
                }
                return false;
            });
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