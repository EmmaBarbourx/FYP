package com.example.injuryrecoveryapplication;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.injuryrecoveryapplication.DailyPlan;
import com.example.injuryrecoveryapplication.models.Exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RecoveryPlanAdapter extends RecyclerView.Adapter<RecoveryPlanAdapter.RecoveryPlanViewHolder> {

    private final List<DailyPlan> dailyPlans;
    private final BiConsumer<String, Boolean> onCompletionChange;
    private int currentWeekNumber = 1;
    private int overallCompletedDays = 0;
    private int previousWeeksCompletedDays = 0;


    public RecoveryPlanAdapter(List<DailyPlan> dailyPlans, BiConsumer<String, Boolean> onCompletionChange) {
        this.dailyPlans = dailyPlans;
        this.onCompletionChange = onCompletionChange;
    }

    public void setCurrentWeekNumber(int weekNumber) {
        this.currentWeekNumber = weekNumber;
    }

    public void setOverallCompletedDays(int completedDays) {
        this.overallCompletedDays = completedDays;
    }

    public void setPreviousWeeksCompletedDays(int days) {
        this.previousWeeksCompletedDays = days;
    }

    @NonNull
    @Override
    public RecoveryPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_plan, parent, false);
        return new RecoveryPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecoveryPlanViewHolder holder, int position) {
        // Get the daily plan for the current position
        DailyPlan dailyPlan = dailyPlans.get(position);
        holder.dayTextView.setText(dailyPlan.getDay());

        // Build a string of exercise names for the day
        StringBuilder exerciseNames = new StringBuilder();
        List<Exercise> exerciseList = dailyPlan.getExercises();
        if (exerciseList != null && !exerciseList.isEmpty()) {
            for (Exercise exercise : exerciseList) {
                exerciseNames.append(exercise.getName()).append(", ");
            }
            // Remove the trailing comma and space
            if (exerciseNames.length() > 2) {
                exerciseNames.setLength(exerciseNames.length() - 2);
            }
        } else {
            exerciseNames.append("Rest Day");
        }
        holder.exercisesTextView.setText(exerciseNames.toString());

        // Set the button text based on whether the day is completed
        holder.completeButton.setText(dailyPlan.isCompleted() ? "Completed" : "Mark as Done");

        // Use the adapter's currentWeekNumber (default to 1 if not set)
        int week = (currentWeekNumber > 0) ? currentWeekNumber : 1;
        // Compute the overall day number ( 7 days per week)
        int overallDay = (week - 1) * 7 + (position + 1);
        // Calculate the total number of completed days across all weeks
        int totalCompletedAcrossWeeks = previousWeeksCompletedDays + overallCompletedDays;

        // Handle button click to toggle completion status
        holder.completeButton.setOnClickListener(v -> {
            boolean newCompletedState = !dailyPlan.isCompleted();

            // If it's a week past week 1, ensure all days in previous weeks are completed
            if (newCompletedState && currentWeekNumber > 1 &&
                    previousWeeksCompletedDays < (currentWeekNumber - 1) * 7) {
                Toast.makeText(v.getContext(), "Please complete all days of the previous week first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enforce sequential completion: the day being marked complete must be exactly the next day
            if (newCompletedState && overallDay != totalCompletedAcrossWeeks + 1) {
                Toast.makeText(v.getContext(), "Please complete the previous day(s) first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Allow unchecking only if this day is the last completed day
            if (!newCompletedState && overallDay != previousWeeksCompletedDays + overallCompletedDays) {
                Toast.makeText(v.getContext(), "You can only uncheck the last completed day.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the daily plan's completion status and notify the listener to update Firestore
            dailyPlan.setCompleted(newCompletedState);
            onCompletionChange.accept(dailyPlan.getDay(), newCompletedState);

            // Update the overall progress tracker for the current week
            if (newCompletedState) {
                overallCompletedDays = overallDay - previousWeeksCompletedDays;
            } else {
                overallCompletedDays = overallDay - previousWeeksCompletedDays - 1;
            }
            // Refresh the adapter to reflect changes
            notifyDataSetChanged();
        });

        // Set a click listener on the item view to show exercise details
        holder.itemView.setOnClickListener(v -> {
            if (exerciseList != null && !exerciseList.isEmpty()) {
                ArrayList<Exercise> exercisesToShow = new ArrayList<>(exerciseList);
                androidx.fragment.app.FragmentActivity activity =
                        (androidx.fragment.app.FragmentActivity) v.getContext();
                ExerciseDetailsDialogFragment dialogFragment =
                        ExerciseDetailsDialogFragment.newInstance(exercisesToShow);
                dialogFragment.show(activity.getSupportFragmentManager(), "ExerciseDetailsDialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return dailyPlans.size();
    }

    static class RecoveryPlanViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView, exercisesTextView;
        Button completeButton;

        public RecoveryPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.textViewDay);
            exercisesTextView = itemView.findViewById(R.id.textViewExercises);
            completeButton = itemView.findViewById(R.id.buttonComplete);
        }
    }
}
