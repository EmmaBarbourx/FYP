package com.example.injuryrecoveryapplication;

import android.util.Log;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YelpApiService {
    private static final String TAG = "YelpApiService";
    private static final String BASE_URL = "https://api.yelp.com/v3/businesses/search";

    private final String apiKey;

    public YelpApiService(String apiKey) {
        this.apiKey = apiKey;
    }


    // Searches for physiotherapy businesses near a location using the given term
    public void searchPhysios(double latitude, double longitude, String term, Callback callback) {
        try {
            String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8.toString());
            // Build the request URL with the location, a 30km radius, and the physiotherapy category
            String url = BASE_URL
                    + "?term=" + encodedTerm
                    + "&latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&radius=30000" // 30km radius
                    + "&categories=physiotherapy";

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Error building Yelp request: " + e.getMessage());
        }
    }
}
