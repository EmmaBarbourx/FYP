package com.example.injuryrecoveryapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.injuryrecoveryapplication.models.Exercise;

import java.util.List;

public class TestExerciseDbActivity extends AppCompatActivity {

    private static final String TAG = "TestExerciseDbActivity";
    private TextView textViewResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_exercise_db);

        textViewResults = findViewById(R.id.textViewResults);

        // Make a simple test call to the new ExerciseDbApiService
        ExerciseDbApiService apiService = new ExerciseDbApiService();

        // For testing, pick any valid ExerciseDB bodyPart
        String testBodyPart = "shoulders";

        // Perform the fetch
        apiService.fetchExercisesByBodyPartAll(testBodyPart, new ExerciseDbApiService.ExerciseDbCallback() {
            @Override
            public void onSuccess(List<Exercise> exercises) {
                // Success! Log and display the first few exercises
                Log.d(TAG, "Fetched " + exercises.size() + " exercises for bodyPart=" + testBodyPart);

                // Update the UI on the main thread
                runOnUiThread(() -> {
                    if (!exercises.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Total fetched: ").append(exercises.size()).append("\n\n");
                        // Print the names of the first 5 exercises (or all if less than 5)
                        for (int i = 0; i < Math.min(exercises.size(), 5); i++) {
                            Exercise ex = exercises.get(i);
                            sb.append(i + 1).append(". ").append(ex.getName())
                                    .append(" (Target: ").append(ex.getTarget()).append(")\n");
                        }
                        textViewResults.setText(sb.toString());
                    } else {
                        textViewResults.setText("No exercises returned!");
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                // Log error and show in textView
                Log.e(TAG, "API call failed: " + errorMessage);
                runOnUiThread(() -> textViewResults.setText("API call failed:\n" + errorMessage));
            }
        });
    }
}
