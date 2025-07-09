package com.usc.centavo.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.R;
import com.usc.centavo.databinding.FragmentHomeBinding;
import com.usc.centavo.model.Transaction;
import com.usc.centavo.view.adapter.TransactionAdapter;
import com.usc.centavo.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final int MAX_RECENT_TRANSACTIONS = 3;

    private TransactionViewModel viewModel;
    private FragmentHomeBinding binding;
    private TransactionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupRecyclerView();
        setupListeners();
        setupObservers();
        loadTransactions();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new ArrayList<>());
        binding.recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRecent.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabAddTransaction.setOnClickListener(v ->
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_home_to_add_transaction)
        );
    }

    private void setupObservers() {
        viewModel.getTransactionsLiveData().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null) return;

            // Assuming you want to show a subset of recent transactions
            List<Transaction> limitedTransactions = transactions.size() > MAX_RECENT_TRANSACTIONS
                    ? transactions.subList(0, MAX_RECENT_TRANSACTIONS)
                    : transactions;
            adapter.setTransactions(limitedTransactions);
        });

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.onErrorHandled();
            }
        });
    }

    private void loadTransactions() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            viewModel.getTransactionsForUser(userId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}