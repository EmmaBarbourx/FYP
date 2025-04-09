package com.example.injuryrecoveryapplication.utils;

import com.example.injuryrecoveryapplication.ExerciseSpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExerciseSpecLibrary {

    private static final Map<String, ExerciseSpec> SPECS = new HashMap<>();

    static {
        // 0669 - "rear deltoid stretch"
        SPECS.put("0669", new ExerciseSpec(
                Arrays.asList("leftShoulderAngle", "rightShoulderAngle"),
                0,
                60f,
                110f,
                true,
                15           
        ));


    }

    public static ExerciseSpec getSpec(String exerciseId) {
        return SPECS.get(exerciseId);
    }
}

