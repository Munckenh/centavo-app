package com.usc.centavo.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.usc.centavo.R;
import com.usc.centavo.model.Budget;
import com.usc.centavo.view.adapter.BudgetAdapter;
import com.usc.centavo.viewmodel.BudgetViewModel;
import com.usc.centavo.viewmodel.CategoryViewModel;
import com.usc.centavo.model.Category;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import com.usc.centavo.viewmodel.TransactionViewModel;
import com.usc.centavo.model.Transaction;
import java.util.Map;
import java.util.HashMap;

public class BudgetFragment extends Fragment {
    private BudgetViewModel budgetViewModel;
    private RecyclerView recyclerViewBudgets;
    private TextView textViewEmptyBudgets;
    private FloatingActionButton fabAddBudget;
    private BudgetAdapter budgetAdapter;
    private CategoryViewModel categoryViewModel;
    private List<Category> categoryList = new ArrayList<>();
    private TransactionViewModel transactionViewModel;
    private List<Budget> budgetList = new ArrayList<>();
    private List<Transaction> transactionList = new ArrayList<>();
    private Map<String, String> categoryNameMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        recyclerViewBudgets = view.findViewById(R.id.recyclerViewBudgets);
        textViewEmptyBudgets = view.findViewById(R.id.textViewEmptyBudgets);
        fabAddBudget = view.findViewById(R.id.fabAddBudget);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        budgetAdapter = new BudgetAdapter();
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBudgets.setAdapter(budgetAdapter);

        // Long press for edit/delete
        budgetAdapter.setOnBudgetClickListener(budget -> showBudgetOptionsDialog(budget));
        budgetAdapter.setOnBudgetLongClickListener(budget -> confirmDeleteBudget(budget));

        // Use real userId from FirebaseAuth
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            budgetViewModel.getBudgetsForUser(userId);
            categoryViewModel.loadCategories();
            transactionViewModel.getTransactionsForUser(userId);
        }

        categoryViewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            categoryList = categories != null ? categories : new ArrayList<>();
            // Pass color map to adapter
            java.util.Map<String, String> colorMap = new java.util.HashMap<>();
            categoryNameMap.clear();
            for (Category cat : categoryList) {
                colorMap.put(cat.getCategoryId(), cat.getColor());
                categoryNameMap.put(cat.getCategoryId(), cat.getName());
            }
            budgetAdapter.setCategoryColorMap(colorMap);
            budgetAdapter.setCategoryNameMap(categoryNameMap);
        });

        budgetViewModel.getBudgetsLiveData().observe(getViewLifecycleOwner(), budgets -> {
            budgetList = budgets != null ? budgets : new ArrayList<>();
            updateBudgetAdapter();
            updateBudgetList(budgetList);
        });

        transactionViewModel.getTransactionsLiveData().observe(getViewLifecycleOwner(), txs -> {
            transactionList = txs != null ? txs : new ArrayList<>();
            updateBudgetAdapter();
        });

        fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void updateBudgetList(List<Budget> budgets) {
        if (budgets == null || budgets.isEmpty()) {
            recyclerViewBudgets.setVisibility(View.GONE);
            textViewEmptyBudgets.setVisibility(View.VISIBLE);
        } else {
            recyclerViewBudgets.setVisibility(View.VISIBLE);
            textViewEmptyBudgets.setVisibility(View.GONE);
            budgetAdapter.setBudgets(budgets);
        }
    }

    private void updateBudgetAdapter() {
        budgetAdapter.setBudgets(budgetList);
        budgetAdapter.setTransactions(transactionList);
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Budget");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_budget, null);
        final EditText amountInput = dialogView.findViewById(R.id.editTextBudgetAmount);
        final MaterialAutoCompleteTextView periodDropdown = dialogView.findViewById(R.id.dropdownBudgetPeriod);
        final MaterialAutoCompleteTextView categoryDropdown = dialogView.findViewById(R.id.dropdownBudgetCategory);

        // Period dropdown
        List<String> periodList = new ArrayList<>();
        periodList.add("Daily");
        periodList.add("Weekly");
        periodList.add("Monthly");
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, periodList);
        periodDropdown.setAdapter(periodAdapter);
        periodDropdown.setThreshold(0);
        periodDropdown.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) periodDropdown.showDropDown(); });

        // Category dropdown
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) categoryNames.add(c.getName());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
        categoryDropdown.setAdapter(categoryAdapter);
        categoryDropdown.setThreshold(0);
        categoryDropdown.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) categoryDropdown.showDropDown(); });

        builder.setView(dialogView);
        builder.setPositiveButton("Save", null); // We'll override this after showing
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            String period = periodDropdown.getText().toString().trim().toLowerCase();
            int catIdx = categoryDropdown.getListSelection();
            String categoryId = null;
            if (catIdx >= 0 && catIdx < categoryList.size()) {
                categoryId = categoryList.get(catIdx).getCategoryId();
            } else if (!categoryList.isEmpty()) {
                // fallback: match by name
                String selectedName = categoryDropdown.getText().toString().trim();
                for (Category c : categoryList) {
                    if (c.getName().equals(selectedName)) {
                        categoryId = c.getCategoryId();
                        break;
                    }
                }
            }
            if (amountStr.isEmpty() || period.isEmpty() || categoryId == null) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            double amount = 0;
            try { amount = Double.parseDouble(amountStr); } catch (NumberFormatException ignored) {}
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            budgetViewModel.addBudget(new com.usc.centavo.model.Budget(userId, categoryId, amount, period));
            dialog.dismiss();
        });
    }

    private void showBudgetOptionsDialog(Budget budget) {
        String categoryName = categoryNameMap.get(budget.getCategoryId());
        new AlertDialog.Builder(requireContext())
            .setTitle(categoryName != null ? categoryName : "Budget")
            .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                if (which == 0) showEditBudgetDialog(budget);
                else if (which == 1) confirmDeleteBudget(budget);
            })
            .show();
    }

    private void showEditBudgetDialog(Budget budget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        String categoryName = categoryNameMap.get(budget.getCategoryId());
        builder.setTitle(categoryName != null ? ("Edit " + categoryName + " Budget") : "Edit Budget");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_budget, null);
        final EditText amountInput = dialogView.findViewById(R.id.editTextBudgetAmount);
        final MaterialAutoCompleteTextView periodDropdown = dialogView.findViewById(R.id.dropdownBudgetPeriod);
        final MaterialAutoCompleteTextView categoryDropdown = dialogView.findViewById(R.id.dropdownBudgetCategory);

        amountInput.setText(String.valueOf(budget.getAmount()));
        // Period dropdown
        List<String> periodList = new ArrayList<>();
        periodList.add("Daily");
        periodList.add("Weekly");
        periodList.add("Monthly");
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, periodList);
        periodDropdown.setAdapter(periodAdapter);
        periodDropdown.setThreshold(0);
        periodDropdown.setText(capitalize(budget.getPeriod()), false);
        periodDropdown.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) periodDropdown.showDropDown(); });
        // Category dropdown
        List<String> categoryNames = new ArrayList<>();
        int selectedIdx = 0;
        for (int i = 0; i < categoryList.size(); i++) {
            categoryNames.add(categoryList.get(i).getName());
            if (categoryList.get(i).getCategoryId().equals(budget.getCategoryId())) selectedIdx = i;
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
        categoryDropdown.setAdapter(categoryAdapter);
        categoryDropdown.setThreshold(0);
        if (!categoryNames.isEmpty()) categoryDropdown.setText(categoryNames.get(selectedIdx), false);
        categoryDropdown.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) categoryDropdown.showDropDown(); });
        builder.setView(dialogView);
        builder.setPositiveButton("Save", null); // We'll override this after showing
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            String period = periodDropdown.getText().toString().trim().toLowerCase();
            int catIdx = categoryDropdown.getListSelection();
            String categoryId = null;
            if (catIdx >= 0 && catIdx < categoryList.size()) {
                categoryId = categoryList.get(catIdx).getCategoryId();
            } else if (!categoryList.isEmpty()) {
                String selectedName = categoryDropdown.getText().toString().trim();
                for (Category c : categoryList) {
                    if (c.getName().equals(selectedName)) {
                        categoryId = c.getCategoryId();
                        break;
                    }
                }
            }
            if (amountStr.isEmpty() || period.isEmpty() || categoryId == null) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            double amount = 0;
            try { amount = Double.parseDouble(amountStr); } catch (NumberFormatException ignored) {}
            budget.setAmount(amount);
            budget.setPeriod(period);
            budget.setCategoryId(categoryId);
            budgetViewModel.updateBudget(budget);
            dialog.dismiss();
        });
    }

    private void confirmDeleteBudget(Budget budget) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete this budget?")
            .setPositiveButton("Delete", (dialog, which) -> budgetViewModel.deleteBudget(budget.getBudgetId()))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
} 