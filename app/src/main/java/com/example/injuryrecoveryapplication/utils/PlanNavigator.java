package com.example.injuryrecoveryapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.injuryrecoveryapplication.RecoveryPlanActivity;
import com.example.injuryrecoveryapplication.DailyPlan;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.atomic.AtomicInteger;

public final class PlanNavigator {

    private static final String TAG = "PlanNavigator";

    public interface Callback {
        void onFound(int weekNo, @NonNull String dayId);
        void onNoWorkouts();                     // all done
    }

    public static void openNextWorkout(@NonNull Context ctx,
                                       @NonNull String  userId) {

        findFirstWorkout(userId, FirebaseFirestore.getInstance(),
                new Callback() {
                    @Override public void onFound(int weekNo, @NonNull String dayId) {
                        Intent i = new Intent(ctx, RecoveryPlanActivity.class)
                                .putExtra("weekNumber", weekNo)
                                .putExtra("openDayId", dayId);   // will trigger the dialog
                        ctx.startActivity(i);
                    }

                    @Override public void onNoWorkouts() {
                        Log.d(TAG, "Whole plan complete");
                    }
                });
    }


    private static void findFirstWorkout(String userId,
                                         FirebaseFirestore db,
                                         Callback cb) {

        db.collection("users").document(userId).get()
                .addOnSuccessListener(userSnap -> {
                    String injId = userSnap.getString("currentInjuryId");
                    if (injId == null) { cb.onNoWorkouts(); return; }

                    CollectionReference weeks = db.collection("users")
                            .document(userId)
                            .collection("recoveryPlan")
                            .document(injId)
                            .collection("weeks");

                    walkWeek(1, weeks, cb);
                });
    }

    private static void walkWeek(int weekNo,
                                 CollectionReference weeks,
                                 Callback cb) {

        if (weekNo > 6) { cb.onNoWorkouts(); return; }

        weeks.document("week"+weekNo).collection("days")
                .orderBy("day").get()
                .addOnSuccessListener(daysSnap ->
                        inspectDays(weekNo, daysSnap, weeks, cb));
    }

    private static void inspectDays(int weekNo,
                                    QuerySnapshot daysSnap,
                                    CollectionReference weeks,
                                    Callback cb) {

        final AtomicInteger pendingWrites = new AtomicInteger(0);

        for (DocumentSnapshot d : daysSnap.getDocuments()) {
            DailyPlan dp = d.toObject(DailyPlan.class);
            if (dp == null || dp.isCompleted()) continue;

            boolean restDay = dp.getExercises()==null
                    || dp.getExercises().isEmpty();

            if (!restDay) {
                cb.onFound(weekNo, dp.getDay());
                return;
            }

            // auto-complete rest day
            pendingWrites.incrementAndGet();
            d.getReference().update("completed", true)
                    .addOnSuccessListener(v -> {
                        if (pendingWrites.decrementAndGet()==0)
                            walkWeek(weekNo, weeks, cb);  // re-check same week
                    });
        }

        if (pendingWrites.get()==0)                 // all days finished , next week
            walkWeek(weekNo+1, weeks, cb);
    }

    private PlanNavigator() {}
}

