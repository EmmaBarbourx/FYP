package com.example.injuryrecoveryapplication;

import android.app.Application;

import com.example.injuryrecoveryapplication.utils.AngleReadyLibrary;
import com.example.injuryrecoveryapplication.utils.ExerciseMetaCache;

public class MyApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        // Pre-fetch the 15 angle-ready variants
        for (String id : AngleReadyLibrary.MAP.keySet()) {
            ExerciseMetaCache.get(id, meta -> {  });
        }
    }
}
