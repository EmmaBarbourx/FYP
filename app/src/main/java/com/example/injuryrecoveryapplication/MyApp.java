package com.example.injuryrecoveryapplication;

import android.app.Application;
import android.content.Intent;

import com.example.injuryrecoveryapplication.utils.AngleReadyLibrary;
import com.example.injuryrecoveryapplication.utils.ExerciseMetaCache;
import com.google.firebase.auth.FirebaseAuth;

public class MyApp extends Application {

    private final FirebaseAuth.AuthStateListener authWatcher =
            firebaseAuth -> {
                if (firebaseAuth.getCurrentUser() == null) {
                    // token expired or user signed-out elsewhere
                    Intent i = new Intent(this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            };

    @Override public void onCreate() {
        super.onCreate();

        // Pre-fetch the 15 angle-ready variants
        for (String id : AngleReadyLibrary.MAP.keySet()) {
            ExerciseMetaCache.get(id, meta -> {  });
        }
        FirebaseAuth.getInstance().addAuthStateListener(authWatcher);
    }
}
