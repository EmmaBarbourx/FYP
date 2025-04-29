package com.example.injuryrecoveryapplication.utils;

import com.example.injuryrecoveryapplication.utils.AngleReadyLibrary;

import java.util.Map;


public class AngleReadyUtils {

    public static boolean isAngleReady(String exerciseId) {
        return AngleReadyLibrary.MAP.containsKey(exerciseId);
    }

    // Returns the exercise-ID that matches the requested family and difficulty
    public static String getIdFor(String family, String difficulty) {
        for (Map.Entry<String, AngleReadyLibrary.VariantInfo> entry
                : AngleReadyLibrary.MAP.entrySet()) {

            AngleReadyLibrary.VariantInfo vi = entry.getValue();
            if (difficulty.equals(vi.difficulty) && family.equals(vi.family)) {
                return entry.getKey();   // exerciseId
            }
        }
        return null;    // no match
    }

    public static String getBaselineId(String family) {
        for (java.util.Map.Entry<String, AngleReadyLibrary.VariantInfo> entry : AngleReadyLibrary.MAP.entrySet()) {
            AngleReadyLibrary.VariantInfo vi = entry.getValue();
            if ("baseline".equals(vi.difficulty) && family.equals(vi.family)) {
                return entry.getKey();          //  exerciseId
            }
        }
        return null;
    }

    private AngleReadyUtils() {}
}
