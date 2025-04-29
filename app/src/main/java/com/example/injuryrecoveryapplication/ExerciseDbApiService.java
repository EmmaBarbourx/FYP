package com.example.injuryrecoveryapplication;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.injuryrecoveryapplication.models.Exercise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExerciseDbApiService {

    private static final String TAG = "ExerciseDbApiService";
    // Base URL for the ExerciseDB API
    private static final String BASE_URL = "https://exercisedb.p.rapidapi.com/exercises";


    private static final String RAPIDAPI_KEY = "5f6a54c55fmshd0a01f5306e256ep10c1d1jsn2c41d05836ce";
    private static final String RAPIDAPI_HOST = "exercisedb.p.rapidapi.com";

    private final OkHttpClient client = new OkHttpClient();


    public interface ExerciseDbCallback {
        void onSuccess(List<Exercise> exercises);
        void onFailure(String errorMessage);
    }

    public interface OneExerciseCallback {
        void onSuccess(Exercise exercise);
        void onFailure(String errorMessage);
    }

    // Fetches exercises by a given body part
    public void fetchExercisesByBodyPartAll(String bodyPart, ExerciseDbCallback callback) {

        String url = BASE_URL + "/bodyPart/" + bodyPart + "?limit=0";

        // Build the GET request with headers
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Key", RAPIDAPI_KEY)
                .addHeader("X-RapidAPI-Host", RAPIDAPI_HOST)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "fetchExercisesByBodyPartAll onFailure: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Check for HTTP success
                if (!response.isSuccessful() || response.body() == null) {
                    String err = "Unsuccessful response code: " + response.code();
                    Log.e(TAG, "fetchExercisesByBodyPartAll onResponse: " + err);
                    callback.onFailure(err);
                    return;
                }

                // Convert the response to a String
                String json = response.body().string();
                Log.d(TAG, "fetchExercisesByBodyPartAll JSON: " + json);

                try {
                    JSONArray jsonArray = new JSONArray(json);
                    List<Exercise> exerciseList = new ArrayList<>();

                    // Parse each JSON object into an Exercise instance
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        String exId = obj.optString("id");
                        String exName = obj.optString("name");
                        String exBodyPart = obj.optString("bodyPart");
                        String exEquipment = obj.optString("equipment");
                        String exGifUrl = obj.optString("gifUrl");
                        String exTarget = obj.optString("target");

                        Log.d(TAG, "API exercise [" + i + "]  id=" + exId + "  name=" + exName);
                        JSONArray secondaryMusclesArray = obj.optJSONArray("secondaryMuscles");
                        JSONArray instructionsArray = obj.optJSONArray("instructions");

                        // Build Exercise object
                        Exercise exercise = new Exercise();
                        exercise.setId(exId);
                        exercise.setName(exName);
                        exercise.setBodyPart(exBodyPart);
                        exercise.setEquipment(exEquipment);
                        exercise.setGifUrl(exGifUrl);
                        exercise.setTarget(exTarget);

                        // Combine instructions if present
                        if (instructionsArray != null) {
                            // Combine instructions into a single string
                            StringBuilder sb = new StringBuilder();
                            for (int j = 0; j < instructionsArray.length(); j++) {
                                sb.append("- ").append(instructionsArray.getString(j)).append("\n");
                            }
                            exercise.setInstructions(sb.toString());
                        } else {
                            exercise.setInstructions("No instructions from ExerciseDB.");
                        }

                        exerciseList.add(exercise);
                    }

                    callback.onSuccess(exerciseList);

                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error: " + e.getMessage());
                    callback.onFailure("JSON parse error: " + e.getMessage());
                }
            }
        });
    }

    // Pull a single exercise
    public void fetchExerciseById(String id, OneExerciseCallback cb) {

        String url = BASE_URL + "/exercise/" + id;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Key", RAPIDAPI_KEY)
                .addHeader("X-RapidAPI-Host", RAPIDAPI_HOST)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call,
                                            @NonNull IOException e) {
                Log.e(TAG, "fetchExerciseById onFailure: " + e.getMessage());
                cb.onFailure(e.getMessage());
            }

            @Override public void onResponse(@NonNull Call call,
                                             @NonNull Response response)
                    throws IOException {

                if (!response.isSuccessful() || response.body() == null) {
                    String err = "Unsuccessful response code: " + response.code();
                    Log.e(TAG, "fetchExerciseById onResponse: " + err);
                    cb.onFailure(err);
                    return;
                }

                String json = response.body().string();

                try {
                    JSONObject obj;
                    if (json.trim().startsWith("[")) {
                        obj = new JSONArray(json).getJSONObject(0);
                    } else {
                        obj = new JSONObject(json);
                    }

                    // Map JSON fields to Exercise model
                    Exercise exercise = new Exercise();
                    exercise.setId(obj.optString("id"));
                    exercise.setName(obj.optString("name"));
                    exercise.setBodyPart(obj.optString("bodyPart"));
                    exercise.setEquipment(obj.optString("equipment"));
                    exercise.setGifUrl(obj.optString("gifUrl"));
                    exercise.setTarget(obj.optString("target"));

                    // Put the instructions array into bullet list
                    JSONArray instructionsArray = obj.optJSONArray("instructions");
                    if (instructionsArray != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < instructionsArray.length(); j++) {
                            sb.append("- ").append(instructionsArray.getString(j)).append("\n");
                        }
                        exercise.setInstructions(sb.toString());
                    } else {
                        exercise.setInstructions("No instructions from ExerciseDB.");
                    }

                    cb.onSuccess(exercise);

                } catch (JSONException je) {
                    Log.e(TAG, "JSON parse error: " + je.getMessage());
                    cb.onFailure(je.getMessage());
                }
            }
        });
    }

}