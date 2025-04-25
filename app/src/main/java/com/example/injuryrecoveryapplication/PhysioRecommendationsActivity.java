package com.example.injuryrecoveryapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.injuryrecoveryapplication.models.Physiotherapist;
import com.example.injuryrecoveryapplication.utils.InjurySpecialtyUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PhysioRecommendationsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private PhysiotherapistAdapter physiotherapistAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap; // Google Map reference

    // This search term will be determined by the user's injury type
    private String searchTerm = "physiotherapy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physio_recommendations);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Physio Recommendations");
        }

        // Firebase initialization
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerViewPhysios);
        physiotherapistAdapter = new PhysiotherapistAdapter(new ArrayList<>());
        recyclerView.setAdapter(physiotherapistAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Map setup
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            return true;
        });

        // Fetch user's injury type from Firestore before requesting location
        fetchUserInjuryType();
    }

    // Get the user's injury type from Firestore
    private void fetchUserInjuryType() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d("PhysioRecommendations", "Got user doc, exists? " + documentSnapshot.exists());
                    if (documentSnapshot.exists()) {
                        String injuryType = documentSnapshot.getString("injuryType");
                        Log.d("PhysioRecommendations", "User injury type from Firestore: " + injuryType);

                            if (injuryType != null && !injuryType.isEmpty()) {
                                // Use InjurySpecialtyUtils to get array of keywords
                                String[] keywords = InjurySpecialtyUtils.getSpecialtyForInjury(injuryType.toLowerCase());

                                if (keywords != null && keywords.length > 0) {
                                    // Join them all into a single string, e.g., "sprain ankle injury"
                                    String joinedKeywords = String.join(" ", keywords);
                                    Log.d("PhysioRecommendations",
                                            "Using all specialty keywords as searchTerm: " + joinedKeywords);

                                    searchTerm = joinedKeywords;
                                } else {
                                    Log.d("PhysioRecommendations",
                                            "No specialty keywords found, using default physiotherapy.");
                                    searchTerm = "physiotherapy";
                                }
                            } else {
                                // If injuryType is null or empty, fallback
                                Log.d("PhysioRecommendations",
                                        "Injury type is null/empty; using default: physiotherapy");
                                searchTerm = "physiotherapy";
                            }
                    } else {
                        // If user doc doesn't exist, fallback
                        Log.d("PhysioRecommendations",
                                "No user doc found; using default searchTerm=physiotherapy");
                        searchTerm = "physiotherapy";
                    }

                    // Get location
                    requestLocation();
                })
                .addOnFailureListener(e -> {
                    Log.e("PhysioRecommendations", "Error fetching user doc: " + e.getMessage(), e);
                    // Even if failing, still try to get location with default searchTerm
                    searchTerm = "physiotherapy";
                    requestLocation();
                });
    }

    // Check location permission and request location
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    // Get the user's current location and use it to query Yelp api
    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d("PhysioRecommendations", "Location: " + latitude + ", " + longitude);
                            // Call Yelp API using the determined search term.
                            fetchPhysiotherapistsFromYelp(latitude, longitude, searchTerm);
                        } else {
                            Toast.makeText(PhysioRecommendationsActivity.this, "Failed to fetch location.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("PhysioRecommendations", "Error getting location: " + e.getMessage())
                    );
        } else {
            Log.e("PhysioRecommendations", "Location permission not granted.");
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    // Query the Yelp API for physiotherapists near the location using the search term
    private void fetchPhysiotherapistsFromYelp(double latitude, double longitude, String term) {
        String yelpApiKey = "6suoDU3tayIKwJ_IvGtFG1mQ9r8a7LUYcfb58o7vV556pg7DzUIGUh7VX5tE9gM-0s7k01dSAmdE_f9R7EIoWtHQDmu7YBqiX-Zdj0cUKzTD_AS1nLsrQTyHJ88LaHYx";
        YelpApiService yelpApiService = new YelpApiService(yelpApiKey);

        yelpApiService.searchPhysios(latitude, longitude, term, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PhysioRecommendations", "Yelp error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(
                        PhysioRecommendationsActivity.this,
                        "Failed to fetch physiotherapists from Yelp.",
                        Toast.LENGTH_SHORT
                ).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("PhysioRecommendations", "Yelp API response failed: " + response.code());
                    runOnUiThread(() -> Toast.makeText(
                            PhysioRecommendationsActivity.this,
                            "Yelp API response failed.",
                            Toast.LENGTH_SHORT
                    ).show());
                    return;
                }
                String responseData = response.body().string();

                List<Physiotherapist> physiotherapistList = new ArrayList<>();
                try {
                    JSONObject root = new JSONObject(responseData);
                    JSONArray businesses = root.optJSONArray("businesses");

                    if (businesses != null) {
                        for (int i = 0; i < businesses.length(); i++) {
                            JSONObject business = businesses.getJSONObject(i);

                            String name = business.optString("name", "Unknown");
                            JSONObject coords = business.optJSONObject("coordinates");
                            double lat = coords != null ? coords.optDouble("latitude", 0.0) : 0.0;
                            double lng = coords != null ? coords.optDouble("longitude", 0.0) : 0.0;

                            JSONObject location = business.optJSONObject("location");
                            String address1 = (location != null) ? location.optString("address1", "") : "";
                            String city = (location != null) ? location.optString("city", "") : "";
                            String address = address1 + ", " + city;

                            // Parse categories from Yelp response
                            JSONArray categoriesArray = business.optJSONArray("categories");
                            List<Physiotherapist.Category> categories = new ArrayList<>();
                            if (categoriesArray != null) {
                                for (int j = 0; j < categoriesArray.length(); j++) {
                                    JSONObject categoryObj = categoriesArray.getJSONObject(j);
                                    String alias = categoryObj.optString("alias", "");
                                    String title = categoryObj.optString("title", "");
                                    categories.add(new Physiotherapist.Category(alias, title));
                                }
                            }

                            // Create a physiotherapist object and add it to the list
                            physiotherapistList.add(new Physiotherapist(
                                    name,
                                    address,
                                    lat,
                                    lng,
                                    term,
                                    categories
                            ));
                        }
                    }
                } catch (JSONException e) {
                    Log.e("PhysioRecommendations", "Error parsing Yelp JSON: " + e.getMessage());
                }

                runOnUiThread(() -> {
                    Toast.makeText(
                            PhysioRecommendationsActivity.this,
                            "Found " + physiotherapistList.size() + " results",
                            Toast.LENGTH_SHORT
                    ).show();
                    updateRecyclerView(physiotherapistList);
                    updateMapWithPhysiotherapists(physiotherapistList);
                });
            }
        });
    }

   // Update the RecyclerView with the new physiotherapist list
    private void updateRecyclerView(List<Physiotherapist> physiotherapists) {
        physiotherapistAdapter.updateData(physiotherapists);
    }

    // Place markers on the map for each physiotherapist
    private void updateMapWithPhysiotherapists(List<Physiotherapist> physiotherapists) {
        if (mMap != null) {
            mMap.clear();
            for (Physiotherapist physio : physiotherapists) {
                LatLng location = new LatLng(physio.getLatitude(), physio.getLongitude());
                mMap.addMarker(new MarkerOptions().position(location).title(physio.getName()));
            }
            if (!physiotherapists.isEmpty()) {
                LatLng firstLocation = new LatLng(
                        physiotherapists.get(0).getLatitude(),
                        physiotherapists.get(0).getLongitude()
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12));
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this,
                    "Location permission not granted for the map.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation(); // Permission granted, fetch location
            } else {
                Toast.makeText(this,
                        "Location permission is required to fetch physiotherapists.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        } else if (itemId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
        }
    }


    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
