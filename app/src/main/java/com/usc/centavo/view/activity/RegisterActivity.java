package com.usc.centavo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.usc.centavo.R;
import com.usc.centavo.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private RegisterViewModel viewModel;
    private TextView loginLinkTextView;
    private EditText emailEditText, passwordEditText, firstNameEditText, lastNameEditText;
    private ProgressBar loadingProgressBar;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        initializeViews();
        setupListeners();
        setupObservers();
    }

    private void initializeViews() {
        loginLinkTextView = findViewById(R.id.tv_login_link);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        firstNameEditText = findViewById(R.id.et_first_name);
        lastNameEditText = findViewById(R.id.et_last_name);
        loadingProgressBar = findViewById(R.id.pb_loading);
        registerButton = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean isValid = !emailEditText.getText().toString().trim().isEmpty() &&
                        !passwordEditText.getText().toString().trim().isEmpty() &&
                        !firstNameEditText.getText().toString().trim().isEmpty() &&
                        !lastNameEditText.getText().toString().trim().isEmpty();
                registerButton.setEnabled(isValid);
            }
        };

        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        firstNameEditText.addTextChangedListener(textWatcher);
        lastNameEditText.addTextChangedListener(textWatcher);
        registerButton.setOnClickListener(v -> register());
        loginLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            loadingProgressBar.setVisibility(View.GONE);
            if (firebaseUser != null) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            loadingProgressBar.setVisibility(View.GONE);
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void register() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password should at least be 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        viewModel.register(email, password, firstName, lastName);
    }
}