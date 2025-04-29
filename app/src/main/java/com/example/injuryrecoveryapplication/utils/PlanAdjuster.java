package com.example.injuryrecoveryapplication.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.injuryrecoveryapplication.DailyPlan;
import com.example.injuryrecoveryapplication.models.Exercise;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PlanAdjuster {

    private static final String TAG = "PlanAdjuster";

    // how many recent logs the average is based on
    private static final int  PAIN_SAMPLE_SIZE    = 3;
    private static final int  PAIN_THRESHOLD_HIGH = 7;
    private static final int  PAIN_THRESHOLD_LOW  = 3;

    // Left is easiest
    private static final List<String> ORDER =
            Arrays.asList("baseline", "easy", "hard");

    private PlanAdjuster() {}

    public static void maybeAdjust(@NonNull Context ctx,
                                   @NonNull String  userId,
                                   @NonNull String  injuryId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // get the last 3 pain logs for this injury (newest first)
        db.collection("users").document(userId)
                .collection("painLogs")
                .whereEqualTo("currentInjuryId", injuryId)
                .whereEqualTo("archived", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAIN_SAMPLE_SIZE)
                .get()
                .addOnSuccessListener(snaps -> {

                    if (snaps.size() < PAIN_SAMPLE_SIZE) return;   // not enough data

                    // average the pain scores
                    int sum = 0;
                    for (DocumentSnapshot d : snaps.getDocuments()) {
                        try { sum += Integer.parseInt(d.getString("painLevel")); }
                        catch (Exception ignored) { return; }
                    }
                    final int avgPain = Math.round(sum / (float) PAIN_SAMPLE_SIZE);

                    // decide which direction to go in
                    int direction = 0;
                    if (avgPain >= PAIN_THRESHOLD_HIGH) direction = -1; //easier
                    else if (avgPain <= PAIN_THRESHOLD_LOW) direction = +1; // harder
                    if (direction == 0) return;   // pain in the middle, no change

                    final int dir = direction;
                    new AlertDialog.Builder(ctx)
                            .setTitle("Update next workouts?")
                            .setMessage("Your average pain was " + avgPain + ".\n" +
                                    "Would you like me to " +
                                    (direction < 0 ? "make the upcoming exercises easier?"
                                            : "progress to tougher variants?"))
                            .setPositiveButton("Yes",
                                    (d,i) -> doSwap(db, userId, injuryId, dir))
                            .setNegativeButton("No", null)
                            .show();
                });
    }

    // swapping the one exercise
    private static void doSwap(FirebaseFirestore db,
                               String userId,
                               String injuryId,
                               int direction) {

        CollectionReference weeks = db.collection("users").document(userId)
                .collection("recoveryPlan")
                .document(injuryId)
                .collection("weeks");

        // get weeks and days until I find the first un-finished day
        List<Task<QuerySnapshot>> weekTasks = new ArrayList<>();
        for (int w = 1; w <= 6; w++) {
            weekTasks.add(weeks.document("week" + w)
                    .collection("days")
                    .orderBy("day")
                    .get());
        }

        Tasks.whenAllSuccess(weekTasks).addOnSuccessListener(all -> {

            // first incomplete day I find
            DocumentSnapshot firstPendingDay = null;

            outer:
            for (Object obj : all) {
                for (DocumentSnapshot d : ((QuerySnapshot)obj).getDocuments()) {
                    DailyPlan p = d.toObject(DailyPlan.class);
                    if (p != null && !p.isCompleted()) {
                        firstPendingDay = d;
                        break outer;
                    }
                }
            }
            if (firstPendingDay == null) return;          // whole plan done

            DailyPlan dp = firstPendingDay.toObject(DailyPlan.class);
            if (dp == null) return;

            final DocumentSnapshot daySnapFinal = firstPendingDay;
            final DailyPlan        dpFinal      = dp;


            AtomicInteger pending   = new AtomicInteger(0);   // meta downloads
            AtomicBoolean anyChange = new AtomicBoolean(false);

            for (Exercise ex : dp.getExercises()) {

                Log.d(TAG, "→ check " + ex.getName()
                        + "  id=" + ex.getId()
                        + "  fam=" + ex.getFamily()
                        + "  diff=" + ex.getDifficulty());

                if (!AngleReadyUtils.isAngleReady(ex.getId())) {
                    Log.d(TAG, "  skipped (not angle-ready)");
                    continue;
                }

                String nextDiff = shifted(ex.getDifficulty(), direction);
                if (nextDiff.equals(ex.getDifficulty())) {
                    Log.d(TAG, "    same difficulty (" + nextDiff + ")");
                    continue;
                }

                String newId = AngleReadyUtils.getIdFor(ex.getFamily(), nextDiff);
                if (newId == null) {
                    Log.d(TAG, "    no variant for " + nextDiff);
                    continue;
                }
                Log.d(TAG, "   swapping to " + newId);


                pending.incrementAndGet();
                ExerciseMetaCache.get(newId, meta -> {

                    if (meta != null) {
                        ex.setName(meta.getName());
                        ex.setInstructions(meta.getInstructions());
                        ex.setGifUrl(meta.getGifUrl());
                    } else {
                        ex.setName("?");
                        ex.setInstructions("meta missing");
                    }

                    // patch id + difficulty
                    ex.setId(newId);
                    ex.setDifficulty(nextDiff);

                    anyChange.set(true);

                    if (pending.decrementAndGet() == 0) {
                        pushIfNeeded(daySnapFinal, dpFinal, anyChange.get());
                    }
                });
            }


            if (pending.get() == 0) {
                pushIfNeeded(daySnapFinal, dpFinal, anyChange.get());
            }
        });
    }

    private static void pushIfNeeded(DocumentSnapshot snap,
                                     DailyPlan        dp,
                                     boolean          changed) {

        if (!changed) {
            Log.d(TAG, "No exercise could be swapped — plan left untouched");
            return;
        }
        snap.getReference().set(dp)
                .addOnSuccessListener(v ->
                        Log.d(TAG, "DailyPlan updated successfully"));
    }

    // shift difficulty up / down
    private static String shifted(String current, int delta) {
        int idx = ORDER.indexOf(current == null ? "baseline" : current);
        if (idx < 0) idx = 0;
        int n = Math.max(0, Math.min(ORDER.size() - 1, idx + delta));
        return ORDER.get(n);
    }
}

