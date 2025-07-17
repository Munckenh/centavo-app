package com.usc.centavo.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.centavo.R;
import com.usc.centavo.view.adapter.CategoryAdapter;
import com.usc.centavo.databinding.FragmentCategoryManagementBinding;
import com.usc.centavo.model.Category;
import com.usc.centavo.viewmodel.CategoryViewModel;

public class CategoryManagementFragment extends Fragment implements CategoryAdapter.OnCategoryInteractionListener {

    private CategoryViewModel viewModel;
    private FragmentCategoryManagementBinding binding;
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        setupAdapters();
        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        binding.fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void setupAdapters() {
        RecyclerView recyclerView = binding.categoriesRecyclerView;
        adapter = new CategoryAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                adapter.setCategories(categories);
            }
        });

        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.onErrorHandled();
            }
        });
    }

    private void showCategoryDialog(@Nullable Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(category == null ? "Add Category" : "Edit Category");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        final EditText nameInput = dialogView.findViewById(R.id.categoryNameInput);
        final EditText colorInput = dialogView.findViewById(R.id.categoryColorInput);

        if (category != null) {
            nameInput.setText(category.getName());
            colorInput.setText(category.getColor());
        }
        builder.setView(dialogView);

        builder.setPositiveButton("Save", null); // We'll override this after showing
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String color = colorInput.getText().toString().trim();

            if (name.isEmpty() || color.isEmpty()) {
                Toast.makeText(getContext(), "Name and color cannot be empty", Toast.LENGTH_SHORT).show();
                return; // Don't dismiss
            }

            // Basic hex color validation
            if (!color.matches("^#([A-Fa-f0-9]{6})$")) {
                Toast.makeText(getContext(), "Invalid color format. Use #RRGGBB", Toast.LENGTH_SHORT).show();
                return; // Don't dismiss
            }

            if (category == null) {
                viewModel.addCategory(name, color);
            } else {
                category.setName(name);
                category.setColor(color);
                viewModel.updateCategory(category);
            }
            dialog.dismiss(); // Only dismiss if valid
        });
    }

    @Override
    public void onEditCategory(Category category) {
        showCategoryDialog(category);
    }

    @Override
    public void onDeleteCategory(String categoryId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category? All transactions in this category will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteCategory(categoryId))
                .setNegativeButton("Cancel", null)
                .show();
    }
}