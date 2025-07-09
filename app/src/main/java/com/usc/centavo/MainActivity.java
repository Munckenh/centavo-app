package com.usc.centavo;

import android.graphics.Insets;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.centavo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);

        setupEdgeToEdge();
        setupNavigation();
        setupBottomNavigation();
    }

    private void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, 0);
            return insets;
        });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            return;
        }

        navController = navHostFragment.getNavController();
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.navigation_graph);

        // Set start destination based on authentication state
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            navGraph.setStartDestination(R.id.navigation_login);
        } else {
            navGraph.setStartDestination(R.id.navigation_home);
        }
        navController.setGraph(navGraph);
    }

    private void setupBottomNavigation() {
        if (navController == null) return;

        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        // Clear back stack when switching tabs
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
            navController.navigate(item.getItemId());
            return true;
        });

        // Hide bottom navigation based on auth screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_login || destination.getId() == R.id.navigation_register) {
                binding.bottomNavView.setVisibility(View.GONE);
            } else {
                binding.bottomNavView.setVisibility(View.VISIBLE);
            }
        });
    }
}