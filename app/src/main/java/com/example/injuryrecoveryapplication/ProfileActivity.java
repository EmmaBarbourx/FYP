package com.example.injuryrecoveryapplication;

import static android.content.ContentValues.TAG;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.google.firebase.firestore.SetOptions;

public class ProfileActivity extends AppCompatActivity {

    private EditText ageEditText;
    private RadioGroup genderRadioGroup;
    private Spinner fitnessLevelSpinner;
    private Spinner injuryAreaSpinner;
    private Spinner injuryTypeSpinner;
    private Button saveButton;
    private String oldInjury;


    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Views
        ageEditText = findViewById(R.id.ageEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        fitnessLevelSpinner = findViewById(R.id.fitnessLevelSpinner);
        injuryAreaSpinner = findViewById(R.id.injuryAreaSpinner);
        injuryTypeSpinner = findViewById(R.id.injuryTypeSpinner);
        saveButton = findViewById(R.id.saveButton);

        // Setup Spinners
        setupInjuryAreaSpinnerListener();
        populateFitnessLevelSpinner();

        // Load User Profile
        loadUserProfile();

        // Save Button Listener
        saveButton.setOnClickListener(view -> saveUserProfile());

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> handleNavigation(item.getItemId()));
        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Highlight Profile as active
    }

    // Populate the fitness level spinner with options
    private void populateFitnessLevelSpinner() {
        String[] fitnessLevels = {"Select fitness level", "Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fitnessLevels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fitnessLevelSpinner.setAdapter(adapter);
    }

    // Listen for changes in the injury area spinner to update the injury type spinner
    private void setupInjuryAreaSpinnerListener() {
        injuryAreaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArea = parent.getItemAtPosition(position).toString();
                populateInjuryTypeSpinner(selectedArea); // Populate the injury type spinner
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                injuryTypeSpinner.setAdapter(null); // Clear if nothing is selected
            }
        });
    }

    // Update the injury type spinner based on the chosen injury area
    private void populateInjuryTypeSpinner(String injuryArea) {
        HashMap<String, String[]> injuryTypeMap = new HashMap<>();
        injuryTypeMap.put("Shoulder", new String[]{"Dislocation", "Rotator Cuff Tear", "Tendinitis"});
        injuryTypeMap.put("Elbow", new String[]{"Tennis Elbow", "Golfer's Elbow", "Bursitis"});
        injuryTypeMap.put("Wrist", new String[]{"Carpal Tunnel Syndrome", "Sprain", "Tendonitis"});
        injuryTypeMap.put("Lower Back", new String[]{"Herniated Disc", "Muscle Strain", "Sciatica"});
        injuryTypeMap.put("Neck", new String[]{"Whiplash", "Cervical Disc Injury", "Strain"});
        injuryTypeMap.put("Knee", new String[]{"ACL Tear", "Meniscus Tear", "Patellar Tendinitis"});
        injuryTypeMap.put("Ankle", new String[]{"Sprain", "Achilles Tendinitis", "Fracture"});
        injuryTypeMap.put("Foot", new String[]{"Plantar Fasciitis", "Fracture", "Sprain"});
        injuryTypeMap.put("Hip", new String[]{"Hip Flexor Strain", "Labral Tear", "Arthritis"});
        injuryTypeMap.put("General Muscle", new String[]{"Strain", "Tear", "Cramps"});

        // Get injury types for the selected area- default to "Select injury type" if not found
        String[] injuryTypes = injuryTypeMap.getOrDefault(injuryArea, new String[]{"Select injury type"});

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, injuryTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        injuryTypeSpinner.setAdapter(adapter);
    }

    // Load the user's profile from Firestore and update the UI
    private void loadUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userDoc = db.collection("users").document(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ageEditText.setText(documentSnapshot.getString("age"));
                String gender = documentSnapshot.getString("gender");
                if (gender != null) {
                    int genderId = gender.equals("Male") ? R.id.genderMale :
                            gender.equals("Female") ? R.id.genderFemale : R.id.genderOther;
                    genderRadioGroup.check(genderId);
                }
                // Get fitness level, injury area, and injury type from profile
                String fitnessLevel = documentSnapshot.getString("fitnessLevel");
                String injuryArea = documentSnapshot.getString("injuryArea");
                String injuryType = documentSnapshot.getString("injuryType");

                // Save the current injury as the old injury for later comparison
                oldInjury = injuryType;

                // Update the fitness level spinner selection
                setSpinnerSelection(fitnessLevelSpinner, fitnessLevel);

                if (injuryArea != null) {
                    // Set the injury area spinner's selection.
                    setSpinnerSelection(injuryAreaSpinner, injuryArea);

                    // Populate the injury type spinner based on the chosen injury area
                    populateInjuryTypeSpinner(injuryArea);

                    // Delay setting the injury type selection to make sure the adapter has been updated
                    injuryTypeSpinner.postDelayed(() -> {
                        // Set the injury type spinner selection using the value from Firestore
                        setSpinnerSelection(injuryTypeSpinner, injuryType);
                    }, 100); // 100ms delay
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Save the updated profile back to Firestore
    private void saveUserProfile() {
        String age = ageEditText.getText().toString().trim();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        String gender = selectedGenderId != -1 ? ((RadioButton) findViewById(selectedGenderId)).getText().toString() : null;
        String fitnessLevel = fitnessLevelSpinner.getSelectedItem().toString();
        String injuryArea = injuryAreaSpinner.getSelectedItem().toString();
        String injuryType = injuryTypeSpinner.getSelectedItem().toString();

        // Check if all fields are filled
        if (age.isEmpty() || gender == null || fitnessLevel.equals("Select fitness level") ||
                injuryArea.equals("Select injury area") || injuryType.equals("Select injury type")) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the injury has changed compared to the old injury
        if (oldInjury != null && !oldInjury.equals(injuryType)) {
            // Show confirmation dialog before proceeding.
            onInjuryChangeRequested(injuryType);
        } else {
            // Injury hasn't changed, so just update the profile normally.
            updateUserProfileWithoutReset(age, gender, fitnessLevel, injuryArea, injuryType);
        }
    }


        // Called when the user attempts to change their injury
        private void onInjuryChangeRequested(String newInjury) {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Reset Recovery Progress")
                    .setMessage("Changing your injury will reset all your recovery progress and add the plan to Archived Plans. Are you sure you want to proceed?")
                    .setPositiveButton("Confirm", (dialog, which) -> {

                        archiveAllData(() -> updateUserProfileWithNewInjury(newInjury));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // User cancelled: do nothing
                        dialog.dismiss();
                    })
                    .show();
        }

    // Helper method to archive both recovery plan data and pain logs
    private void archiveAllData(Runnable onComplete) {
        // First archive the recovery plan, then archive the pain logs
        archiveRecoveryData(() -> {
            archivePainLogs(onComplete);
        });
    }

    // Archive recovery plan data with additional fields
    private void archiveRecoveryData(Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userDoc = db.collection("users").document(userId);

        Log.d(TAG, "Starting archiveRecoveryData...");
        // Get the current active plan's ID from the user's document.
        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentInjuryId = documentSnapshot.getString("currentInjuryId");
                if (currentInjuryId == null) {
                    Log.e(TAG, "archiveRecoveryData: currentInjuryId is null. Cannot archive plan.");
                    onComplete.run();
                    return;
                }
                Log.d(TAG, "archiveRecoveryData: currentInjuryId = " + currentInjuryId);

                // Reference to the active recovery plan weeks
                CollectionReference activeWeeksRef = userDoc.collection("recoveryPlan")
                        .document(currentInjuryId).collection("weeks");

                // Copy the subcollections (weeks/days) to archivedRecoveryPlans
                activeWeeksRef.get().addOnSuccessListener(weekSnapshots -> {
                    if (weekSnapshots.isEmpty()) {
                        Log.d(TAG, "No active plan weeks found to archive for plan " + currentInjuryId);
                    } else {
                        Log.d(TAG, "Found " + weekSnapshots.size() + " weeks to archive for plan " + currentInjuryId);
                        for (DocumentSnapshot weekDoc : weekSnapshots.getDocuments()) {
                            String weekId = weekDoc.getId();
                            Log.d(TAG, "Archiving week doc: " + weekId);
                            weekDoc.getReference().collection("days").get()
                                    .addOnSuccessListener(daysSnapshot -> {
                                        Log.d(TAG, "Found " + daysSnapshot.size() + " days in week " + weekId);
                                        for (DocumentSnapshot dayDoc : daysSnapshot.getDocuments()) {
                                            if (dayDoc.exists() && dayDoc.getData() != null) {
                                                Log.d(TAG, "Copying day doc " + dayDoc.getId() + " from week " + weekId);
                                                // Write to archived collection
                                                userDoc.collection("archivedRecoveryPlans")
                                                        .document(currentInjuryId)
                                                        .collection("weeks")
                                                        .document(weekId)
                                                        .collection("days")
                                                        .document(dayDoc.getId())
                                                        .set(dayDoc.getData())
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "Archived day " + dayDoc.getId() + " from week " + weekId);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Error archiving day " + dayDoc.getId() + " from week " + weekId, e);
                                                        });
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error fetching days for week " + weekId, e);
                                    });
                        }
                    }
                    //  Update the top-level recoveryPlan document with archive fields.
                    HashMap<String, Object> archiveFields = new HashMap<>();
                    archiveFields.put("archived", true);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedDate = sdf.format(new Date());
                    Log.d(TAG, "Formatted archivedDate: " + formattedDate);
                    archiveFields.put("archivedDate", formattedDate);
                    // Also store injuryArea and injuryType from the user document
                    archiveFields.put("injuryArea", documentSnapshot.getString("injuryArea"));
                    archiveFields.put("injuryType", documentSnapshot.getString("injuryType"));

                    // Use update() to merge the new fields into the existing recoveryPlan document
                    userDoc.collection("recoveryPlan").document(currentInjuryId)
                            .update(archiveFields)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Successfully updated archive fields on doc: " + currentInjuryId);
                                onComplete.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating archive fields on doc " + currentInjuryId, e);
                                onComplete.run();
                            });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching active recovery plan weeks", e);
                    onComplete.run();
                });

            } else {
                Log.e(TAG, "User document does not exist for archiving. Aborting archiveRecoveryData.");
                onComplete.run();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching user document for archiving", e);
            onComplete.run();
        });
    }


    private void archivePainLogs(Runnable onComplete) {
        String userId = FirebaseAuth.getInstance().getUid();

        // Query the active pain logs (those not yet archived)
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userDoc  -> {

                    String currentInjuryId = userDoc.getString("currentInjuryId");
                    if (currentInjuryId == null || currentInjuryId.isEmpty()) {
                        onComplete.run();            // nothing to archive
                        return;
                    }

                    db.collection("users").document(userId)
                            .collection("painLogs")
                            .document(currentInjuryId)
                            .collection("logs")
                            .get()
                            .addOnSuccessListener(logsSnapshot -> {

                                List<Task<Void>> copyTasks = new ArrayList<>();

                                for (DocumentSnapshot doc : logsSnapshot.getDocuments()) {
                                    Log.d("archivePainLogs", "Archiving pain log: " + doc.getId());


                                    copyTasks.add(
                                            db.collection("users").document(userId)
                                                    .collection("archivedPainLogs")
                                                    .document(currentInjuryId)
                                                    .collection("logs")
                                                    .document(doc.getId())
                                                    .set(doc.getData())
                                    );

                                    // delete original
                                    copyTasks.add(doc.getReference().delete());
                                }

                                Tasks.whenAll(copyTasks)
                                        .addOnSuccessListener(x -> {
                                            Log.d("archivePainLogs", "Successfully archived all pain logs.");
                                            onComplete.run();
                                        })
                                        .addOnFailureListener(err -> {
                                            Log.e("archivePainLogs", "Error archiving pain logs", err);
                                            onComplete.run();   // still continue the workflow
                                        });
                            })
                            .addOnFailureListener(err -> {
                                Log.e("archivePainLogs", "Error fetching live pain logs", err);
                                onComplete.run();
                            });
                })
                .addOnFailureListener(err -> {
                    Log.e("archivePainLogs", "Error fetching user document", err);
                    onComplete.run();
                });
    }

        // Updates the profile with the new injury and generates a new injury ID
        private void updateUserProfileWithNewInjury(String newInjury) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Generate a new injury ID
            String newInjuryId = java.util.UUID.randomUUID().toString();

            // Prepare the updated profile data.
            HashMap<String, Object> updatedProfile = new HashMap<>();
            updatedProfile.put("injuryArea", injuryAreaSpinner.getSelectedItem().toString());
            updatedProfile.put("injuryType", newInjury);
            updatedProfile.put("injuryId", newInjuryId);
            updatedProfile.put("currentInjuryId", newInjuryId);
            updatedProfile.put("age", ageEditText.getText().toString().trim());
            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            String gender = selectedGenderId != -1 ? ((RadioButton) findViewById(selectedGenderId)).getText().toString() : null;
            updatedProfile.put("gender", gender);
            updatedProfile.put("fitnessLevel", fitnessLevelSpinner.getSelectedItem().toString());

            db.collection("users").document(userId)
                    .update(updatedProfile)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Profile updated and recovery progress reset!", Toast.LENGTH_SHORT).show();
                        // Update the oldInjury field to reflect the new injury.
                        oldInjury = newInjury;

                        // Create a new active recovery plan document with the newInjuryId.
                        // This ensures that the document ID in recoveryPlan matches currentInjuryId.
                        HashMap<String, Object> activePlanInitData = new HashMap<>();
                        activePlanInitData.put("injuryType", newInjury);
                        activePlanInitData.put("createdAt", System.currentTimeMillis());
                        db.collection("users").document(userId)
                                .collection("recoveryPlan")
                                .document(newInjuryId)
                                .set(activePlanInitData)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "New active recovery plan created with ID: " + newInjuryId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating new active recovery plan", e);
                                });


                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        // Updates the profile normally (when the injury hasn't changed)
        private void updateUserProfileWithoutReset (String age, String gender, String
        fitnessLevel, String injuryArea, String injuryType){
            String userId = auth.getCurrentUser().getUid();
            DocumentReference userDoc = db.collection("users").document(userId);

            HashMap<String, Object> updatedProfile = new HashMap<>();
            updatedProfile.put("age", age);
            updatedProfile.put("gender", gender);
            updatedProfile.put("fitnessLevel", fitnessLevel);
            updatedProfile.put("injuryArea", injuryArea);
            updatedProfile.put("injuryType", injuryType);

            // Update the profile document in Firestore
            userDoc.update(updatedProfile)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileActivity.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }



    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = -1;
            // Loop through adapter items and find a match ignoring case and extra whitespace
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = adapter.getItem(i);
                if (item != null && item.trim().equalsIgnoreCase(value.trim())) {
                    position = i;
                    break;
                }
            }
            // If a match was found, update the spinner selection
            if (position != -1) {
                spinner.setSelection(position);
            }
        }
    }

    private boolean handleNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else if (itemId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
        } else if (itemId == R.id.nav_profile) {
            return true;
        }
        return false;
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
