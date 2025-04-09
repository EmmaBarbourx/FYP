package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the title for the Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::handleNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings); // Highlight Settings as active
    }


    private boolean handleNavigation(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_dashboard) {
            navigateTo(DashboardActivity.class);
        } else if (itemId == R.id.nav_profile) {
            navigateTo(ProfileActivity.class);
        } else if (itemId == R.id.nav_settings) {
            // Already on the Settings page
            return true;
        } else if (itemId == R.id.nav_logout) {
            navigateTo(LoginActivity.class);
        }
        return false;
    }


    private void navigateTo(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
        finish();
    }
}
