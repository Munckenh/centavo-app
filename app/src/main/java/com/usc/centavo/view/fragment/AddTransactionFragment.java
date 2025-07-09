package com.usc.centavo.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.databinding.FragmentAddTransactionBinding;
import com.usc.centavo.model.Transaction;
import com.usc.centavo.viewmodel.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {

    private TransactionViewModel viewModel;
    private FragmentAddTransactionBinding binding;
    private final Calendar selectedDate = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupDatePicker();
        setupListeners();
        setupObservers();
    }

    private void setupDatePicker() {
        updateDateInView();
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

    private void setupListeners() {
        binding.buttonSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String amountStr = binding.editTextAmount.getText().toString();
        String description = binding.editTextDescription.getText().toString();
        String categoryId = "TODO"; // Replace with actual category selection logic
        String accountId = "TODO";  // Replace with actual account selection logic
        String userId = FirebaseAuth.getInstance().getUid();

        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(categoryId) || TextUtils.isEmpty(accountId) || userId == null) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Date transactionDate = selectedDate.getTime();
            Transaction transaction = new Transaction(userId, categoryId, accountId, amount, description, transactionDate);
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