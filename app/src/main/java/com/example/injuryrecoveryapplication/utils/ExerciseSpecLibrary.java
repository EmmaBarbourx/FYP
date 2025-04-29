package com.example.injuryrecoveryapplication.utils;

import com.example.injuryrecoveryapplication.ExerciseSpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExerciseSpecLibrary {

    private static final Map<String, ExerciseSpec> SPECS = new HashMap<>();

    static {
        // 0669 - rear deltoid stretch
        SPECS.put("0669", new ExerciseSpec(
                Arrays.asList("leftShoulderAngle", "rightShoulderAngle"),
                0,
                60f,
                110f,
                true,
                15 ,
                true
        ));

        // 1685 – squat-to-overhead-reach
        SPECS.put("1685", new ExerciseSpec(
                Arrays.asList("leftKneeAngle", "rightKneeAngle"),
                8,
                110f,
                175f,
                false,
                0   ,
                true
        ));

        // 3470 – forward lunge
        SPECS.put("3470", new ExerciseSpec(
                Arrays.asList("leftKneeAngle", "rightKneeAngle"),
                10,
                90f,
                120f,
                false,
                0   ,
                true
        ));

        // 3013 – low glute bridge (floor)
        SPECS.put("3013", new ExerciseSpec(
                Arrays.asList("leftHipAngle", "rightHipAngle"),
                8,
                168f,
                185f,
                true,
                3 ,
                false
        ));


    }

    public static ExerciseSpec getSpec(String exerciseId) {
        return SPECS.get(exerciseId);
    }
}

