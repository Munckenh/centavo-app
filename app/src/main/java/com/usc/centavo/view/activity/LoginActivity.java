package com.usc.centavo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
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
import com.usc.centavo.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;
    private TextView registerLinkTextView;
    private EditText emailEditText, passwordEditText;
    private ProgressBar loadingProgressBar;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int horizontalPadding = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDimensionPixelSize(R.dimen.horizontal_padding),
                    getResources().getDisplayMetrics()
            );
            int verticalPadding = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDimensionPixelSize(R.dimen.vertical_padding),
                    getResources().getDisplayMetrics()
            );
            v.setPadding(
                    systemBars.left + horizontalPadding,
                    systemBars.top + verticalPadding,
                    systemBars.right + horizontalPadding,
                    systemBars.bottom + verticalPadding
            );
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initializeViews();
        setupListeners();
        setupObservers();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        loadingProgressBar = findViewById(R.id.pb_loading);
        loginButton = findViewById(R.id.btn_login);
        registerLinkTextView = findViewById(R.id.tv_register_link);
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean isValid = !emailEditText.getText().toString().isEmpty()
                        && !passwordEditText.getText().toString().isEmpty();
                loginButton.setEnabled(isValid);
            }
        };

        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        loginButton.setOnClickListener(v -> login());
        registerLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            loadingProgressBar.setVisibility(View.GONE);
            if (firebaseUser != null) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        viewModel.login(email, password);
    }
}