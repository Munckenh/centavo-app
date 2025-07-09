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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTransactionFragment extends Fragment {

    private TransactionViewModel viewModel;
    private FragmentEditTransactionBinding binding;
    private Transaction currentTransaction;
    private final Calendar selectedDate = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupListeners();
        setupObservers();
    }

    private void populateUI() {
        binding.editTextAmount.setText(String.valueOf(currentTransaction.getAmount()));
        binding.editTextDescription.setText(currentTransaction.getDescription());
//        binding.editTextCategory.setText(currentTransaction.getCategoryId());
//        binding.editTextAccount.setText(currentTransaction.getAccountId());
        if (currentTransaction.getTransactionDate() != null) {
            selectedDate.setTime(currentTransaction.getTransactionDate());
            updateDateInView();
        }
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
            }
        });
    }

    private void saveTransaction() {
        String amountStr = binding.editTextAmount.getText().toString();
        String description = binding.editTextDescription.getText().toString();
        String categoryId = "TODO"; // Replace with actual category selection logic
        String accountId = "TODO";  // Replace with actual account selection logic

        if (amountStr.isEmpty() || description.isEmpty() || categoryId.isEmpty() || accountId.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        currentTransaction.setAmount(Double.parseDouble(amountStr));
        currentTransaction.setDescription(description);
        currentTransaction.setCategoryId(categoryId);
        currentTransaction.setAccountId(accountId);
        currentTransaction.setTransactionDate(selectedDate.getTime());
        currentTransaction.setUserId(FirebaseAuth.getInstance().getUid());

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