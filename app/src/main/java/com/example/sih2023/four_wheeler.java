package com.example.sih2023;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class four_wheeler extends AppCompatActivity {
    EditText vehicle_number,vehicle_chassis,vehicle_engine;
    Button submit;
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://sih2024-ac37a-default-rtdb.firebaseio.com/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_of_vehicle_four);
        vehicle_number=findViewById(R.id.Vehicle_Number);
        vehicle_engine=findViewById(R.id.Engine_Number);
        vehicle_chassis=findViewById(R.id.Chasis_Number);
        submit=findViewById(R.id.Submit_button);
        mAuth= FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        String id=user.getUid();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String vnumber=vehicle_number.getText().toString();
                String enumber=vehicle_engine.getText().toString();
                String cnumber=vehicle_chassis.getText().toString();
                if(!TextUtils.isEmpty(vnumber) && !TextUtils.isEmpty(cnumber) && !TextUtils.isEmpty(enumber)){
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {


                            databaseReference.child("users").child(id).child("four_wheeler").child(vnumber).child("vehicle_number").setValue(vnumber);
                            databaseReference.child("users").child(id).child("four_wheeler").child(vnumber).child("chassis_number").setValue(cnumber);
                            databaseReference.child("users").child(id).child("four_wheeler").child(vnumber).child("engine_number").setValue(enumber);

                            Toast.makeText(four_wheeler.this,"Registered Sucessfully",Toast.LENGTH_SHORT).show();
                            finish();
                            Intent i= new Intent(four_wheeler.this, dashboardmain.class);
                            startActivity(i);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }
}
