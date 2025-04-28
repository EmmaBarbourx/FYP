package com.example.injuryrecoveryapplication.utils;

import com.example.injuryrecoveryapplication.utils.AngleReadyLibrary;


public class AngleReadyUtils {

    public static boolean isAngleReady(String exerciseId) {
        return AngleReadyLibrary.MAP.containsKey(exerciseId);
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
