/**package com.example.injuryrecoveryapplication;

import android.util.Log;
import com.example.injuryrecoveryapplication.models.Exercise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WgerApiService {
    private static final String TAG = "WgerApiService";
    private static final String TRANSLATION_URL = "https://wger.de/api/v2/exercise-translation/";
    private static final String BASE_URL = "https://wger.de/api/v2/exercise/";

    private static final String EXERCISE_IMAGE_URL = "https://wger.de/api/v2/exerciseimage/";

    private final OkHttpClient client = new OkHttpClient();

    // Get exercises for a given category and page
    public void getExercises(String category, int page, Callback callback) {
        try {
            String url = BASE_URL + "?language=2&status=2&page=" + page;
            if (category != null && !category.isEmpty()) {
                url += "&category=" + URLEncoder.encode(category, StandardCharsets.UTF_8.toString());
            }

            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {

        }
    }

    //Get exercises with default page=1
    public void getExercises(String category, Callback callback) {
        getExercises(category, 1, callback);
    }

    // Get exercises using offset-based pagination (20 items per call)
    public void getExercisesOffset(String category, int offset, Callback callback) {
        try {
            // We’ll fetch 20 items at a time, starting from offset=0, then offset=20, offset=40, etc.
            String url = BASE_URL + "?language=2&status=2&limit=20&offset=" + offset;
            if (category != null && !category.isEmpty()) {
                url += "&category=" + URLEncoder.encode(category, StandardCharsets.UTF_8.toString());
            }
            Log.d(TAG, "Request URL: " + url);
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Error building Wger request: " + e.getMessage());
        }
    }

    // Parse a JSON response into a List of Exercise objects
    public static List<Exercise> parseExercises(String jsonResponse) {
        List<Exercise> exercises = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray results = root.optJSONArray("results");
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    int id = obj.optInt("id");
                    String name = obj.optString("name");
                    String description = obj.optString("description");
                    int category = obj.optInt("category");

                    // Log the raw data for each exercise
                    Log.d(TAG, "Parsed Exercise - ID: " + id +
                            ", Name: '" + name +
                            "', Description: '" + description + "'");

                    exercises.add(new Exercise(id, name, description, category, "", ""));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing exercises JSON: " + e.getMessage());
        }
        return exercises;
    }

    // Parse paged exercises JSON response and return the results, next URL, and total count
    public static PagedExercisesResult parseExercisesPaged(String jsonResponse) {
        PagedExercisesResult result = new PagedExercisesResult();
        result.items = new ArrayList<>();
        result.nextUrl = null;
        result.totalCount = 0;

        try {
            JSONObject root = new JSONObject(jsonResponse);

            // Get the 'count' field
            if (!root.isNull("count")) {
                result.totalCount = root.optInt("count", 0);
            }

            // Get the 'next' field
            if (!root.isNull("next")) {
                String nextRaw = root.optString("next", null);

                if (nextRaw != null && !"null".equals(nextRaw)) {
                    result.nextUrl = nextRaw;
                }
            }

            // Parse the results array
            JSONArray resultsArray = root.optJSONArray("results");
            if (resultsArray != null) {
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject obj = resultsArray.getJSONObject(i);
                    int id = obj.optInt("id");
                    String name = obj.optString("name");
                    String description = obj.optString("description");
                    int category = obj.optInt("category");
                    result.items.add(new Exercise(id, name, description, category, "", ""));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing paged exercises JSON: " + e.getMessage());
        }

        return result;
    }

    // Get exercise images for a given exercise ID
    public void getExerciseImages(int exerciseId, Callback callback) {
        try {
            String url = EXERCISE_IMAGE_URL + "?exercise=" + exerciseId + "&language=2";
            Log.d(TAG, "Exercise Images URL: " + url);
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Error building exercise images request: " + e.getMessage());
        }
    }

    // New interface for a merged callback
    public interface MergedCallback {
        void onSuccess(List<Exercise> mergedList);
        void onFailure(Call call, IOException e);
    }

    // New method to get merged exercises using offset-based pagination
    // It calls both the original exercise endpoint and the translation endpoint and merges results based on exercise id.
    public void getMergedExercisesOffset(final String category, final int offset, final MergedCallback callback) {
        try {
            String url = BASE_URL + "?language=2&status=2&limit=20&offset=" + offset;
            if (category != null && !category.isEmpty()) {
                url += "&category=" + URLEncoder.encode(category, StandardCharsets.UTF_8.toString());
            }
            Log.d(TAG, "Original Request URL: " + url);
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(Call call, Response originalResponse) throws IOException {
                    String originalJson = originalResponse.body().string();
                    final List<Exercise> originalList = parseExercises(originalJson);

                    // Now call the translation endpoint
                    try {
                        String transUrl = TRANSLATION_URL + "?language=2&limit=20&offset=" + offset;
                        // You can add additional parameters here if necessary
                        Log.d(TAG, "Translation Request URL: " + transUrl);
                        Request transRequest = new Request.Builder().url(transUrl).build();
                        client.newCall(transRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                // If the translation call fails, we return the original list
                                callback.onSuccess(originalList);
                            }

                            @Override
                            public void onResponse(Call call, Response translationResponse) throws IOException {
                                String transJson = translationResponse.body().string();
                                // Parse translations into a map of exercise id to TranslationData
                                final java.util.Map<Integer, TranslationData> translations = parseTranslations(transJson);

                                // *** Merging loop with logging added ***
                                for (Exercise ex : originalList) {
                                    if (translations.containsKey(ex.getId())) {
                                        TranslationData td = translations.get(ex.getId());
                                        ex.setName(td.name);
                                        ex.setDescription(td.description);
                                        Log.d(TAG, "Merged exercise id " + ex.getId() + " with translation: " + td.name);
                                    } else {
                                        Log.d(TAG, "No translation found for exercise id " + ex.getId());
                                    }
                                }

                                callback.onSuccess(originalList);
                            }
                        });
                    } catch (Exception e) {
                        callback.onFailure(call, new IOException("Error building translation request: " + e.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(null, new IOException("Error building original request: " + e.getMessage()));
        }
    }

    // New helper method to parse the translation endpoint JSON into a map
    private static java.util.Map<Integer, TranslationData> parseTranslations(String jsonResponse) {
        java.util.Map<Integer, TranslationData> translations = new java.util.HashMap<>();
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray results = root.optJSONArray("results");
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    int exerciseId = obj.optInt("exercise");
                    String name = obj.optString("name");
                    String description = obj.optString("description");

                    // Log the raw translation data
                    Log.d(TAG, "Parsed Translation - Exercise ID: " + exerciseId +
                            ", Name: '" + name +
                            "', Description: '" + description + "'");

                    TranslationData td = new TranslationData(name, description);
                    translations.put(exerciseId, td);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing translations JSON: " + e.getMessage());
        }
        return translations;
    }

    // New inner static class to hold translation data (name and description)
    private static class TranslationData {
        String name;
        String description;
        TranslationData(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    // Helper class to hold paged exercises result data
    public static class PagedExercisesResult {
        public List<Exercise> items;
        public String nextUrl;
        public int totalCount;
    }
}
**/