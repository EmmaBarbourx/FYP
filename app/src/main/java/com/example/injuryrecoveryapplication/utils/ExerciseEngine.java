package com.example.injuryrecoveryapplication.utils;

import com.example.injuryrecoveryapplication.ExerciseSpec;


public class ExerciseEngine {

    private static final float KEEP_PADDING_DEG = 5f;
    private static final float MIN_BOTTOM_TIME  = 0.4f;
    private static final float PAD = 4f;


    public static final class Update {
        public final int repCount;
        public final String feedback;
        public Update(int repCount, String feedback) {
            this.repCount = repCount;
            this.feedback = feedback;
        }
    }

    // per-session state
    private final ExerciseSpec spec;
    private RepListener listener;
    private float timeInCorrectRange = 0f;
    private float timeInRangeThisRep = 0f;
    private float outOfRangeTimer    = 0f;
    private boolean waitingForDown   = false;
    private boolean inRangeState     = false;
    private int repCount             = 0;

    public void setListener(RepListener l) {
        this.listener = l;
    }

    // For progress bar
    public float getHoldProgress() {
        if (!spec.isRequiresHold()) return 0f;
        return Math.min(timeInCorrectRange / spec.getHoldTimeSeconds(), 1f);
    }

    public ExerciseEngine(ExerciseSpec spec) { this.spec = spec; }

    // runs once per camera frame
    public Update onFrame(float leftKneeAngle,
                          float rightKneeAngle,
                          boolean allAnglesInRange,
                          float deltaSeconds) {

        if (spec.isRequiresHold()) {
            return handleHold(leftKneeAngle, rightKneeAngle,
                    allAnglesInRange, deltaSeconds);
        } else {
            return handleRep(leftKneeAngle, rightKneeAngle,
                     deltaSeconds);
        }
    }

    // hold-based movements
    private Update handleHold(float left, float right,
                              boolean tightRange, float dt) {

        boolean looseRange =
                left  >= spec.getMinAngle() -KEEP_PADDING_DEG &&
                        left  <= spec.getMaxAngle() + KEEP_PADDING_DEG &&
                        right >= spec.getMinAngle() - KEEP_PADDING_DEG &&
                        right <= spec.getMaxAngle() + KEEP_PADDING_DEG;
        // wait until the user comes back to the start position
        if (waitingForDown) {
            if (!looseRange) {
                waitingForDown     = false;
                timeInCorrectRange = 0f;
            }
            return new Update(repCount, "Return to start position");
        }

        // timers with 0.25s jitter-grace
        if (looseRange) {
            timeInCorrectRange += dt;
            outOfRangeTimer     = 0f;
        } else {
            timeInCorrectRange = 0f;
            outOfRangeTimer   += dt;
            if (outOfRangeTimer < .25f) looseRange = true;
        }

        // completed a hold rep
        if (timeInCorrectRange >= spec.getHoldTimeSeconds() && tightRange) {
            // one rep (hold) finished
            repCount++;

            if (listener != null) {
                listener.onRepComplete(repCount);   // per-rep ping
                if (repCount >= spec.getRepGoal()) {
                    listener.onExerciseComplete();
                }
            }

            waitingForDown     = true;
            timeInCorrectRange = 0f;       // reset timer
            return new Update(repCount, "Great! Return to start position");
        }

        if (!looseRange) {
            return new Update(repCount, "Incorrect form!");
        } else {
            float leftSeconds = spec.getHoldTimeSeconds() - timeInCorrectRange;
            return new Update(repCount,
                    String.format("Holding: %.1f / %d s",
                            timeInCorrectRange, spec.getHoldTimeSeconds()));
        }
    }

    private boolean inDownPhase = false;   // true while the user is in the bottom position of a rep

    private Update handleRep(float left, float right, float dt) {

        final float bottom = spec.getMinAngle();
        final float top    = spec.getMaxAngle();
        final float PAD    = 4f;  // buffer

        // both knees are past the bottom threshold
        boolean belowBottom = left  < bottom - PAD &&
                right < bottom - PAD;

        // both knees are past the top threshold
        boolean aboveTop    = left  > top    - PAD &&
                right > top    - PAD;

        // user is still up, waiting to drop down
        if (!inDownPhase) {

            if (belowBottom) {   // just reached bottom
                inDownPhase = true;

                // some moves count the rep at the bottom instead of the top
                if (spec.isCountAtBottom()) {
                    repCount++;
                    if (listener != null) {
                        listener.onRepComplete(repCount);
                        if (repCount >= spec.getRepGoal())
                            listener.onExerciseComplete();
                    }
                    return new Update(repCount, "Good posture!");
                }

                // start a timer for moves that need a pause
                timeInRangeThisRep = 0f;
                return new Update(repCount, "Good depth – hold …");
            }

            return new Update(repCount, null);
        }

        // user is staying at the bottom
        if (belowBottom) {
            timeInRangeThisRep += dt;
        }

        // user stood back up
        if (aboveTop) {

            if (!spec.isCountAtBottom() &&
                    timeInRangeThisRep >= MIN_BOTTOM_TIME) {

                repCount++;
                if (listener != null) {
                    listener.onRepComplete(repCount);
                    if (repCount >= spec.getRepGoal())
                        listener.onExerciseComplete();
                }
                inDownPhase        = false;
                timeInRangeThisRep = 0f;
                return new Update(repCount, "Good posture!");
            }

            // either was already counted at bottom or the pause was too short
            String fb = spec.isCountAtBottom()
                    ? null
                    : "Pause a bit deeper!";
            inDownPhase        = false;
            timeInRangeThisRep = 0f;
            return new Update(repCount, fb);
        }

        return new Update(repCount, null);
    }
}
