package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.injuryrecoveryapplication.models.Exercise;
import com.example.injuryrecoveryapplication.utils.ExerciseDbBodyPartMapping;  // Make sure you created this
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecoveryPlanActivity is now fully switched to using ExerciseDB.
 * The old Wger logic (offset, merges, etc.) has been removed.
 */
public class RecoveryPlanActivity extends AppCompatActivity {

    private static final String TAG = "RecoveryPlanActivity";

    private RecyclerView recyclerView;
    private RecoveryPlanAdapter adapter;
    private RecyclerView.Adapter adapter1; // For showing the "week selection" at the end
    private List<DailyPlan> weeklyPlan = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;

    private TextView textViewInjuryType;
    private int weekNumber; // e.g. 1..6

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_plan);

        // Restore weekNumber from savedInstanceState if available; otherwise, get it from the intent
        if (savedInstanceState != null) {
            weekNumber = savedInstanceState.getInt("weekNumber", 0);
        } else {
            weekNumber = getIntent().getIntExtra("weekNumber", 0);
        }

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recovery Plan - Week " + weekNumber);
        }

        // Initialize Firestore and user ID
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize TextView for injury type
        textViewInjuryType = findViewById(R.id.textViewInjuryType);

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // If a valid weekNumber is provided, load that week’s daily plan;
        // otherwise, fetch the user's injury info and generate a plan
        if (weekNumber > 0) {
            loadDailyPlanForWeek(weekNumber);
        } else {
            fetchInjuryTypeAndGeneratePlan();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("weekNumber", weekNumber);
    }

    // Get the user's injury type from Firestore
    private void fetchInjuryTypeAndGeneratePlan() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String injuryType = documentSnapshot.getString("injuryType");
                        String currentInjuryId = documentSnapshot.getString("currentInjuryId");
                        if (injuryType != null && currentInjuryId != null) {
                            textViewInjuryType.setText("Injury Type: " + injuryType);
                            loadExistingRecoveryPlan(currentInjuryId, injuryType);
                        } else {
                            Toast.makeText(this, "No injury type found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching injury type", Toast.LENGTH_SHORT).show();
                });
    }

    // Check if a plan already exists in Firestore for the given week.
    // If it does, load it; if not, generate a new plan from ExerciseDB
    private void loadExistingRecoveryPlan(String currentInjuryId, String injuryType) {
        int weekNumber = getIntent().getIntExtra("weekNumber", 1);
        String weekId = "week" + weekNumber;

        // Query Firestore to see if there's a daily plan for this week
        db.collection("users").document(userId)
                .collection("recoveryPlan")
                .document(currentInjuryId)
                .collection("weeks")
                .document(weekId)
                .collection("days")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // We have an existing plan
                        weeklyPlan.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            DailyPlan plan = document.toObject(DailyPlan.class);
                            if (plan != null) weeklyPlan.add(plan);
                        }

                        // Update UI with existing plan
                        runOnUiThread(() -> {
                            adapter = new RecoveryPlanAdapter(weeklyPlan, this::updatePlanCompletion);
                            recyclerView.setAdapter(adapter);
                        });
                    } else {
                        // No plan for this week, generate a new one from ExerciseDB
                        fetchExercisesFromExerciseDb(injuryType);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RecoveryPlanActivity.this, "Error loading weekly plan", Toast.LENGTH_SHORT).show();
                });
    }

    // Load the daily plan for a specific week from Firestore.
    // If there is no saved plan, generate one from ExerciseDB
    private void loadDailyPlanForWeek(int weekNumber) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    String currentInjuryId = documentSnapshot.getString("currentInjuryId");
                    if (currentInjuryId == null) return;

                    String injuryType = documentSnapshot.getString("injuryType");
                    if (injuryType != null) {
                        textViewInjuryType.setText("Injury Type: " + injuryType);
                    }

                    String weekId = "week" + weekNumber;
                    db.collection("users").document(userId)
                            .collection("recoveryPlan")
                            .document(currentInjuryId)
                            .collection("weeks")
                            .document(weekId)
                            .collection("days")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (querySnapshot.isEmpty()) {
                                    // No daily plans exist -> generate a new plan
                                    fetchExercisesFromExerciseDb(injuryType);
                                    return;
                                }

                                // Convert Firestore docs into DailyPlan objects
                                List<DailyPlan> dailyPlans = new ArrayList<>();
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    DailyPlan plan = doc.toObject(DailyPlan.class);
                                    if (plan != null) {
                                        dailyPlans.add(plan);
                                    }
                                }

                                // Sort daily plans by day number ("Day 1")
                                Collections.sort(dailyPlans, (p1, p2) -> {
                                    int day1 = extractDayNumber(p1.getDay());
                                    int day2 = extractDayNumber(p2.getDay());
                                    return Integer.compare(day1, day2);
                                });

                                // Count how many days are completed in this week
                                int countCompleted = 0;
                                for (DailyPlan plan : dailyPlans) {
                                    if (plan.isCompleted()) {
                                        countCompleted++;
                                    }
                                }
                                final int currentWeekCompletedCount = countCompleted;

                                // If it's week 1, we have no "previous weeks"
                                if (weekNumber == 1) {
                                    runOnUiThread(() -> {
                                        RecoveryPlanAdapter adapter = new RecoveryPlanAdapter(dailyPlans, this::updatePlanCompletion);
                                        adapter.setCurrentWeekNumber(weekNumber);
                                        adapter.setOverallCompletedDays(currentWeekCompletedCount);
                                        adapter.setPreviousWeeksCompletedDays(0);
                                        recyclerView.setAdapter(adapter);
                                    });
                                } else {
                                    // For weeks > 1, add up previous weeks completed days
                                    List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                                    for (int w = 1; w < weekNumber; w++) {
                                        String prevWeekId = "week" + w;
                                        Task<QuerySnapshot> task = db.collection("users").document(userId)
                                                .collection("recoveryPlan")
                                                .document(currentInjuryId)
                                                .collection("weeks")
                                                .document(prevWeekId)
                                                .collection("days")
                                                .get();
                                        tasks.add(task);
                                    }

                                    Tasks.whenAllSuccess(tasks)
                                            .addOnSuccessListener(results -> {
                                                int cumulativeCompleted = 0;
                                                for (Object obj : results) {
                                                    QuerySnapshot snapshot = (QuerySnapshot) obj;
                                                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                                        DailyPlan plan = doc.toObject(DailyPlan.class);
                                                        if (plan != null && plan.isCompleted()) {
                                                            cumulativeCompleted++;
                                                        }
                                                    }
                                                }
                                                final int previousWeeksCompleted = cumulativeCompleted;

                                                runOnUiThread(() -> {
                                                    RecoveryPlanAdapter adapter = new RecoveryPlanAdapter(dailyPlans, this::updatePlanCompletion);
                                                    adapter.setCurrentWeekNumber(weekNumber);
                                                    adapter.setOverallCompletedDays(currentWeekCompletedCount);
                                                    adapter.setPreviousWeeksCompletedDays(previousWeeksCompleted);
                                                    recyclerView.setAdapter(adapter);
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                runOnUiThread(() ->
                                                        Toast.makeText(RecoveryPlanActivity.this,
                                                                "Error loading previous weeks' plan", Toast.LENGTH_SHORT).show());
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                runOnUiThread(() ->
                                        Toast.makeText(RecoveryPlanActivity.this, "Error loading weekly plan", Toast.LENGTH_SHORT).show());
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle errors getting the user profile if needed
                });
    }

    // Extract the day number from a string like "Day 1". If parsing fails, return 0
    private int extractDayNumber(String day) {
        if (day != null && day.startsWith("Day ")) {
            try {
                return Integer.parseInt(day.substring(4).trim());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse day number from: " + day, e);
            }
        }
        return 0;
    }

    // Use ExerciseDbApiService to generate a new 6-week plan based on the injury type.
    private void fetchExercisesFromExerciseDb(String injuryType) {
        Log.d(TAG, "fetchExercisesFromExerciseDb: for injuryType=" + injuryType);


        // First map to area
        String mappedArea = mapInjuryTypeToArea(injuryType);
        if (mappedArea == null) mappedArea = injuryType; // fallback

        // Then map area -> exerciseDB body part
        String bodyPart = ExerciseDbBodyPartMapping.getBodyPartForInjury(mappedArea);

        ExerciseDbApiService apiService = new ExerciseDbApiService();
        apiService.fetchExercisesByBodyPartAll(bodyPart, new ExerciseDbApiService.ExerciseDbCallback() {
            @Override
            public void onSuccess(List<Exercise> exercises) {
                Log.d(TAG, "ExerciseDB returned " + exercises.size() + " exercises for bodyPart=" + bodyPart);

                // Filter out exercises that use heavier equipment
                List<Exercise> filtered = new ArrayList<>();
                for (Exercise ex : exercises) {
                    if (ex.getEquipment() != null) {
                        // Convert to lower case
                        String eq = ex.getEquipment().toLowerCase();

                        // Keep it if it's "body weight", "none"
                        if (eq.contains("bodyweight") || eq.contains("body weight") || eq.contains("none")) {
                            filtered.add(ex);
                        }
                    }
                }
                Log.d(TAG, "Filtered down to " + filtered.size() + " exercises (light/no equipment).");

                //Generate a 6-week plan using only these filtered exercises
                RecoveryPlanGenerator generator = new RecoveryPlanGenerator();
                List<WeeklyRecoveryPlan> weeklyRecoveryPlans =
                        generator.generateMultipleWeeklyPlans(injuryType, filtered, 6);

                // Save to Firestore
                saveWeeklyPlan(weeklyRecoveryPlans);

                // Show the user a "Week Selection" list
                runOnUiThread(() -> {
                    List<Integer> weeks = new ArrayList<>();
                    for (int i = 1; i <= 6; i++) {
                        weeks.add(i);
                    }
                    WeeklyPlanAdapter adapter1 = new WeeklyPlanAdapter(weeks, RecoveryPlanActivity.this::onWeekClick);
                    recyclerView.setAdapter(adapter1);

                    Toast.makeText(RecoveryPlanActivity.this,
                            "Fetched " + exercises.size() + " from ExerciseDB; Using " + filtered.size() + " light-equipment exercises",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "ExerciseDB fetch failed: " + errorMessage);
                runOnUiThread(() -> Toast.makeText(RecoveryPlanActivity.this,
                        "Failed to fetch from ExerciseDB: " + errorMessage,
                        Toast.LENGTH_SHORT).show());
            }
        });
    }
    // Save the newly generated 6-week plan to Firestore
    private void saveWeeklyPlan(List<WeeklyRecoveryPlan> weeklyRecoveryPlans) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentInjuryId = documentSnapshot.getString("currentInjuryId");
                        if (currentInjuryId == null) return;

                        // For each of the 6 weeks
                        for (int weekIndex = 0; weekIndex < weeklyRecoveryPlans.size(); weekIndex++) {
                            String weekId = "week" + (weekIndex + 1);
                            WeeklyRecoveryPlan weeklyPlan = weeklyRecoveryPlans.get(weekIndex);

                            // For each day in that week, store it under Firestore
                            for (DailyPlan plan : weeklyPlan.getDailyPlans()) {
                                db.collection("users").document(userId)
                                        .collection("recoveryPlan")
                                        .document(currentInjuryId)
                                        .collection("weeks")
                                        .document(weekId)
                                        .collection("days")
                                        .document(plan.getDay())
                                        .set(plan)
                                        .addOnSuccessListener(aVoid -> {

                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle error saving the daily plan
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error fetching user doc
                });
    }

    // Update a specific day's completion status in Firestore
    public void updatePlanCompletion(String day, boolean completed) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentInjuryId = documentSnapshot.getString("currentInjuryId");
                        if (currentInjuryId == null) return;

                        String weekId = "week" + weekNumber;
                        db.collection("users").document(userId)
                                .collection("recoveryPlan")
                                .document(currentInjuryId)
                                .collection("weeks")
                                .document(weekId)
                                .collection("days")
                                .document(day)
                                .update("completed", completed)
                                .addOnFailureListener(e -> {
                                    // Handle error updating completion
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error fetching user doc
                });
    }

    // Map an injury type like "ACL tear" or "Rotator Cuff Tear" to a general area such as "Knee" or "Shoulder"
    public static String mapInjuryTypeToArea(String injuryType) {
        if (injuryType == null) return null;
        injuryType = injuryType.toLowerCase();
        Map<String, String> injuryTypeToAreaMap = new HashMap<>();

        // Shoulder injuries
        injuryTypeToAreaMap.put("dislocation", "Shoulder");
        injuryTypeToAreaMap.put("rotator cuff tear", "Shoulder");
        injuryTypeToAreaMap.put("tendinitis", "Shoulder");

        // Elbow injuries
        injuryTypeToAreaMap.put("tennis elbow", "Elbow");
        injuryTypeToAreaMap.put("golfer's elbow", "Elbow");
        injuryTypeToAreaMap.put("bursitis", "Elbow");

        // Wrist injuries
        injuryTypeToAreaMap.put("carpal tunnel syndrome", "Wrist");
        injuryTypeToAreaMap.put("sprain (wrist)", "Wrist");
        injuryTypeToAreaMap.put("tendonitis (wrist)", "Wrist");

        // Lower Back injuries
        injuryTypeToAreaMap.put("herniated disc", "Lower Back");
        injuryTypeToAreaMap.put("muscle strain (back)", "Lower Back");
        injuryTypeToAreaMap.put("sciatica", "Lower Back");

        // Neck injuries
        injuryTypeToAreaMap.put("whiplash", "Neck");
        injuryTypeToAreaMap.put("cervical disc injury", "Neck");
        injuryTypeToAreaMap.put("strain (neck)", "Neck");

        // Knee injuries
        injuryTypeToAreaMap.put("acl tear", "Knee");
        injuryTypeToAreaMap.put("meniscus tear", "Knee");
        injuryTypeToAreaMap.put("patellar tendinitis", "Knee");

        // Ankle injuries
        injuryTypeToAreaMap.put("sprain", "Ankle");
        injuryTypeToAreaMap.put("achilles tendinitis", "Ankle");
        injuryTypeToAreaMap.put("fracture", "Ankle");

        // Foot injuries
        injuryTypeToAreaMap.put("plantar fasciitis", "Foot");
        injuryTypeToAreaMap.put("fracture (foot)", "Foot");
        injuryTypeToAreaMap.put("sprain (foot)", "Foot");

        // Hip injuries
        injuryTypeToAreaMap.put("hip flexor strain", "Hip");
        injuryTypeToAreaMap.put("labral tear", "Hip");
        injuryTypeToAreaMap.put("arthritis (hip)", "Hip");

        // General muscle issues
        injuryTypeToAreaMap.put("strain", "General Muscle");
        injuryTypeToAreaMap.put("tear", "General Muscle");
        injuryTypeToAreaMap.put("cramps", "General Muscle");

        return injuryTypeToAreaMap.get(injuryType);
    }


    private void onWeekClick(int weekNumber) {
        Intent intent = new Intent(this, RecoveryPlanActivity.class);
        intent.putExtra("weekNumber", weekNumber);
        startActivity(intent);
    }

    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
        }
        else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        else if (itemId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if (itemId == R.id.nav_logout) {
            handleLogout();
        }
        finish();
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
