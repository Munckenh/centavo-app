package com.usc.centavo.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.usc.centavo.R;
import com.usc.centavo.viewmodel.HomeViewModel;

public class HomeActivity extends AppCompatActivity {
    private HomeViewModel viewModel;
    private TextView firstNameTextView;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int horizontalPadding = (int) TypedValue.applyDimension(
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

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        initializeViews();
        setupListeners();
        setupObservers();
    }

    private void initializeViews() {
        firstNameTextView = findViewById(R.id.tv_first_name);
        logoutButton = findViewById(R.id.btn_logout);
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> logout());
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(this, user -> {
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        viewModel.getUserDetailsLiveData().observe(this, userDetails -> {
            if (userDetails != null) {
                firstNameTextView.setText(userDetails.getFirstName());
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        viewModel.logout();
    }
}