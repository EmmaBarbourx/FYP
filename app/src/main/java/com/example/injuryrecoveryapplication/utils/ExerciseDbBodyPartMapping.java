package com.example.injuryrecoveryapplication.utils;

import java.util.HashMap;
import java.util.Map;

public class ExerciseDbBodyPartMapping {


    private static final Map<String, String> INJURY_TO_BODYPART_MAP = new HashMap<>();

    static {
        // Shoulder injuries -> "shoulders"
        INJURY_TO_BODYPART_MAP.put("Shoulder", "shoulders");

        // Elbow injuries -> "upper arms"
        INJURY_TO_BODYPART_MAP.put("Elbow", "upper arms");

        // Wrist injuries -> "lower arms"
        INJURY_TO_BODYPART_MAP.put("Wrist", "lower arms");

        // Lower back -> "back"
        INJURY_TO_BODYPART_MAP.put("Lower Back", "back");

        // Neck -> "neck"
        INJURY_TO_BODYPART_MAP.put("Neck", "neck");

        // Knee -> "upper legs"
        INJURY_TO_BODYPART_MAP.put("Knee", "upper legs");

        // Ankle -> "lower legs"
        INJURY_TO_BODYPART_MAP.put("Ankle", "lower legs");

        // Foot -> "lower legs" as well
        INJURY_TO_BODYPART_MAP.put("Foot", "lower legs");

        // Hip -> "upper legs"
        INJURY_TO_BODYPART_MAP.put("Hip", "upper legs");

        // General Muscle -> "waist"
        INJURY_TO_BODYPART_MAP.put("General Muscle", "waist");
    }

    public static String getBodyPartForInjury(String injuryArea) {
        if (injuryArea == null) return "back"; // default fallback
        // Use the map if available, else default to "back" or something generic
        return INJURY_TO_BODYPART_MAP.getOrDefault(injuryArea, "back");
    }
}
