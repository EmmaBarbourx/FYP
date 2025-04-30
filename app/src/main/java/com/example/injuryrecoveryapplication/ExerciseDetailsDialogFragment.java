package com.example.injuryrecoveryapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.injuryrecoveryapplication.models.Exercise;

import java.util.ArrayList;

public class ExerciseDetailsDialogFragment extends DialogFragment {

    private static final String ARG_EXERCISES = "exercises"; // Key to pass exercises
    private static final int REQ_CAMERA = 1001;
    private static final String ARG_DAY_ID    = "dayId";

    private ArrayList<Exercise> exercises;

    // Create a new dialog instance with a list of exercises
    public static ExerciseDetailsDialogFragment newInstance(ArrayList<Exercise> exercises, String dayId) {
        ExerciseDetailsDialogFragment fragment = new ExerciseDetailsDialogFragment();
        Bundle args = new Bundle();
        // Put the list as a parsable array
        args.putParcelableArrayList(ARG_EXERCISES, exercises);
        args.putString(ARG_DAY_ID, dayId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        Log.d("ExerciseDetailsDialog", "onActivityResult triggered. req=" + req + " res=" + res);

        if (req == REQ_CAMERA && res == Activity.RESULT_OK && data != null) {

            String exId  = data.getStringExtra("exerciseId");       // exercise user just did
            String dayId = getArguments().getString(ARG_DAY_ID);    // day that owns this dialog

            Log.d("ExerciseDetailsDialog", "Got dayId=" + dayId + " exId=" + exId);
            if (exId != null && dayId != null) {
                // hand the pair back to the activity
                ((RecoveryPlanActivity) requireActivity())
                        .notifyExerciseDone(dayId, exId);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Retrieve the list of exercises passed to the dialog
        exercises = getArguments().getParcelableArrayList(ARG_EXERCISES);

        // Inflate the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_exercise_details, null);


        LinearLayout exercisesContainer = view.findViewById(R.id.exercisesContainer);

        // Check if there are any exercises to show
        if (exercises != null && !exercises.isEmpty()) {
            for (Exercise exercise : exercises) {

                // LOG the exercise info in Logcat
                Log.d("ExerciseDetailsDialog",
                        "Exercise ID: " + exercise.getId() +
                                ", Name: " + exercise.getName() +
                                ", Instructions: " + exercise.getInstructions()
                );

                // Creates a TextView for the exercise name
                TextView nameTextView = new TextView(requireContext());
                String nameHtml = "<b>Name:</b> " + exercise.getName();
                nameTextView.setText(android.text.Html.fromHtml(nameHtml, android.text.Html.FROM_HTML_MODE_LEGACY));
                nameTextView.setTextSize(16f);
                nameTextView.setPadding(0, 0, 0, 8); // small bottom margin

                exercisesContainer.addView(nameTextView);

                // Create a TextView for the Instructions
                TextView instructionsTextView = new TextView(requireContext());

               // Grab the raw instructions
                String rawInstructions = exercise.getInstructions();

              // Replace each dash with a newline + dash
                String replacedInstructions = rawInstructions.replace("-", "<br/>-");

              // Build the final HTML (bold label + replaced instructions)
                String instructionsHtml = "<b>Instructions:</b> " + replacedInstructions;

                // Set the text
                instructionsTextView.setText(
                        android.text.Html.fromHtml(instructionsHtml, android.text.Html.FROM_HTML_MODE_LEGACY)
                );
                instructionsTextView.setTextSize(14f);

                // Add this TextView to the container
                exercisesContainer.addView(instructionsTextView);

                // If there's a gifUrl, create an ImageView and load it
                if (exercise.getGifUrl() != null && !exercise.getGifUrl().isEmpty()) {
                    ImageView gifImageView = new ImageView(requireContext());
                    gifImageView.setAdjustViewBounds(true);
                    gifImageView.setPadding(0, 8, 0, 16); // top & bottom margin

                    exercisesContainer.addView(gifImageView);

                    // Use Glide to load the GIF
                    Glide.with(requireContext())
                            .asGif()
                            .load(exercise.getGifUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(gifImageView);
                } else {
                    // Add a little spacing after instructions if no GIF
                    TextView spacer = new TextView(requireContext());
                    spacer.setPadding(0, 8, 0, 16);
                    exercisesContainer.addView(spacer);
                }

                //  "Do Exercise" button under each exercise
                Button doExerciseButton = new Button(requireContext());
                doExerciseButton.setText("Do Exercise");
                doExerciseButton.setPadding(0, 0, 0, 32); // some bottom margin

                // Launch CameraExerciseActivity
                doExerciseButton.setOnClickListener(v -> {
                    // Create an Intent
                    Intent intent = new Intent(requireContext(), CameraExerciseActivity.class);
                    // pass id
                    intent.putExtra("exerciseId", exercise.getId());
                    startActivityForResult(intent, REQ_CAMERA);
                });

                // Add this button to the container
                exercisesContainer.addView(doExerciseButton);
            }
        } else {
            // If no exercises, show a No details message
            TextView noDetailsTextView = new TextView(requireContext());
            noDetailsTextView.setText("No exercise details available.");
            noDetailsTextView.setTextSize(16f);
            exercisesContainer.addView(noDetailsTextView);
        }

        // Build and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setTitle("Exercise Details")
                .setPositiveButton("Close", (DialogInterface dialog, int id) -> dialog.dismiss());

        return builder.create();
    }
}
