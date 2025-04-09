package com.example.injuryrecoveryapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_plans);

        // Set up Toolbar with title
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Archived Plans");
        setSupportActionBar(toolbar);

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
}
