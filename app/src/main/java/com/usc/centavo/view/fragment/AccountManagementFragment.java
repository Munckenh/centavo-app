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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.usc.centavo.R;
import com.usc.centavo.model.Account;
import com.usc.centavo.viewmodel.AccountViewModel;
import java.util.List;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class AccountManagementFragment extends Fragment {
    private AccountViewModel viewModel;
    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private FloatingActionButton fabAdd;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_management, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewAccounts);
        fabAdd = view.findViewById(R.id.fabAddAccount);
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AccountAdapter();
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        viewModel.getAccountsLiveData().observe(getViewLifecycleOwner(), this::updateAccounts);
        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.onErrorHandled();
            }
        });
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void updateAccounts(List<Account> accounts) {
        adapter.setAccounts(accounts);
        if (accounts == null || accounts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showAddDialog() {
        showAccountDialog(null);
    }

    private void showEditDialog(Account account) {
        showAccountDialog(account);
    }

    private void showAccountDialog(@Nullable Account accountToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(accountToEdit == null ? "Add Account" : "Edit Account");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_account, null);
        final EditText nameInput = dialogView.findViewById(R.id.editTextAccountName);
        final EditText balanceInput = dialogView.findViewById(R.id.editTextAccountBalance);
        if (accountToEdit != null) {
            nameInput.setText(accountToEdit.getName());
            balanceInput.setText(String.valueOf(accountToEdit.getBalance()));
        }
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String balanceStr = balanceInput.getText().toString().trim();
            double balance = 0;
            try { balance = Double.parseDouble(balanceStr); } catch (NumberFormatException ignored) {}
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (accountToEdit == null) {
                viewModel.addAccount(name, balance);
            } else {
                accountToEdit.setName(name);
                accountToEdit.setBalance(balance);
                viewModel.updateAccount(accountToEdit);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private class AccountAdapter extends RecyclerView.Adapter<AccountViewHolder> {
        private List<Account> accounts;
        public void setAccounts(List<Account> accounts) {
            this.accounts = accounts;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
            return new AccountViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
            Account account = accounts.get(position);
            holder.bind(account);
            holder.itemView.setOnClickListener(v -> showEditDialog(account));
            holder.itemView.setOnLongClickListener(v -> {
                viewModel.deleteAccount(account.getAccountId());
                return true;
            });
        }
        @Override
        public int getItemCount() {
            return accounts == null ? 0 : accounts.size();
        }
    }

    private static class AccountViewHolder extends RecyclerView.ViewHolder {
        private final View nameView;
        private final View balanceView;
        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textViewAccountName);
            balanceView = itemView.findViewById(R.id.textViewAccountBalance);
        }
        public void bind(Account account) {
            ((android.widget.TextView) nameView).setText(account.getName());
            ((android.widget.TextView) balanceView).setText(String.format("%.2f", account.getBalance()));
        }
    }
} 