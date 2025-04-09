package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PainTrackingActivity extends AppCompatActivity {

    private Spinner painLevelSpinner;
    private EditText notesEditText;
    private Button savePainLogButton;
    private RecyclerView painLogRecyclerView;
    private String currentInjuryId;


    private PainLogAdapter painLogAdapter;
    private List<PainLog> painLogList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pain_tracking);



        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pain Tracking");
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Views
        painLevelSpinner = findViewById(R.id.painLevelSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        savePainLogButton = findViewById(R.id.savePainLogButton);
        painLogRecyclerView = findViewById(R.id.painLogRecyclerView);

        // Setup RecyclerView
        painLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        painLogAdapter = new PainLogAdapter(painLogList);
        painLogRecyclerView.setAdapter(painLogAdapter);

        // Load the current injury ID from the user profile
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentInjuryId = documentSnapshot.getString("currentInjuryId");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Save Pain Log Button Listener
        savePainLogButton.setOnClickListener(v -> savePainLog());

        // Load Pain Logs
        loadPainLogs();

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;


        });
    }

    private void savePainLog() {
        String painLevel = painLevelSpinner.getSelectedItem().toString(); // Get selected pain level
        String notes = notesEditText.getText().toString().trim();  // Get notes input

        if (painLevel.equals("Select pain level")) {
            Toast.makeText(this, "Please select a pain level.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user ID and current timestamp
        String userId = auth.getCurrentUser().getUid();
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Build a map for the pain log data
        HashMap<String, Object> painLog = new HashMap<>();
        painLog.put("painLevel", painLevel);
        painLog.put("notes", notes);
        painLog.put("timestamp", timestamp);
        painLog.put("archived", false);
        painLog.put("currentInjuryId", currentInjuryId);  // Add the current injury ID

        // Save the pain log document under the user's painLogs collection
        db.collection("users").document(userId).collection("painLogs")
                .document(timestamp)
                .set(painLog)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pain log saved successfully!", Toast.LENGTH_SHORT).show();
                    // Clear the fields
                    notesEditText.setText("");
                    painLevelSpinner.setSelection(0); // Reset spinner to default
                    // Refresh pain logs
                    loadPainLogs();
                })
                .addOnFailureListener(e -> {
                    // Show an error message if saving fails
                    if (e.getMessage().contains("PERMISSION_DENIED")) {
                        Toast.makeText(this, "Error: Permission denied. Check Firestore rules.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error saving pain log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Load the latest 7 pain logs from Firestore
    private void loadPainLogs() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("painLogs")
                .whereEqualTo("archived", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(7)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    painLogList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Convert document to PainLog object and add to list
                        PainLog painLog = document.toObject(PainLog.class);
                        painLogList.add(painLog);
                    }
                    // Notify the adapter that the data has changed
                    painLogAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading pain logs: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard && !this.getClass().equals(DashboardActivity.class)) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else if (itemId == R.id.nav_profile && !this.getClass().equals(ProfileActivity.class)) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        } else if (itemId == R.id.nav_settings && !this.getClass().equals(SettingsActivity.class)) {
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
