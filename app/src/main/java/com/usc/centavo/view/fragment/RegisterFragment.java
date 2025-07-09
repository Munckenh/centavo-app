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
import com.usc.centavo.databinding.FragmentRegisterBinding;
import com.usc.centavo.viewmodel.RegisterViewModel;

public class RegisterFragment extends Fragment {

    private RegisterViewModel viewModel;
    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> register());
        binding.tvLoginLink.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String firstName = binding.etFirstName.getText().toString().trim();
                String lastName = binding.etLastName.getText().toString().trim();
                String email = binding.etEmail.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();

                boolean isValid = !firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !password.isEmpty();
                binding.btnRegister.setEnabled(isValid);
            }
        };

        binding.etFirstName.addTextChangedListener(textWatcher);
        binding.etLastName.addTextChangedListener(textWatcher);
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

    private void register() {
        binding.pbLoading.setVisibility(View.VISIBLE);

        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        viewModel.register(email, password, firstName, lastName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}