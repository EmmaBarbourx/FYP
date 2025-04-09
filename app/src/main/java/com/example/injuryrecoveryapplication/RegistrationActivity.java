package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

public class RegistrationActivity extends AppCompatActivity {

    // Firebase authentication variable
    private FirebaseAuth auth;
    //ListenerRegistration verificationListener;

    // UI variables
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button createAccountButton;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();

        // Linking UI variables with layout
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        createAccountButton = findViewById(R.id.createAccountButton);
        loginLink = findViewById(R.id.loginLink);

        // Set up create account button click listener
        createAccountButton.setOnClickListener(v -> { // Get the email, password, and confirm password values
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (validateEmail(email) & validatePassword(password) & confirmPassword(password, confirmPassword)) { // Validate email, password, and confirm password
                createAccount(email, password); // To register the user
            }
        });

        // Login Link Click Listener
        loginLink.setOnClickListener(v -> finish());

        // Add listener for email verification status
       // checkEmailVerificationStatus();
    }

    // Validate email format
    private boolean validateEmail(String email) {
        if (email.isEmpty() || !email.contains("@")) { // Checking if email is empty or does not contain '@'
            emailInputLayout.setError("Please enter a valid email.");
            return false;
        } else {
            emailInputLayout.setError(null);
            return true;
        }
    }

    // Validate password format
    private boolean validatePassword(String password) {
        if (password.isEmpty()) { // Checking if password is empty
            passwordInputLayout.setError("Password cannot be empty.");
            return false;
        }
        if (password.length() < 6) { // Checking if password is at least 6 characters
            passwordInputLayout.setError("Password must be at least 6 characters.");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) { // Checking if password contains at least one uppercase letter
            passwordInputLayout.setError("Password must contain at least one uppercase letter.");
            return false;
        }
        if (!password.matches(".*\\d.*")) { // Checking if password contains at least one number
            passwordInputLayout.setError("Password must contain at least one number.");
            return false;
        }

        // Clear error if meets requirements
        passwordInputLayout.setError(null);
        return true;
    }

    // Confirm passwords match
    private boolean confirmPassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) { // Check if password matches the confirmed password
            confirmPasswordInputLayout.setError("Passwords do not match.");
            return false;
        } else {
            confirmPasswordInputLayout.setError(null);
            return true;
        }
    }


    // Create account with Firebase Authentication
    private void createAccount(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password) // Create a new user account with the provided email and password
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser(); // If registration is successful, get the current user
                        if (user != null) {
                            user.sendEmailVerification() // Send a verification email to the new registered user
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {  // Ask the user to check their inbox for verification
                                            Toast.makeText(RegistrationActivity.this,
                                                    "Verification email sent. Please check your inbox.",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(RegistrationActivity.this,
                                                    "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else { // If registration fails, display an error message with the reason
                        Toast.makeText(RegistrationActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

   /* private void checkEmailVerificationStatus() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDoc = db.collection("users").document(userId);

            verificationListener = userDoc.addSnapshotListener(MetadataChanges.INCLUDE, (snapshot, e) -> {
                if (e != null) {
                    Toast.makeText(RegistrationActivity.this, "Error listening for verification changes.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists() && Boolean.TRUE.equals(snapshot.getBoolean("isVerified"))) {
                    Toast.makeText(RegistrationActivity.this, "Email verified. Redirecting to Create Profile...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this, CreateProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (verificationListener != null) {
            verificationListener.remove();
        }
    }
} */
