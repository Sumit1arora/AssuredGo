package com.example.sih2023;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class dashboardmain extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    public boolean check = false;
    DatabaseReference databaseReference;
    static String username = "";

    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final int SMS_PERMISSION_CODE = 101;
    private static final String TAG = "DashboardMain";

    private FusedLocationProviderClient fusedLocationClient;
    public double currentLatitude = 0.0, currentLongitude = 0.0;

    // Store emergency services data
    private List<EmergencyLocation> hospitals = new ArrayList<>();
    private List<EmergencyLocation> policeStations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_main);

        // Bottom nav setup
        bottomNavigationView = findViewById(R.id.BottomNavigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.Vehicle_icon) {
                loadfrag(new vehicleFragment(), false);
            } else if (id == R.id.Setting_icon) {
                loadfrag(new settingFragment(), false);
            } else {
                // When map is loaded, pass any emergency services data we have
                MapsFragment mapFragment = new MapsFragment();
                if (!hospitals.isEmpty() || !policeStations.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putSerializable("hospitals", (Serializable) hospitals);
                    args.putSerializable("police", (Serializable) policeStations);
                    mapFragment.setArguments(args);
                }
                loadfrag(mapFragment, true);
            }
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.ghar_home);

        // Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Permissions
        requestPermissions();

        // Firebase alert listener
        databaseListner();
    }

    private void requestPermissions() {
        // Request both location and SMS permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET},
                    LOCATION_PERMISSION_CODE);
        } else {
            getLastLocation();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        Log.d(TAG, "Updated location: " + currentLatitude + ", " + currentLongitude);

                        // Find emergency services once we have location
                        findNearbyEmergencyServices(currentLatitude, currentLongitude);
                    } else {
                        Log.w(TAG, "Location is null");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location: " + e.getMessage());
                });
    }

    public void loadfrag(Fragment fragment, boolean add) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (add) ft.add(R.id.flFragment, fragment);
        else     ft.replace(R.id.flFragment, fragment);
        ft.commit();
    }

    private void databaseListner() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double distance = snapshot.child("alert/distance").getValue(Double.class);
                if (distance == null) return;

                int inz = distance.intValue();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (inz < 5 && !check) {
                    // Force location update
                    getLastLocation();

                    // Refresh emergency services data
                    findNearbyEmergencyServices(currentLatitude, currentLongitude);

                    // Update police metrics
                    databaseReference.child("police/traffic_accidents").setValue("11");
                    databaseReference.child("police/fir_generated").setValue("11");

                    // Create FIR entry
                    String now = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));

                    // Format location as URL string exactly as shown in your example
                    String locationUrl = "https://maps.google.com/?q=" +
                            String.format("%.6f", currentLatitude).replace(",", ".") + "," +
                            String.format("%.6f", currentLongitude).replace(",", ".");

                    DatabaseReference firRef = databaseReference.child("police/fir").child(uid);
                    firRef.child("id").setValue(uid);
                    firRef.child("location").setValue(locationUrl);
                    firRef.child("name").setValue(username);
                    firRef.child("status").setValue("0");
                    firRef.child("time").setValue(now);

                    Log.i(TAG, "FIR created with location: " + locationUrl);

                    // Fetch emergency contacts and send SMS to all of them
                    fetchEmergencyContactsAndSendAlerts(locationUrl);

                    check = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dashboardmain.this,
                        "Failed to read alerts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEmergencyContactsAndSendAlerts(String locationUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("emergency_contacts");

        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> phoneNumbers = new ArrayList<>();

                // Collect all contact numbers
                for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                    String phoneNumber = contactSnapshot.child("phone").getValue(String.class);
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        phoneNumbers.add(phoneNumber);
                    }
                }

                // Send SMS to all emergency contacts
                if (!phoneNumbers.isEmpty()) {
                    sendSMSToContacts(phoneNumbers, locationUrl);
                } else {
                    Log.w(TAG, "No emergency contacts found to send alerts");
                    Toast.makeText(dashboardmain.this,
                            "No emergency contacts found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read emergency contacts", databaseError.toException());
                Toast.makeText(dashboardmain.this,
                        "Failed to read emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSMSToContacts(List<String> phoneNumbers, String locationUrl) {
        if (ActivityCompat.checkSelfPermission(dashboardmain.this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            SmsManager sms = SmsManager.getDefault();
            String message = "EMERGENCY ALERT: Possible crash detected. Location: " + locationUrl;

            int successCount = 0;

            for (String phoneNumber : phoneNumbers) {
                try {
                    sms.sendTextMessage(phoneNumber, null, message, null, null);
                    successCount++;
                    Log.i(TAG, "SMS sent successfully to " + phoneNumber);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
                }
            }

            String resultMessage = "Alert sent to " + successCount + " out of " +
                    phoneNumbers.size() + " emergency contacts";
            Toast.makeText(dashboardmain.this, resultMessage, Toast.LENGTH_SHORT).show();

        } else {
            Log.w(TAG, "SEND_SMS permission not granted");
            Toast.makeText(dashboardmain.this,
                    "SMS permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // New methods for finding emergency services with Google Places API

    // Helper class to store emergency location info
    public static class EmergencyLocation implements Serializable {
        String name;
        String type; // "hospital" or "police"
        double latitude;
        double longitude;

        public EmergencyLocation(String name, double lat, double lng, String type) {
            this.name = name;
            this.latitude = lat;
            this.longitude = lng;
            this.type = type;
        }
    }

    // Method to find nearby emergency services
    private void findNearbyEmergencyServices(double latitude, double longitude) {
        // Create a new thread for network operations
        new Thread(() -> {
            try {
                // Find hospitals nearby
                hospitals = fetchNearbyPlaces(latitude, longitude, "hospital");

                // Find police stations nearby
                policeStations = fetchNearbyPlaces(latitude, longitude, "police");

                // Update current map fragment if it's active
                runOnUiThread(() -> {
                    // Find the current map fragment if it exists
                    for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                        if (fragment instanceof MapsFragment) {
                            // Pass the data to the existing map fragment
                            ((MapsFragment) fragment).updateEmergencyServices(hospitals, policeStations);
                            break;
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error finding emergency services: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(dashboardmain.this,
                            "Error finding emergency services", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Method to fetch places of a specific type
    private List<EmergencyLocation> fetchNearbyPlaces(double latitude, double longitude, String type)
            throws IOException, JSONException {
        List<EmergencyLocation> locations = new ArrayList<>();

        // Create URL for Places API
        String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + latitude + "," + longitude +
                "&radius=10000" + // Search within 5km
                "&type=" + type +
                "&key=" + getString(R.string.google_maps_key);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Read response
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray results = jsonResponse.getJSONArray("results");

        // Process each place (limit to 5 of each type to avoid clutter)
        for (int i = 0; i < Math.min(results.length(), 5); i++) {
            JSONObject place = results.getJSONObject(i);

            String name = place.getString("name");

            JSONObject geometry = place.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            double placeLat = location.getDouble("lat");
            double placeLng = location.getDouble("lng");

            EmergencyLocation loc = new EmergencyLocation(name, placeLat, placeLng, type);
            locations.add(loc);
        }

        return locations;
    }
}