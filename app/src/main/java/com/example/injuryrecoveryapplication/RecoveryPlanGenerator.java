package com.example.injuryrecoveryapplication;

import android.util.Log;

import com.example.injuryrecoveryapplication.models.Exercise;
import com.example.injuryrecoveryapplication.utils.AngleReadyLibrary;
import com.example.injuryrecoveryapplication.utils.AngleReadyUtils;
import com.example.injuryrecoveryapplication.utils.InjurySpecialtyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecoveryPlanGenerator {

    public List<DailyPlan> generateWeeklyPlan(String injuryType, List<Exercise> allExercises) {
        // Log the total exercises received from the API (already filtered by category)
        Log.d("RecoveryPlanGenerator", "Total exercises received: " + allExercises.size());

        List<Exercise> angleReadyBaseline = new ArrayList<>();

        for (Exercise ex : allExercises) {
            if (AngleReadyUtils.isAngleReady(ex.getId())) {
                AngleReadyLibrary.VariantInfo vi = AngleReadyLibrary.MAP.get(ex.getId());
                if ("baseline".equals(vi.difficulty)) {
                    angleReadyBaseline.add(ex);
                }
            }
        }
        Log.d("RecoveryPlanGenerator",
                "Angle-ready baseline count = " + angleReadyBaseline.size());

        // If no angle ready exercises are found, revert to full list
        List<Exercise> filteredExercises;
        if (angleReadyBaseline.isEmpty()) {
            Log.d("RecoveryPlanGenerator",
                    "No angle-ready matches – falling back to generic plan.");
            filteredExercises = new ArrayList<>(allExercises);   // original behaviour
        } else {
            filteredExercises = angleReadyBaseline;              // knee demo set
        }
        Log.d("RecoveryPlanGenerator",
                "Using exercises count = " + filteredExercises.size());

        if (filteredExercises.isEmpty()) {
            Log.w("RecoveryPlanGenerator",
                    "No exercises available at all - no plan generation.");
            return Collections.emptyList();
        }

            // Generate a weekly plan for 7 days with alternating workout and rest days
        List<DailyPlan> weeklyPlan = new ArrayList<>();
        int numDays = 7;
        for (int day = 1; day <= numDays; day++) {
            String dayLabel = "Day " + day;
            if (day % 2 == 1) {
                List<Exercise> dailyExercises = new ArrayList<>();
                int index1 = (day - 1) % filteredExercises.size();
                int index2 = day % filteredExercises.size();
                dailyExercises.add(filteredExercises.get(index1));
                dailyExercises.add(filteredExercises.get(index2));
                weeklyPlan.add(new DailyPlan(dayLabel, dailyExercises, false));
            } else {
                weeklyPlan.add(new DailyPlan(dayLabel, new ArrayList<>(), false));
            }
        }
        return weeklyPlan;
    }

    public List<WeeklyRecoveryPlan> generateMultipleWeeklyPlans(String injuryType, List<Exercise> allExercises, int numberOfWeeks) {
        List<WeeklyRecoveryPlan> weeklyRecoveryPlans = new ArrayList<>();
        // Generate one weekly plan template
        List<DailyPlan> singleWeekPlan = generateWeeklyPlan(injuryType, allExercises);
        // For each week, create a deep copy of the weekly plan template
        for (int i = 0; i < numberOfWeeks; i++) {
            List<DailyPlan> weekCopy = new ArrayList<>();
            for (DailyPlan dailyPlan : singleWeekPlan) {
                // Create a new copy of the DailyPlan (new list for exercises and same completion status)
                List<Exercise> exercisesCopy = new ArrayList<>(dailyPlan.getExercises());
                weekCopy.add(new DailyPlan(dailyPlan.getDay(), exercisesCopy, dailyPlan.isCompleted()));
            }
            weeklyRecoveryPlans.add(new WeeklyRecoveryPlan(weekCopy));
        }
        return weeklyRecoveryPlans;
    }

}
