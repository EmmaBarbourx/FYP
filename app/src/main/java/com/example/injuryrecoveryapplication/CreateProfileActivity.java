package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;


public class CreateProfileActivity extends AppCompatActivity {

    // UI components
    private EditText ageEditText;
    private RadioGroup genderRadioGroup;
    private Spinner fitnessLevelSpinner;
    private Spinner injuryAreaSpinner;
    private Spinner injuryTypeSpinner;
    private Button submitButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;


    // HashMap to store injury areas and their injury types
    private HashMap<String, List<String>> injuryTypeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        // Linking UI variables with layout
        ageEditText = findViewById(R.id.ageEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        fitnessLevelSpinner = findViewById(R.id.fitnessLevelSpinner);
        injuryAreaSpinner = findViewById(R.id.injuryAreaSpinner);
        injuryTypeSpinner = findViewById(R.id.injuryTypeSpinner);
        submitButton = findViewById(R.id.submitButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        // Populate the HashMap with injury areas and types
        injuryTypeMap = new HashMap<>();
        injuryTypeMap.put("Shoulder", List.of("Dislocation", "Rotator Cuff Tear", "Tendinitis"));
        injuryTypeMap.put("Elbow", List.of("Tennis Elbow", "Golfer's Elbow", "Bursitis"));
        injuryTypeMap.put("Wrist", List.of("Carpal Tunnel Syndrome", "Sprain", "Tendonitis"));
        injuryTypeMap.put("Lower Back", List.of("Herniated Disc", "Muscle Strain", "Sciatica"));
        injuryTypeMap.put("Neck", List.of("Whiplash", "Cervical Disc Injury", "Strain"));
        injuryTypeMap.put("Knee", List.of("ACL Tear", "Meniscus Tear", "Patellar Tendinitis"));
        injuryTypeMap.put("Ankle", List.of("Sprain", "Achilles Tendinitis", "Fracture"));
        injuryTypeMap.put("Foot", List.of("Plantar Fasciitis", "Fracture", "Sprain"));
        injuryTypeMap.put("Hip", List.of("Hip Flexor Strain", "Labral Tear", "Arthritis"));
        injuryTypeMap.put("General Muscle", List.of("Strain", "Tear", "Cramps"));

        // Set up a listener for the injury area spinner
        injuryAreaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArea = injuryAreaSpinner.getSelectedItem().toString();
                if (injuryTypeMap.containsKey(selectedArea)) {
                    List<String> injuryTypes = injuryTypeMap.get(selectedArea);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateProfileActivity.this,
                            android.R.layout.simple_spinner_item, injuryTypes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    injuryTypeSpinner.setAdapter(adapter);
                } else {
                    injuryTypeSpinner.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing for now
            }
        });

        // Set up submit button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    // Collect user inputs
                    String age = ageEditText.getText().toString().trim();
                    int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                    String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString();
                    String fitnessLevel = fitnessLevelSpinner.getSelectedItem().toString();
                    String injuryArea = injuryAreaSpinner.getSelectedItem().toString();
                    String injuryType = injuryTypeSpinner.getSelectedItem().toString();

                    // Get current user ID
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();

                        // Create a UserProfile object
                        UserProfile profile = new UserProfile(age, gender, fitnessLevel, injuryArea, injuryType);

                        // Save the UserProfile to Firestore
                        db.collection("users").document(userId).set(profile)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CreateProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                                    // Navigate to DashboardActivity
                                    Intent intent = new Intent(CreateProfileActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CreateProfileActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(CreateProfileActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


        // Validation for input fields
    private boolean validateInputs() {
        String age = ageEditText.getText().toString().trim();
        if (age.isEmpty()) {
            ageEditText.setError("Please enter your age.");
            ageEditText.requestFocus();
            return false;
        }
        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fitnessLevelSpinner.getSelectedItem().toString().equals("Select fitness level")) {
            Toast.makeText(this, "Please select a fitness level.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (injuryTypeSpinner.getSelectedItem().toString().equals("Select injury type")) {
            Toast.makeText(this, "Please select an injury type.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}

