package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private LineChart painTrendsChart;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration painLogListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize the LineChart
        painTrendsChart = findViewById(R.id.painTrendsChart);

        // Set up feature box click listeners
        setupFeatureBoxListeners();

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard); // Highlight Dashboard as active

        // Listen for pain log updates and update the chart dynamically
        fetchAndListenToPainLogs();

        // Fetch and update the recovery progress card based on user's most recent progress
        fetchRecoveryProgress();
    }

    private void setupFeatureBoxListeners() {
        findViewById(R.id.recoveryPlanFeature).setOnClickListener(v -> {
            startActivity(new Intent(this, WeekSelectionActivity.class));
        });
        findViewById(R.id.painTrackingFeature).setOnClickListener(v -> {
            startActivity(new Intent(this, PainTrackingActivity.class));
        });
        findViewById(R.id.exerciseFeature).setOnClickListener(v -> {
            startActivity(new Intent(this, SelectPlanExercisesActivity.class));
        });
        findViewById(R.id.physioRecommendationsFeature).setOnClickListener(v -> {
            // Pass a default injury type directly to PhysioRecommendationsActivity
            Intent intent = new Intent(DashboardActivity.this, PhysioRecommendationsActivity.class);
            intent.putExtra("injuryType", "defaultInjuryType"); // Replace with actual injury type later if needed
            startActivity(intent);
        });
    }

    // Listen for the latest 7 pain log entries and update the chart
    private void fetchAndListenToPainLogs() {
        String userId = auth.getCurrentUser().getUid();

        painLogListener = db.collection("users").document(userId).collection("painLogs")
                .whereEqualTo("archived", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(7) // Last 7 entries
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("DashboardActivity", "Error fetching pain logs: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<Entry> entries = new ArrayList<>();
                        int dayIndex = 1; // X-axis value for each entry

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String painLevel = document.getString("painLevel");

                            if (painLevel != null) {
                                entries.add(new Entry(dayIndex++, Float.parseFloat(painLevel)));
                            }
                        }

                        updatePainTrendsChart(entries); // Update chart with new data
                    }
                });
    }

    private void updatePainTrendsChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Pain Levels");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);// Create line data
        painTrendsChart.setData(lineData);// Set data on chart

        painTrendsChart.getDescription().setEnabled(false);
        painTrendsChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        painTrendsChart.getXAxis().setGranularity(1f);
        painTrendsChart.getXAxis().setLabelCount(entries.size(), true);
        painTrendsChart.getAxisRight().setEnabled(false);
        painTrendsChart.getAxisLeft().setAxisMinimum(0);
        painTrendsChart.getAxisLeft().setAxisMaximum(10);
        painTrendsChart.animateY(1000);
        painTrendsChart.invalidate();
    }

    private void fetchRecoveryProgress() {
        String userId = auth.getCurrentUser().getUid();

        // Get the user's document to retrieve the currentInjuryId
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentInjuryId = documentSnapshot.getString("currentInjuryId");
                        if (currentInjuryId == null) {
                            // currentInjuryId is missing; cannot proceed further
                            return;
                        }

                        // 6 weeks in the recovery plan
                        final int totalWeeks = 6;
                        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

                        // For each week, create a task to fetch its "days" subcollection
                        for (int i = 1; i <= totalWeeks; i++) {
                            String weekId = "week" + i;
                            Task<QuerySnapshot> task = db.collection("users").document(userId)
                                    .collection("recoveryPlan")
                                    .document(currentInjuryId)
                                    .collection("weeks")
                                    .document(weekId)
                                    .collection("days")
                                    .get();
                            tasks.add(task);
                        }

                        // When all week tasks complete, calculate the recovery progress
                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    int totalCompletedCount = 0;
                                    // Loop through each week's result to count completed days
                                    for (Object obj : results) {
                                        QuerySnapshot snapshot = (QuerySnapshot) obj;
                                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                            DailyPlan plan = doc.toObject(DailyPlan.class);
                                            if (plan != null && plan.isCompleted()) {
                                                totalCompletedCount++;
                                            }
                                        }
                                    }

                                    // Determine the active week and progress within that week
                                    int activeWeek;
                                    int progressInWeek;
                                    if (totalCompletedCount == 0) {
                                        activeWeek = 1;
                                        progressInWeek = 0;
                                    } else if (totalCompletedCount % 7 == 0) {
                                        // Completed full weeks
                                        activeWeek = totalCompletedCount / 7;
                                        progressInWeek = 7;
                                    } else {
                                        activeWeek = totalCompletedCount / 7 + 1;
                                        progressInWeek = totalCompletedCount % 7;
                                    }

                                    // Calculate the percentage for the ProgressBar (7 days per week)
                                    int progressPercent = (int) ((progressInWeek / 7f) * 100);

                                    runOnUiThread(() -> {
                                        // Update the ProgressBar and recovery progress details text
                                        ProgressBar progressBar = findViewById(R.id.recoveryProgressBar);
                                        TextView progressDetails = findViewById(R.id.recoveryProgressDetails);
                                        progressBar.setProgress(progressPercent);
                                        progressDetails.setText("Week " + activeWeek + ": " + progressInWeek + "/7 days completed");
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    // Show an error message if there is an issue loading recovery progress
                                    runOnUiThread(() ->
                                            Toast.makeText(DashboardActivity.this, "Error loading recovery progress", Toast.LENGTH_SHORT).show()
                                    );
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors when fetching the user profile if needed
                });
    }

    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            // Already on the Dashboard
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (painLogListener != null) {
            painLogListener.remove();
        }
    }
}
