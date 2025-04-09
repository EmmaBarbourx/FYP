package com.example.injuryrecoveryapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    // Firebase authentication variable
    private FirebaseAuth auth;

    // UI variables
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private TextView forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();

        // Linking UI variables with layout
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Set up login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Set up register text click listener to navigate to RegisterActivity
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        // Set up forgot password click listener
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim(); // Retrieving the email entered by the user
                if (!email.isEmpty()) {
                    auth.sendPasswordResetEmail(email) // Sends user reset email
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) { // Checking if the password reset email was sent successfully
                                    Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Error in sending password reset email.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else { // If the email field is empty, asks user to enter the email
                    Toast.makeText(LoginActivity.this, "Please enter your email first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to check user with Firebase
    private void loginUser() { // Get user inputs for email and password
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) { // Checking if email or password fields are empty
            Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password) // Try to sign in with Firebase using email and password
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser(); // Get the current user

                        // Check if email is verified
                        if (user != null && user.isEmailVerified()) {
                            // If the email is verified, go to MainActivity
                            // Check if user has created a profile
                            checkUserProfile(user.getUid());
                        } else if (user != null && !user.isEmailVerified()) {
                            // If email is not verified, ask user to verify email
                            Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // If sign-in fails, display a message to the user
                        if (task.getException() != null && task.getException().getMessage().contains("password")) {
                            Toast.makeText(LoginActivity.this, "Incorrect email or password. Please try again.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // Method to check if a user profile exists in Firestore
    private void checkUserProfile(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Profile exists, navigate to DashboardActivity
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Profile does not exist, navigate to CreateProfileActivity
                        Intent intent = new Intent(LoginActivity.this, CreateProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error checking user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}