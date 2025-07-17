package com.usc.centavo.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.databinding.FragmentEditTransactionBinding;
import com.usc.centavo.model.Transaction;
import com.usc.centavo.viewmodel.TransactionViewModel;
import com.usc.centavo.viewmodel.CategoryViewModel;
import com.usc.centavo.model.Category;
import com.usc.centavo.viewmodel.AccountViewModel;
import com.usc.centavo.model.Account;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class EditTransactionFragment extends Fragment {

    private TransactionViewModel viewModel;
    private FragmentEditTransactionBinding binding;
    private Transaction currentTransaction;
    private final Calendar selectedDate = Calendar.getInstance();
    private CategoryViewModel categoryViewModel;
    private String selectedCategoryId;
    private String selectedAccount;
    private String selectedType;
    private AccountViewModel accountViewModel;
    private List<Account> accountObjects = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        showLoading(true);
        setupDropdowns();
        setupListeners();
        setupObservers();
    }

    private void showLoading(boolean loading) {
        if (binding.progressBar != null && binding.editTransactionForm != null) {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.editTransactionForm.setVisibility(loading ? View.GONE : View.VISIBLE);
        }
    }

    private void populateUI() {
        // Disable hint animation for all input layouts
        binding.amountInputLayout.setHintAnimationEnabled(false);
        binding.descriptionInputLayout.setHintAnimationEnabled(false);
        binding.dateInputLayout.setHintAnimationEnabled(false);
        binding.categoryInputLayout.setHintAnimationEnabled(false);
        binding.accountInputLayout.setHintAnimationEnabled(false);
        binding.typeInputLayout.setHintAnimationEnabled(false);

        binding.editTextAmount.setText(String.valueOf(currentTransaction.getAmount()));
        binding.editTextDescription.setText(currentTransaction.getDescription());
        binding.editTextDate.setText(new java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.US).format(currentTransaction.getTransactionDate()));
        // Pre-select dropdowns
        setupDropdowns();
        if (currentTransaction.getTransactionDate() != null) {
            selectedDate.setTime(currentTransaction.getTransactionDate());
            updateDateInView();
        }

        // Re-enable hint animation for all input layouts
        binding.amountInputLayout.setHintAnimationEnabled(true);
        binding.descriptionInputLayout.setHintAnimationEnabled(true);
        binding.dateInputLayout.setHintAnimationEnabled(true);
        binding.categoryInputLayout.setHintAnimationEnabled(true);
        binding.accountInputLayout.setHintAnimationEnabled(true);
        binding.typeInputLayout.setHintAnimationEnabled(true);
    }

    private void setupDropdowns() {
        // Category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        binding.autoCompleteCategory.setAdapter(categoryAdapter);
        binding.autoCompleteCategory.setThreshold(0);

        categoryViewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            List<String> categoryNames = new ArrayList<>();
            for (Category c : categories) {
                categoryNames.add(c.getName());
            }
            categoryAdapter.clear();
            categoryAdapter.addAll(categoryNames);
            categoryAdapter.notifyDataSetChanged();

            // Pre-select if editing
            if (currentTransaction != null && currentTransaction.getCategoryId() != null) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getCategoryId().equals(currentTransaction.getCategoryId())) {
                        binding.autoCompleteCategory.setText(categories.get(i).getName(), false);
                        selectedCategoryId = categories.get(i).getCategoryId();
                        break;
                    }
                }
            }
        });

        binding.autoCompleteCategory.setOnItemClickListener((parent, view, position, id) -> {
            List<Category> categories = categoryViewModel.getCategoriesLiveData().getValue();
            if (categories != null && position < categories.size()) {
                selectedCategoryId = categories.get(position).getCategoryId();
            }
        });

        // Account dropdown
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        binding.autoCompleteAccount.setAdapter(accountAdapter);
        binding.autoCompleteAccount.setThreshold(0);
        accountViewModel.getAccountsLiveData().observe(getViewLifecycleOwner(), accounts -> {
            accountObjects = accounts != null ? accounts : new ArrayList<>();
            List<String> accountNames = new ArrayList<>();
            accountNames.add("None");
            for (Account acc : accountObjects) {
                accountNames.add(acc.getName());
            }
            accountAdapter.clear();
            accountAdapter.addAll(accountNames);
            accountAdapter.notifyDataSetChanged();
            // Pre-select if editing
            if (currentTransaction != null) {
                String accId = currentTransaction.getAccountId();
                if (accId == null || accId.isEmpty()) {
                    binding.autoCompleteAccount.setText(accountNames.get(0), false);
                    selectedAccount = null;
                } else {
                    int idx = 0;
                    for (int i = 0; i < accountObjects.size(); i++) {
                        if (accountObjects.get(i).getAccountId().equals(accId)) {
                            idx = i + 1; // +1 for 'None'
                            break;
                        }
                    }
                    binding.autoCompleteAccount.setText(accountNames.get(idx), false);
                    selectedAccount = accId;
                }
            }
        });
        binding.autoCompleteAccount.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedAccount = null;
            } else {
                selectedAccount = accountObjects.get(position - 1).getAccountId();
            }
        });

        // Transaction type dropdown
        List<String> typeList = new ArrayList<>();
        typeList.add("Income");
        typeList.add("Expense");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, typeList);
        binding.autoCompleteType.setAdapter(typeAdapter);
        binding.autoCompleteType.setThreshold(0);

        // Pre-select if editing
        if (currentTransaction != null && currentTransaction.getType() != null) {
            int idx = typeList.indexOf(currentTransaction.getType());
            if (idx >= 0) {
                binding.autoCompleteType.setText(typeList.get(idx), false);
                selectedType = typeList.get(idx);
            }
        }

        binding.autoCompleteType.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = typeList.get(position);
        });

        // Show dropdowns on focus
        binding.autoCompleteCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.autoCompleteCategory.showDropDown();
        });
        binding.autoCompleteAccount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.autoCompleteAccount.showDropDown();
        });
        binding.autoCompleteType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.autoCompleteType.showDropDown();
        });
    }

    private void setupListeners() {
        binding.editTextDate.setOnClickListener(v -> showDatePickerDialog());
        binding.buttonSave.setOnClickListener(v -> saveTransaction());
        binding.buttonDelete.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) ->
                    viewModel.deleteTransaction(currentTransaction.getTransactionId())
                )
                .setNegativeButton("Cancel", null)
                .show());
    }

    private void setupObservers() {
        viewModel.getOperationStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            switch (status) {
                case SUCCESS:
                    NavHostFragment.findNavController(this).popBackStack();
                    viewModel.onSuccessHandled();
                    break;
                case FAILURE:
                case LOADING:
                case IDLE:
                    break;
            }
        });

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.onErrorHandled();
            }
        });

        // Handle null arguments
        if (getArguments() == null) {
            Toast.makeText(getContext(), "No transaction data provided", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        String transactionId = getArguments().getString("transactionId");
        if (transactionId == null || transactionId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid transaction ID", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        viewModel.getTransactionById(transactionId).observe(getViewLifecycleOwner(), transaction -> {
            if (transaction != null) {
                currentTransaction = transaction;
                populateUI();
                showLoading(false); // Hide loading, show form
            }
        });
    }

    private void saveTransaction() {
        String amountStr = binding.editTextAmount.getText().toString();
        String description = binding.editTextDescription.getText().toString();
        String categoryId = selectedCategoryId;
        String accountId = selectedAccount;
        String type = selectedType;

        if (amountStr.isEmpty() || description.isEmpty() || categoryId.isEmpty() || type == null || type.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        currentTransaction.setAmount(Double.parseDouble(amountStr));
        currentTransaction.setDescription(description);
        currentTransaction.setCategoryId(categoryId);
        currentTransaction.setAccountId(accountId);
        currentTransaction.setTransactionDate(selectedDate.getTime());
        currentTransaction.setUserId(FirebaseAuth.getInstance().getUid());
        currentTransaction.setType(type);
        viewModel.updateTransaction(currentTransaction);
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateInView() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        binding.editTextDate.setText(sdf.format(selectedDate.getTime()));
    }
}