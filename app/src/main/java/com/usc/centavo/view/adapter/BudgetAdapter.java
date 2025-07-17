package com.usc.centavo.view.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.centavo.R;
import com.usc.centavo.model.Budget;
import com.usc.centavo.model.Transaction;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Budget> budgets = new ArrayList<>();
    private Map<String, String> categoryColorMap = null;
    private List<Transaction> transactions = new ArrayList<>();
    private Map<String, String> categoryNameMap = null;

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        String categoryName = categoryNameMap != null && budget.getCategoryId() != null ? categoryNameMap.get(budget.getCategoryId()) : "";
        holder.textViewBudgetName.setText(categoryName);
        holder.textViewBudgetAmount.setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(budget.getAmount()));
        holder.textViewBudgetPeriod.setText(capitalize(budget.getPeriod()));
        // Set category color if available
        GradientDrawable bg = (GradientDrawable) holder.viewCategoryColor.getBackground();
        String color = null;
        if (categoryColorMap != null && budget.getCategoryId() != null) {
            color = categoryColorMap.get(budget.getCategoryId());
        }
        if (color != null && color.matches("^#([A-Fa-f0-9]{6})$")) {
            bg.setColor(android.graphics.Color.parseColor(color));
        } else {
            bg.setColor(holder.itemView.getContext().getColor(R.color.md_theme_primary)); // Default color
        }
        // Progress calculation
        double spent = calculateSpentForBudget(budget);
        int percent = 0;
        if (budget.getAmount() > 0) {
            percent = (int) Math.round(Math.min(100, (spent / budget.getAmount()) * 100));
        }
        holder.progressBudgetItem.setProgress(percent);
        holder.progressBudgetItem.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBudgetClick(budget);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onBudgetLongClick(budget);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCategoryColorMap(Map<String, String> colorMap) {
        this.categoryColorMap = colorMap;
        notifyDataSetChanged();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCategoryNameMap(Map<String, String> nameMap) {
        this.categoryNameMap = nameMap;
        notifyDataSetChanged();
    }

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }
    private OnBudgetClickListener clickListener;
    public void setOnBudgetClickListener(OnBudgetClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnBudgetLongClickListener {
        void onBudgetLongClick(Budget budget);
    }
    private OnBudgetLongClickListener longClickListener;
    public void setOnBudgetLongClickListener(OnBudgetLongClickListener listener) {
        this.longClickListener = listener;
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        View viewCategoryColor;
        TextView textViewBudgetName, textViewBudgetAmount, textViewBudgetPeriod;
        LinearProgressIndicator progressBudgetItem;
        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);
            textViewBudgetName = itemView.findViewById(R.id.textViewBudgetName);
            textViewBudgetAmount = itemView.findViewById(R.id.textViewBudgetAmount);
            textViewBudgetPeriod = itemView.findViewById(R.id.textViewBudgetPeriod);
            progressBudgetItem = itemView.findViewById(R.id.progressBudgetItem);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private double calculateSpentForBudget(Budget budget) {
        if (transactions == null) return 0;
        double sum = 0;
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        for (Transaction tx : transactions) {
            if (tx.getCategoryId() == null || !tx.getCategoryId().equals(budget.getCategoryId())) continue;
            if (!"Expense".equalsIgnoreCase(tx.getType())) continue;
            Date txDate = tx.getTransactionDate();
            if (txDate == null) continue;
            Calendar txCal = Calendar.getInstance();
            txCal.setTime(txDate);
            boolean inPeriod = false;
            switch (budget.getPeriod()) {
                case "daily":
                    inPeriod = cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR);
                    break;
                case "weekly":
                    inPeriod = cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) && cal.get(Calendar.WEEK_OF_YEAR) == txCal.get(Calendar.WEEK_OF_YEAR);
                    break;
                case "monthly":
                    inPeriod = cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == txCal.get(Calendar.MONTH);
                    break;
            }
            if (inPeriod) sum += tx.getAmount();
        }
        return sum;
    }
} 