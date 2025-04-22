package com.example.sih2023;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class vehicleFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://sih2024-ac37a-default-rtdb.firebaseio.com/");
    CircleImageView Vehicle_info_profile;
    String name;

    private static final int ADD_MORE_VEHICLE_ID = 1001;

    public vehicleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vehicle, container, false);

        TextView dashboardname = view.findViewById(R.id.Name_Vechicle_info);
        Vehicle_info_profile = view.findViewById(R.id.Vehicle_info_profile);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        String id = user.getUid();

        // Fetch user profile image from Firebase Storage
        StorageReference reference2 = FirebaseStorage.getInstance().getReference(id + "/" + id + ".jpg");
        try {
            File localfile = File.createTempFile("tempfile", ".jpg");
            reference2.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    Vehicle_info_profile.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        name(id, dashboardname, view);
        return view;
    }

    private void name(String id, TextView dashboard, View view) {
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nam = snapshot.child(id).child("details").child("fullname").getValue(String.class);
                name = nam;

                // Merging all vehicle data
                Map<String, Object> map2 = (Map<String, Object>) snapshot.child(id).child("two_wheeler").getValue();
                Map<String, Object> map3 = (Map<String, Object>) snapshot.child(id).child("three_wheeler").getValue();
                Map<String, Object> map4 = (Map<String, Object>) snapshot.child(id).child("four_wheeler").getValue();
                Map<String, Object> map5 = (Map<String, Object>) snapshot.child(id).child("other_wheeler").getValue();

                // Handle null maps
                if (map2 == null) map2 = new java.util.HashMap<>();
                if (map3 != null) map2.putAll(map3);
                if (map4 != null) map2.putAll(map4);
                if (map5 != null) map2.putAll(map5);

                Log.i("map", String.valueOf(map2.keySet()));

                // Set greeting text for the user
                if (nam.trim().length() < 6) {
                    dashboard.setText("Hi " + nam + " \uD83D\uDC4B");
                } else {
                    dashboard.setText("Hi, " + nam.substring(0, 5) + "..");
                }

                // Get the LinearLayout where buttons will be added
                LinearLayout linearlayout = view.findViewById(R.id.btnholder);

                // Clear existing buttons before adding new ones
                linearlayout.removeAllViews();

                // Create buttons dynamically for each vehicle in map2
                Set<String> set = map2.keySet();
                String[] arr = set.toArray(new String[0]);
                for (int i = 0; i < map2.size(); i++) {
                    CardView cardView = new CardView(view.getContext());
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.bluee));
                    cardView.setRadius(30);
                    cardView.setContentPadding(40, 0, 40, 20);

                    LinearLayout layout = new LinearLayout(view.getContext());
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setGravity(Gravity.TOP);

                    ImageView imageView = new ImageView(view.getContext());
                    imageView.setImageResource(R.drawable.cardcar);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(300, 300);
                    imageParams.setMargins(60, 49, 60, 49);
                    imageView.setLayoutParams(imageParams);

                    LinearLayout textContainer = new LinearLayout(view.getContext());
                    textContainer.setOrientation(LinearLayout.VERTICAL);

                    TextView textView = new TextView(view.getContext());
                    textView.setText(arr[i]);
                    textView.setTextSize(18);
                    textView.setTextColor(getResources().getColor(R.color.white));
                    textView.setPadding(20, 20, 20, 0);

                    Paint paint = new Paint();
                    paint.setTextSize(textView.getTextSize());
                    float textWidth = paint.measureText(arr[i]);
                    int lineWidth = (int) textWidth;

                    View line = new View(view.getContext());
                    LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(lineWidth, 4);
                    lineParams.setMargins(20, 8, 40, 0);
                    line.setLayoutParams(lineParams);
                    line.setBackgroundColor(getResources().getColor(R.color.white));

                    // Extract vehicle data from map2
                    Map<String, Object> vehicleData = (Map<String, Object>) map2.get(arr[i]);

                    // Add debug logging to see what data is available
                    Log.d("VehicleDebug", "Vehicle: " + arr[i]);
                    if (vehicleData != null) {
                        Log.d("VehicleDebug", "Vehicle data keys: " + vehicleData.keySet());
                    }

                    // Engine Number TextView
                    TextView engineText = new TextView(view.getContext());
                    engineText.setTextColor(getResources().getColor(R.color.white));
                    engineText.setTextSize(14);
                    engineText.setPadding(20, 10, 0, 0);
                    engineText.setText("Engine No: " + ((vehicleData != null && vehicleData.get("engine_number") != null)
                            ? vehicleData.get("engine_number").toString() : "N/A"));

                    // Chassis Number TextView
                    TextView chassisText = new TextView(view.getContext());
                    chassisText.setTextColor(getResources().getColor(R.color.white));
                    chassisText.setTextSize(14);
                    chassisText.setPadding(20, 6, 0, 20);
                    chassisText.setText("Chassis No: " + ((vehicleData != null && vehicleData.get("chassis_number") != null)
                            ? vehicleData.get("chassis_number").toString() : "N/A"));

                    textContainer.addView(textView);
                    textContainer.addView(line);
                    textContainer.addView(engineText);
                    textContainer.addView(chassisText);

                    layout.addView(imageView);
                    layout.addView(textContainer);

                    cardView.addView(layout);

                    LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    cardParams.setMargins(100, 50, 100, 10);

                    cardView.setOnClickListener(v -> handleButtonClick(textView.getText().toString()));

                    linearlayout.addView(cardView, cardParams);
                }

                Button addMoreButton = new Button(view.getContext());
                addMoreButton.setText(" + Add More Vehicle");
                addMoreButton.setTextColor(getResources().getColor(R.color.white));
                addMoreButton.setId(ADD_MORE_VEHICLE_ID);
                addMoreButton.setBackgroundColor(getResources().getColor(R.color.bluee));
                addMoreButton.setTextSize(18);
                addMoreButton.setBackground(getResources().getDrawable(R.drawable.rounded_button));

                addMoreButton.setPadding(20, 0, 20, 0);

                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                buttonParams.setMargins(80, 40, 80, 10);
                linearlayout.addView(addMoreButton, buttonParams);

                // Set the listener for the "Add More Vehicle" button
                addMoreButton.setOnClickListener(v -> {
                    Intent j = new Intent(getActivity(), choose_vehicle.class);
                    startActivity(j);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
                Log.e("VehicleFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private void handleButtonClick(String buttonText) {
        Toast.makeText(getActivity(), "Button clicked: " + buttonText, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getContext(), vehicle_info.class);
        i.putExtra("carnum", buttonText);
        i.putExtra("name", name);
        startActivity(i);
    }
}