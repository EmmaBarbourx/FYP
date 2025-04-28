package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class WeekSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WeeklyPlanAdapter weeklyPlanAdapter;
    private MaterialButton archivedPlansButton;
    private BottomNavigationView bottomNavigationView;
    private Button generatePlanButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_selection);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewWeeks);
        archivedPlansButton  = findViewById(R.id.archivedPlansButton);
        generatePlanButton   = findViewById(R.id.generatePlanButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create a list of week numbers (weeks 1 to 6)
        List<Integer> weeks = Arrays.asList(1, 2, 3, 4, 5, 6);

        weeklyPlanAdapter = new WeeklyPlanAdapter(weeks, weekNumber -> {
            // When a week is clicked, launch RecoveryPlanActivity with the week number
            Intent intent = new Intent(WeekSelectionActivity.this, RecoveryPlanActivity.class);
            intent.putExtra("weekNumber", weekNumber);
            startActivity(intent);
        });

        // Generate Plan button - launch RecoveryPlanActivity with generatePlanImmediately
        generatePlanButton.setOnClickListener(v -> {
            Intent intent = new Intent(WeekSelectionActivity.this, RecoveryPlanActivity.class);
            intent.putExtra("generatePlanImmediately", true);
            startActivity(intent);
        });

        // Set up the Archived Plans button and its click listener
        archivedPlansButton.setOnClickListener(v -> {
            // Launch the new ArchivedPlansActivity
            Intent intent = new Intent(WeekSelectionActivity.this, ArchivedPlansActivity.class);
            startActivity(intent);
        });

        // Set up Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });

        // Check if a plan exists

        checkIfPlanExistsAndDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfPlanExistsAndDisplay();
    }

    private void checkIfPlanExistsAndDisplay() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        // user doc missing, plan doesn’t exist
                        showGeneratePlanUI();
                        return;
                    }

            String currentInjuryId = userDoc.getString("currentInjuryId");
            if (currentInjuryId == null) {
                // no injury - no plan
                showGeneratePlanUI();
                return;
            }

                    // check if there's at least 1 doc under week1/days
                    db.collection("users").document(userId)
                            .collection("recoveryPlan")
                            .document(currentInjuryId)
                            .collection("weeks")
                            .document("week1")
                            .collection("days")
                            .limit(1)

                            .addSnapshotListener((daysSnap, err) -> {
                                if (err != null) {
                                    showGeneratePlanUI();
                                    return;
                                }
                                if (daysSnap != null && !daysSnap.isEmpty()) {
                                    showWeeksUI();
                                } else {
                                    showGeneratePlanUI();
                                }
                            });
                })
                .addOnFailureListener(err -> showGeneratePlanUI());
    }

    private void showWeeksUI() {
        Log.d("WeekSel", ">>> showWeeksUI (should list weeks + Archived)");
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(weeklyPlanAdapter);

        generatePlanButton.setVisibility(View.GONE);
        archivedPlansButton.setVisibility(View.VISIBLE);


    }

    private void showGeneratePlanUI() {
        Log.d("WeekSel", ">>> showGeneratePlanUI (should show Generate + Archived)");
        // Hide the normal weeks/archived UI
        recyclerView.setVisibility(View.GONE);
        generatePlanButton.setVisibility(View.VISIBLE);
        archivedPlansButton.setVisibility(View.VISIBLE);
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
            handleLogout();
        }
    }

    private void handleLogout() {
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
