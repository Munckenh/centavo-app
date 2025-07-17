package com.usc.centavo.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.databinding.FragmentAddTransactionBinding;
import com.usc.centavo.model.Transaction;
import com.usc.centavo.viewmodel.TransactionViewModel;
import com.usc.centavo.viewmodel.CategoryViewModel;
import com.usc.centavo.model.Category;
import com.usc.centavo.viewmodel.AccountViewModel;
import com.usc.centavo.model.Account;
import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {

    private TransactionViewModel viewModel;
    private FragmentAddTransactionBinding binding;
    private final Calendar selectedDate = Calendar.getInstance();
    private CategoryViewModel categoryViewModel;
    private String selectedCategoryId;
    private String selectedAccount;
    private String selectedType;
    private AccountViewModel accountViewModel;
    private List<Account> accountObjects = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        setupDatePicker();
        setupDropdowns();
        setupListeners();
        setupObservers();
    }

    private void setupDatePicker() {
        // Disable hint animation before setting initial date
        binding.dateInputLayout.setHintAnimationEnabled(false);
        updateDateInView();
        binding.dateInputLayout.setHintAnimationEnabled(true);
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };

        binding.editTextDate.setOnClickListener(v -> {
            if (getContext() != null) {
                new DatePickerDialog(getContext(), dateSetListener,
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateDateInView() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        binding.editTextDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupDropdowns() {
        // Category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        binding.autoCompleteCategory.setAdapter(categoryAdapter);
        binding.autoCompleteCategory.setThreshold(0); // Show dropdown immediately

        categoryViewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            List<String> categoryNames = new ArrayList<>();
            for (Category c : categories) {
                categoryNames.add(c.getName());
            }
            categoryAdapter.clear();
            categoryAdapter.addAll(categoryNames);
            categoryAdapter.notifyDataSetChanged();
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
        binding.buttonSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String amountStr = binding.editTextAmount.getText().toString();
        String description = binding.editTextDescription.getText().toString();
        String categoryId = selectedCategoryId;
        String accountId = selectedAccount;
        String type = selectedType;
        String userId = FirebaseAuth.getInstance().getUid();

        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(categoryId) || TextUtils.isEmpty(type) || userId == null) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Date transactionDate = selectedDate.getTime();
            Transaction transaction = new Transaction(userId, categoryId, accountId, amount, description, transactionDate, type);
            viewModel.addTransaction(transaction);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        viewModel.getOperationStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            switch (status) {
                case SUCCESS:
                    NavHostFragment.findNavController(this).navigateUp();
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
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.onErrorHandled();
            }
        });
    }
}