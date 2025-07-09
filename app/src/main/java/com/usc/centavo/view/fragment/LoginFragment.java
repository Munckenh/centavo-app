package com.usc.centavo.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.usc.centavo.R;
import com.usc.centavo.databinding.FragmentLoginBinding;
import com.usc.centavo.viewmodel.LoginViewModel;

public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> login());
        binding.tvRegisterLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_register)
        );

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = binding.etEmail.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();

                boolean isValid = !email.isEmpty() && !password.isEmpty();
                binding.btnLogin.setEnabled(isValid);
            }
        };

        binding.etEmail.addTextChangedListener(textWatcher);
        binding.etPassword.addTextChangedListener(textWatcher);
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            binding.pbLoading.setVisibility(View.GONE);
            if (user != null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_global_to_home);
            }
        });

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            binding.pbLoading.setVisibility(View.GONE);
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.onErrorHandled();
            }
        });
    }

    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        binding.pbLoading.setVisibility(View.VISIBLE);
        viewModel.login(email, password);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}