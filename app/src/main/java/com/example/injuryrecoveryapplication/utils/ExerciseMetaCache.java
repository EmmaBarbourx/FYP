package com.example.injuryrecoveryapplication.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.injuryrecoveryapplication.ExerciseDbApiService;
import com.example.injuryrecoveryapplication.models.Exercise;

import java.util.HashMap;
import java.util.Map;

public class ExerciseMetaCache {

    private static final Map<String, Exercise> CACHE = new HashMap<>();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ExerciseDbApiService api = new ExerciseDbApiService();
    private static final String TAG = "ExerciseMetaCache";

    public interface Callback { void onLoaded(Exercise meta); }

    //Grab meta from RAM if have it, if not hit ExerciseDB once
    public static void get(String id, Callback cb) {
        Exercise cached = CACHE.get(id);
        if (cached != null) { // already in memory
            cb.onLoaded(cached);
            return;
        }

        // not cached, fetch and remember
        api.fetchExerciseById(id, new ExerciseDbApiService.OneExerciseCallback() {
            @Override public void onSuccess(Exercise exercise) {
                CACHE.put(id, exercise); // data for next time
                MAIN.post(() -> cb.onLoaded(exercise));
                Log.d(TAG, "cached " + id);
            }
            @Override public void onFailure(String error) {
                Log.w(TAG, "fetch " + id + " failed: " + error);
                MAIN.post(() -> cb.onLoaded(null));
            }
        });
    }

    public static void addAll(Iterable<Exercise> list) {
        for (Exercise e : list) CACHE.put(e.getId(), e);
    }
}
