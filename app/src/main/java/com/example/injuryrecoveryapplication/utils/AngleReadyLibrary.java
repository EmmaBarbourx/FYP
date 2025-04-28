package com.example.injuryrecoveryapplication.utils;

import java.util.HashMap;
import java.util.Map;

public class AngleReadyLibrary {

    public static class VariantInfo {
        public String family;      // “squat”
        public String difficulty;  // “easy”, “baseline” , “hard”
        public String[] angles;

        public VariantInfo(String family, String difficulty, String... angles) {
            this.family = family;
            this.difficulty = difficulty;
            this.angles = angles;
        }
    }

    public static final Map<String, VariantInfo> MAP = new HashMap<>();

    static {
        // knee-friendly body-weight set
        MAP.put("3132", new VariantInfo("squat",      "easy",     "leftKneeAngle", "rightKneeAngle"));  // potty-squat with support
        MAP.put("1685", new VariantInfo("squat",      "baseline", "leftKneeAngle", "rightKneeAngle"));  // squat-to-overhead-reach
        MAP.put("1476", new VariantInfo("squat",      "hard",     "leftKneeAngle", "rightKneeAngle"));  // one-leg (pistol-prep)

        MAP.put("2368", new VariantInfo("lunge",      "easy",     "leftKneeAngle", "rightKneeAngle"));  // split-squat
        MAP.put("3470", new VariantInfo("lunge",      "baseline", "leftKneeAngle", "rightKneeAngle")); //  lunge
        MAP.put("3582", new VariantInfo("lunge",      "hard",     "leftKneeAngle", "rightKneeAngle"));  // lunge-with-jump

        MAP.put("1408", new VariantInfo("bridge",     "easy",     "leftKneeAngle", "rightKneeAngle"));  // hip-lift
        MAP.put("3013", new VariantInfo("bridge",     "baseline", "leftKneeAngle", "rightKneeAngle"));  // low glute bridge
        MAP.put("3561", new VariantInfo("bridge",     "hard",     "leftKneeAngle", "rightKneeAngle"));  // glute-bridge march

        MAP.put("3433", new VariantInfo("hinge",      "easy",     "leftKneeAngle", "rightKneeAngle"));  // swimmer kicks


        MAP.put("0710", new VariantInfo("hipMob",     "easy",     "leftKneeAngle", "rightKneeAngle"));  // side-lying hip abduction
        MAP.put("1559", new VariantInfo("hipMob",     "baseline", "leftKneeAngle", "rightKneeAngle"));  // exercise-ball hip-flexor stretch


        MAP.put("1511", new VariantInfo("stretch",    "easy",     "leftKneeAngle", "rightKneeAngle"));  // hamstring stretch
        MAP.put("1599", new VariantInfo("stretch",    "baseline", "leftKneeAngle", "rightKneeAngle"));  // standing ham-&-calf strap stretch
        MAP.put("1604", new VariantInfo("stretch",    "hard",     "leftKneeAngle", "rightKneeAngle"));  // world-greatest stretch

        android.util.Log.d("AngleReadyLibrary", "Loaded "
                + MAP.size() + " knee-ready exercises");
    }

    public static VariantInfo get(String exerciseId) {
        return MAP.get(exerciseId);
    }

    public static boolean contains(String exerciseId) {
        return MAP.containsKey(exerciseId);
    }
}
