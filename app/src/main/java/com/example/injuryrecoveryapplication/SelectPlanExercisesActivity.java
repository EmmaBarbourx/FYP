package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SelectPlanExercisesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_plan_exercises);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarSelectExercises);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select Plan Exercises");
        }


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationViewSelectExercises);
        bottomNav.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });

        // placeholder text
        TextView placeholder = findViewById(R.id.textViewPlaceholder);
        placeholder.setText("List of Plan Exercises goes here...");
    }


    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        } else if (itemId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        } else if (itemId == R.id.nav_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}
