package com.usc.centavo.view.fragment;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.databinding.FragmentTransactionsBinding;
import com.usc.centavo.view.adapter.TransactionAdapter;
import com.usc.centavo.viewmodel.TransactionViewModel;

import java.util.ArrayList;

public class TransactionsFragment extends Fragment {

    private TransactionAdapter adapter;
    private TransactionViewModel viewModel;
    private FragmentTransactionsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupSearchView();
        loadTransactions();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new ArrayList<>());
        binding.recyclerViewAll.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewAll.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getTransactionsLiveData().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setupWithSearchBar(binding.searchBar);
        binding.searchView.getEditText().addTextChangedListener(createSearchTextWatcher());
    }

    private TextWatcher createSearchTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
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