package com.example.sih2023;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class emergency_contact extends AppCompatActivity {

    private EditText nameEditText, phoneEditText;
    private Button addButton;
    private FloatingActionButton backButton;
    private ListView contactsListView;

    private ArrayList<EmergencyContact> contactsList;
    private ArrayAdapter<EmergencyContact> adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("emergency_contacts");

        // Initialize UI elements
        nameEditText = findViewById(R.id.editTextName);
        phoneEditText = findViewById(R.id.editTextPhone);
        addButton = findViewById(R.id.buttonAdd);
        backButton = findViewById(R.id.backButton);
        contactsListView = findViewById(R.id.contactsListView);

        // Initialize contacts list and adapter
        contactsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, contactsList);
        contactsListView.setAdapter(adapter);

        // Add contact button click listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEmergencyContact();
            }
        });

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close this activity and return to previous
            }
        });

        // Set up long click listener for deleting contacts
        contactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }
        });

        // Load saved contacts
        loadEmergencyContacts();
    }

    private void addEmergencyContact() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            return;
        }

        // Basic phone number validation
        if (phone.length() < 10) {
            phoneEditText.setError("Please enter a valid phone number");
            return;
        }

        // Create a unique key for the new contact
        String contactId = contactsRef.push().getKey();

        // Create contact object
        EmergencyContact contact = new EmergencyContact(contactId, name, phone);

        // Save to Firebase
        if (contactId != null) {
            Map<String, Object> contactValues = new HashMap<>();
            contactValues.put("id", contactId);
            contactValues.put("name", name);
            contactValues.put("phone", phone);

            contactsRef.child(contactId).setValue(contactValues)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(emergency_contact.this,
                                "Contact added successfully", Toast.LENGTH_SHORT).show();

                        // Clear input fields
                        nameEditText.setText("");
                        phoneEditText.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(emergency_contact.this,
                                "Failed to add contact: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadEmergencyContacts() {
        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactsList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.child("id").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    if (id != null && name != null && phone != null) {
                        contactsList.add(new EmergencyContact(id, name, phone));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(emergency_contact.this,
                        "Failed to load contacts: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Contact");
        builder.setMessage("Are you sure you want to delete this contact?");

        // Add the buttons
        builder.setPositiveButton("Delete", (dialog, id) -> {
            // User clicked Delete button
            deleteContact(contactsList.get(position).getId());
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog
            dialog.dismiss();
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteContact(String contactId) {
        contactsRef.child(contactId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(emergency_contact.this,
                            "Contact deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(emergency_contact.this,
                            "Failed to delete contact: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Model class for emergency contacts
    public static class EmergencyContact {
        private String id;
        private String name;
        private String phone;

        public EmergencyContact(String id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        @Override
        public String toString() {
            return name + "\n" + phone;
        }
    }
}