package com.usc.centavo.view.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.centavo.R;
import com.usc.centavo.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> implements Filterable {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String DATE_FORMAT_PATTERN = "MMMM d yyyy, hh:mm aaa";
    private static final String AMOUNT_FORMAT = "$%.2f";

    private List<Transaction> transactionList;
    private final List<Transaction> transactionListFull;
    private final SimpleDateFormat dateFormat;
    private Map<String, String> categoryColorMap;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = new ArrayList<>(transactionList);
        this.transactionListFull = new ArrayList<>(transactionList);
        this.dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US);
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactionList = new ArrayList<>(transactions);
        this.transactionListFull.clear();
        this.transactionListFull.addAll(transactions);
        notifyItemRangeChanged(0, transactionList.size());
    }

    public void setCategoryColorMap(Map<String, String> categoryColorMap) {
        this.categoryColorMap = categoryColorMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction, dateFormat, categoryColorMap);

        holder.itemView.setOnClickListener(v -> navigateToEditTransaction(v, transaction));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    @Override
    public Filter getFilter() {
        return transactionFilter;
    }

    private void navigateToEditTransaction(View view, Transaction transaction) {
        Bundle bundle = new Bundle();
        bundle.putString(TRANSACTION_ID_KEY, transaction.getTransactionId());
        Navigation.findNavController(view)
                .navigate(R.id.action_global_to_edit_transaction, bundle);
    }

    private final Filter transactionFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Transaction> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(transactionListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Transaction transaction : transactionListFull) {
                    if (transaction.getDescription().toLowerCase().contains(filterPattern)) {
                        filteredList.add(transaction);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<Transaction> newList = results.values != null ?
                (List<Transaction>) results.values : new ArrayList<>();

            int oldSize = transactionList.size();
            transactionList.clear();
            transactionList.addAll(newList);

            if (oldSize > newList.size()) {
                notifyItemRangeRemoved(newList.size(), oldSize - newList.size());
            } else if (oldSize < newList.size()) {
                notifyItemRangeInserted(oldSize, newList.size() - oldSize);
            }
            notifyItemRangeChanged(0, Math.min(oldSize, newList.size()));
        }
    };

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDescription;
        private final TextView textAmount;
        private final TextView textDate;
        private final View viewCategoryColor;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.text_description);
            textAmount = itemView.findViewById(R.id.text_amount);
            textDate = itemView.findViewById(R.id.text_date);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);
        }

        public void bind(Transaction transaction, SimpleDateFormat dateFormat, Map<String, String> categoryColorMap) {
            textDescription.setText(transaction.getDescription());
            textAmount.setText(String.format(Locale.US, AMOUNT_FORMAT, transaction.getAmount()));
            textDate.setText(dateFormat.format(transaction.getTransactionDate()));
            // Set background color (keep circle)
            GradientDrawable bg = (GradientDrawable) viewCategoryColor.getBackground();
            String color = null;
            if (categoryColorMap != null && transaction.getCategoryId() != null) {
                color = categoryColorMap.get(transaction.getCategoryId());
            }
            if (color != null && color.matches("^#([A-Fa-f0-9]{6})$")) {
                bg.setColor(Color.parseColor(color));
            } else {
                bg.setColor(itemView.getContext().getColor(R.color.md_theme_primary));
            }
        }
    }
}