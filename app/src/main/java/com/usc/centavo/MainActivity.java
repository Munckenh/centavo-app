package com.usc.centavo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.centavo.repository.UserRepository;
import com.usc.centavo.view.activity.HomeActivity;
import com.usc.centavo.view.activity.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private boolean keepSplashScreenVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreenVisible);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserRepository userRepository = UserRepository.getInstance();
            userRepository.fetchUserDetails();

            userRepository.getUserDetailsLiveData().observe(this, userDetails -> {
                if (userDetails != null) {
                    navigateToHome();
                }
            });

            userRepository.getErrorLiveData().observe(this, error -> {
                if (error != null) {
                    navigateToLogin();
                }
            });
        } else {
            navigateToLogin();
        }
    }

    private void navigateToHome() {
        keepSplashScreenVisible = false;
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        keepSplashScreenVisible = false;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}