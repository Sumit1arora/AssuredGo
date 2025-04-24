package com.example.sih2023;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class settingFragment extends Fragment {
    private static final String TAG = "settingFragment"; // Tag for logging

    CardView edit_profile, changepass, Add_emergency_button;
    Button signout;
    FirebaseAuth mAuth;
    FirebaseUser user;

    ImageView profileImageView;
    TextView nameTextView;
    TextView emailTextView;

    DatabaseReference userRef;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public settingFragment() {
        // Required empty public constructor
    }

    public static settingFragment newInstance(String param1, String param2) {
        settingFragment fragment = new settingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting2, container, false);

        // Initialize profile UI elements
        profileImageView = view.findViewById(R.id.profile_image);
        nameTextView = view.findViewById(R.id.profile_name);
        emailTextView = view.findViewById(R.id.profile_email);

        // Setting options
        edit_profile = view.findViewById(R.id.edit_profile_card);
        changepass = view.findViewById(R.id.change_password_card);
        Add_emergency_button = view.findViewById(R.id.emergency_contact_card);
        signout = view.findViewById(R.id.Sign_Out);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            FirebaseAuth.getInstance().signOut();
            Intent i2 = new Intent(getActivity(), login.class);
            startActivity(i2);
        } else {
            // Initialize default display values
            if (user.getEmail() != null) {
                emailTextView.setText(user.getEmail());
            }
            nameTextView.setText("User");  // Default value

            // Try different database paths - we'll check all possible locations
            checkUserData();
        }

        // Click listeners
        changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), change_pass_option.class);
                startActivity(i);
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), editprofile.class);
                startActivity(i);
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i2 = new Intent(getActivity(), login.class);
                startActivity(i2);
                requireActivity().finish();
            }
        });

        Add_emergency_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), emergency_contact.class);
                startActivity(i);
            }
        });

        return view;
    }

    // Try different paths for the user data
    private void checkUserData() {
        // Try the path used in signup (lowercase 'users')
        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("details");
        Log.d(TAG, "Checking path: " + userRef.toString());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Found data at details path in lowercase path: " + dataSnapshot.toString());
                    processUserData(dataSnapshot);
                } else {
                    Log.d(TAG, "No data at details path in lowercase path, trying uppercase path");
                    // Try with uppercase "Users"
                    DatabaseReference upperRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("details");
                    upperRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.d(TAG, "Found data at uppercase path: " + dataSnapshot.toString());
                                processUserData(dataSnapshot);
                            } else {
                                Log.d(TAG, "No data at uppercase path, trying without details node");
                                // Final attempt - direct path without details node
                                DatabaseReference directRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                                directRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Log.d(TAG, "Found data at direct path: " + dataSnapshot.toString());
                                            if (dataSnapshot.hasChild("details")) {
                                                processUserData(dataSnapshot.child("details"));
                                            } else {
                                                processUserData(dataSnapshot);
                                            }
                                        } else {
                                            Log.d(TAG, "No user data found in any location");
                                            Toast.makeText(getContext(), "Unable to load profile data", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, "Database error on direct path: " + databaseError.getMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Database error on uppercase path: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Database connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Process user data from any snapshot
    private void processUserData(DataSnapshot dataSnapshot) {
        // Debug - print all available fields
        for (DataSnapshot child : dataSnapshot.getChildren()) {
            Log.d(TAG, "Field: " + child.getKey() + " = " + child.getValue());
        }

        // Try different field names for fullname
        String name = null;

        // Try "fullname" field
        if (dataSnapshot.hasChild("fullname")) {
            name = dataSnapshot.child("fullname").getValue(String.class);
            Log.d(TAG, "Found name in 'fullname' field: " + name);
        }
        // Try "name" field if fullname doesn't exist or is empty
        else if (dataSnapshot.hasChild("name")) {
            name = dataSnapshot.child("name").getValue(String.class);
            Log.d(TAG, "Found name in 'name' field: " + name);
        }
        // Try "username" field
        else if (dataSnapshot.hasChild("username")) {
            name = dataSnapshot.child("username").getValue(String.class);
            Log.d(TAG, "Found name in 'username' field: " + name);
        }

        // Update UI with name if found
        if (name != null && !name.isEmpty()) {
            nameTextView.setText(name);
        }

        // Try getting email
        String email = null;
        if (dataSnapshot.hasChild("email")) {
            email = dataSnapshot.child("email").getValue(String.class);
            Log.d(TAG, "Found email: " + email);
            if (email != null && !email.isEmpty()) {
                emailTextView.setText(email);
            }
        }

        // Set default profile image
        profileImageView.setImageResource(R.drawable.img);
    }
}