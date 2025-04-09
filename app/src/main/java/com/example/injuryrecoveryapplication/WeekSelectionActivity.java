package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class WeekSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WeeklyPlanAdapter weeklyPlanAdapter;
    private MaterialButton archivedPlansButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_selection);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewWeeks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create a list of week numbers (weeks 1 to 6)
        List<Integer> weeks = Arrays.asList(1, 2, 3, 4, 5, 6);

        weeklyPlanAdapter = new WeeklyPlanAdapter(weeks, weekNumber -> {
            // When a week is clicked, launch RecoveryPlanActivity with the week number
            Intent intent = new Intent(WeekSelectionActivity.this, RecoveryPlanActivity.class);
            intent.putExtra("weekNumber", weekNumber);
            startActivity(intent);
        });

        recyclerView.setAdapter(weeklyPlanAdapter);

        // Set up the Archived Plans button and its click listener
        archivedPlansButton = findViewById(R.id.archivedPlansButton);
        archivedPlansButton.setOnClickListener(v -> {
            // Launch the new ArchivedPlansActivity
            Intent intent = new Intent(WeekSelectionActivity.this, ArchivedPlansActivity.class);
            startActivity(intent);
        });
    }


}
