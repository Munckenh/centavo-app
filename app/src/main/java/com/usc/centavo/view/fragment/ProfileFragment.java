package com.usc.centavo.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.usc.centavo.R;
import com.usc.centavo.databinding.FragmentProfileBinding;
import com.usc.centavo.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        binding.tvLogout.setOnClickListener(v -> viewModel.logout());
        binding.tvManageCategories.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_category_management)
        );
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                // User is logged out, navigate to login screen.
                NavHostFragment.findNavController(ProfileFragment.this)
                        .navigate(R.id.action_global_to_login);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}