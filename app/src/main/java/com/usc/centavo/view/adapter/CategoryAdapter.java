package com.usc.centavo.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.centavo.R;
import com.usc.centavo.model.Category;

import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.GradientDrawable;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private final OnCategoryInteractionListener listener;

    public interface OnCategoryInteractionListener {
        void onEditCategory(Category category);
        void onDeleteCategory(String categoryId);
    }

    public CategoryAdapter(@NonNull OnCategoryInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(@Nullable List<Category> categories) {
        List<Category> newCategories = categories != null ? categories : new ArrayList<>();
        int oldSize = this.categories.size();
        this.categories = new ArrayList<>(newCategories);

        if (oldSize > newCategories.size()) {
            notifyItemRangeRemoved(newCategories.size(), oldSize - newCategories.size());
        } else if (oldSize < newCategories.size()) {
            notifyItemRangeInserted(oldSize, newCategories.size() - oldSize);
        }
        notifyItemRangeChanged(0, Math.min(oldSize, newCategories.size()));
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryNameTextView;
        private final View viewCategoryColor;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);
        }
        public void bind(@NonNull Category category, @NonNull OnCategoryInteractionListener listener) {
            categoryNameTextView.setText(category.getName());
            // Set color
            String color = category.getColor();
            GradientDrawable bg = (GradientDrawable) viewCategoryColor.getBackground();
            if (color != null && color.matches("^#([A-Fa-f0-9]{6})$")) {
                bg.setColor(android.graphics.Color.parseColor(color));
            } else {
                bg.setColor(itemView.getContext().getColor(R.color.md_theme_primary));
            }
            itemView.setOnClickListener(v -> listener.onEditCategory(category));
            itemView.setOnLongClickListener(v -> {
                listener.onDeleteCategory(category.getCategoryId());
                return true;
            });
        }
    }
}