package com.usc.centavo.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.R;
import com.usc.centavo.model.Account;
import com.usc.centavo.model.Goal;
import com.usc.centavo.view.adapter.GoalAdapter;
import com.usc.centavo.viewmodel.AccountViewModel;
import com.usc.centavo.viewmodel.GoalViewModel;
import com.usc.centavo.viewmodel.TransactionViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.usc.centavo.model.Transaction;

public class GoalFragment extends Fragment {

    private GoalViewModel goalViewModel;
    private AccountViewModel accountViewModel;
    private TransactionViewModel transactionViewModel;
    private GoalAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyView;
    private FloatingActionButton fab;
    private List<Account> accountList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewGoals);
        emptyView = view.findViewById(R.id.emptyView);
        fab = view.findViewById(R.id.fabAddGoal);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        
        setupRecyclerView();
        setupObservers();
        
        fab.setOnClickListener(v -> showAddEditGoalDialog(null));
        
        loadInitialData();
    }
    
    private void setupRecyclerView() {
        adapter = new GoalAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnGoalClickListener(this::showContributeGoalDialog);
        adapter.setOnGoalLongClickListener(this::showGoalOptionsDialog);
    }
    
    private void setupObservers() {
        goalViewModel.getGoalsLiveData().observe(getViewLifecycleOwner(), goals -> {
            if (goals == null || goals.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                adapter.submitList(goals);
            }
        });
        
        accountViewModel.getAccountsLiveData().observe(getViewLifecycleOwner(), accounts -> {
            accountList = accounts != null ? accounts : new ArrayList<>();
        });
    }
    
    private void loadInitialData() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            goalViewModel.getGoalsForUser(userId);
            accountViewModel.loadAccounts();
        }
    }

    private void showAddEditGoalDialog(@Nullable Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);
        
        final TextInputEditText nameInput = dialogView.findViewById(R.id.editTextGoalName);
        final TextInputEditText targetAmountInput = dialogView.findViewById(R.id.editTextTargetAmount);
        final TextInputEditText deadlineInput = dialogView.findViewById(R.id.editTextDeadline);
        final Calendar calendar = Calendar.getInstance();
        
        if (goal != null) {
            builder.setTitle(R.string.dialog_title_edit_goal);
            nameInput.setText(goal.getGoalName());
            targetAmountInput.setText(String.valueOf(goal.getTargetAmount()));
            if (goal.getDeadline() != null) {
                calendar.setTime(goal.getDeadline());
                updateDeadlineInView(deadlineInput, calendar);
            }
        } else {
            builder.setTitle(R.string.dialog_title_add_goal);
        }
        
        deadlineInput.setOnClickListener(v -> showDatePicker(deadlineInput, calendar));

        int positiveButtonTextRes = (goal != null) ? R.string.action_save_changes : R.string.action_save_transaction;
        builder.setView(dialogView)
                .setPositiveButton(positiveButtonTextRes, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String amountStr = targetAmountInput.getText().toString().trim();
                    
                    if (name.isEmpty() || amountStr.isEmpty() || deadlineInput.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    double amount = Double.parseDouble(amountStr);
                    Date deadline = calendar.getTime();
                    String userId = FirebaseAuth.getInstance().getUid();
                    
                    if (userId != null) {
                        if (goal != null) {
                            goal.setGoalName(name);
                            goal.setTargetAmount(amount);
                            goal.setDeadline(deadline);
                            goalViewModel.updateGoal(goal);
                        } else {
                            goalViewModel.addGoal(new Goal(userId, name, amount, deadline));
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
    
    private void showContributeGoalDialog(Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_contribute_goal, null);

        final TextInputEditText amountInput = dialogView.findViewById(R.id.editTextContributionAmount);
        final MaterialAutoCompleteTextView accountDropdown = dialogView.findViewById(R.id.autoCompleteSourceAccount);

        List<String> accountNames = accountList.stream().map(Account::getName).collect(Collectors.toList());
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, accountNames);
        accountDropdown.setAdapter(accountAdapter);

        builder.setTitle(R.string.dialog_title_contribute_goal)
                .setView(dialogView)
                .setPositiveButton(R.string.action_contribute, (dialog, which) -> {
                    String amountStr = amountInput.getText().toString().trim();
                    String accountName = accountDropdown.getText().toString().trim();

                    if (amountStr.isEmpty() || accountName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    Account selectedAccount = accountList.stream()
                            .filter(acc -> acc.getName().equals(accountName))
                            .findFirst()
                            .orElse(null);

                    if (selectedAccount != null) {
                        if (selectedAccount.getBalance() >= amount) {
                            goalViewModel.contributeToGoal(goal.getGoalId(), amount);
                            
                            // Create a corresponding transaction
                            String userId = FirebaseAuth.getInstance().getUid();
                            if (userId != null) {
                                Transaction transaction = new Transaction(
                                        userId,
                                        null, // No category for goal contributions
                                        selectedAccount.getAccountId(),
                                        amount,
                                        "Contribution to " + goal.getGoalName(),
                                        new Date(),
                                        "Expense"
                                );
                                transactionViewModel.addTransaction(transaction);

                                // Update account balance
                                selectedAccount.setBalance(selectedAccount.getBalance() - amount);
                                accountViewModel.updateAccount(selectedAccount);
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.error_insufficient_funds, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
    
    private void showGoalOptionsDialog(Goal goal) {
        final CharSequence[] options = { "Contribute", "Edit", "Delete" };
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(goal.getGoalName());
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Contribute")) {
                showContributeGoalDialog(goal);
            } else if (options[item].equals("Edit")) {
                showAddEditGoalDialog(goal);
            } else if (options[item].equals("Delete")) {
                confirmDeleteGoal(goal);
            }
        });
        builder.show();
    }
    
    private void confirmDeleteGoal(Goal goal) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_title_delete_goal)
                .setMessage(R.string.dialog_message_delete_goal)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> goalViewModel.deleteGoal(goal.getGoalId()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showDatePicker(TextInputEditText dateInput, Calendar calendar) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDeadlineInView(dateInput, calendar);
        };
        new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }
    
    private void updateDeadlineInView(TextInputEditText dateInput, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        dateInput.setText(sdf.format(calendar.getTime()));
    }
} 