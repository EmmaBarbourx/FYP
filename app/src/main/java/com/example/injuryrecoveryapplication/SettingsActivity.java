package com.example.injuryrecoveryapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the title for the Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        // Load the preference fragment once
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::handleNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings); // Highlight Settings as active
    }

    // Holds the two SwitchPreferences
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.prefs, rootKey);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Preference emailPref = findPreference("pref_email");
                if (emailPref != null) {
                    emailPref.setSummary(user.getEmail());
                }
            }

            // Reset password action
            Preference resetPw = findPreference("pref_reset_pw");
            if (resetPw != null) {
                resetPw.setOnPreferenceClickListener(p -> {
                    if (user == null) return false;

                    // disable for 5 s to avoid double-taps
                    resetPw.setEnabled(false);

                    FirebaseAuth.getInstance()
                            .sendPasswordResetEmail(user.getEmail())
                            .addOnCompleteListener(task -> {
                                String msg = task.isSuccessful()
                                        ? "Password-reset e-mail sent."
                                        : "Failed to send reset e-mail.";

                                // nice, in-app confirmation
                                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();

                                // re-enable after a short pause
                                new Handler(Looper.getMainLooper())
                                        .postDelayed(() -> resetPw.setEnabled(true), 5000);
                            });
                    return true;
                });
            }
        }
    }


    private boolean handleNavigation(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_dashboard) {
            navigateTo(DashboardActivity.class);
        } else if (itemId == R.id.nav_profile) {
            navigateTo(ProfileActivity.class);
        } else if (itemId == R.id.nav_settings) {
            // Already on the Settings page
            return true;
        } else if (itemId == R.id.nav_logout) {
            navigateTo(LoginActivity.class);
        }
        return false;
    }


    private void navigateTo(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
        finish();
    }
}
