package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.List;

public class ArchivedPlansActivity extends AppCompatActivity {

    private static final String TAG = "ArchivedPlansActivity";
    private RecyclerView recyclerView;
    private ArchivedPlansAdapter adapter;
    private List<ArchivedPlan> archivedPlansList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_plans);

        // Set up Toolbar with title
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Archived Plans");
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewArchivedPlans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Load archived recovery plans from Firestore
        loadArchivedPlans();
    }

    private void loadArchivedPlans() {
        String userId = auth.getCurrentUser().getUid();
        CollectionReference recoveryPlanRef = db.collection("users").document(userId).collection("recoveryPlan");

        // Only plans marked as archived will appear
        Log.d(TAG, "Querying recoveryPlan where archived = true...");
        recoveryPlanRef.whereEqualTo("archived", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query returned " + queryDocumentSnapshots.size() + " documents");
                    archivedPlansList.clear();

                    // Loop through each returned document
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ArchivedPlan plan = doc.toObject(ArchivedPlan.class);
                        if (plan != null) {
                            plan.setPlanId(doc.getId());
                            archivedPlansList.add(plan);
                            Log.d(TAG, "Loaded archived plan doc ID: " + doc.getId()
                                    + ", injuryType=" + plan.getInjuryType()
                                    + ", injuryArea=" + plan.getInjuryArea());
                        }
                    }

                    // Create an adapter and attach it to the RecyclerView
                    adapter = new ArchivedPlansAdapter(archivedPlansList, plan -> {
                        Log.d(TAG, "Archived plan clicked: " + plan.getPlanId());

                        Toast.makeText(ArchivedPlansActivity.this,
                                "Restore plan: " + plan.getInjuryType(),
                                Toast.LENGTH_SHORT).show();
                    });
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading archived plans", e);
                    Toast.makeText(ArchivedPlansActivity.this, "Error loading archived plans",
                            Toast.LENGTH_SHORT).show();
                });
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
                .setPositiveButton("Yes", (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .show();
    }
}
