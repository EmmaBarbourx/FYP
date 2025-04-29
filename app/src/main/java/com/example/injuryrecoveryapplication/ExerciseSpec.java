package com.example.injuryrecoveryapplication;

import java.util.List;

public class ExerciseSpec {

    private List<String> anglesToTrack;
    private int repGoal;
    private float minAngle;
    private float maxAngle;

    // Hold-style exercises
    private boolean requiresHold;
    private int holdTimeSeconds;
    private final boolean countAtBottom;

    public ExerciseSpec(
            List<String> anglesToTrack,
            int repGoal,
            float minAngle,
            float maxAngle,
            boolean requiresHold,
            int holdTimeSeconds,
            boolean countAtBottom

    ) {
        this.anglesToTrack = anglesToTrack;
        this.repGoal = repGoal;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.requiresHold = requiresHold;
        this.holdTimeSeconds = holdTimeSeconds;
        this.countAtBottom = countAtBottom;
    }

    // Getters
    public List<String> getAnglesToTrack() {
        return anglesToTrack;
    }
    public int getRepGoal() {
        return repGoal;
    }
    public float getMinAngle() {
        return minAngle;
    }
    public float getMaxAngle() {
        return maxAngle;
    }
    public boolean isRequiresHold() {
        return requiresHold;
    }
    public int getHoldTimeSeconds() {
        return holdTimeSeconds;
    }
    public boolean isCountAtBottom()     { return countAtBottom; }
}
