package com.example.sih2023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    Button login_redirect,signup_redirect;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login_redirect=findViewById(R.id.Login_button);
        signup_redirect=findViewById(R.id.Sign_Up_Button);
        mAuth=FirebaseAuth.getInstance();
        login_redirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(MainActivity.this,login.class);
                startActivity(i);
            }
        });
        signup_redirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(MainActivity.this,signup.class);
                startActivity(i);
            }
        });



    }
    @Override
    protected void onStart() {

        super.onStart();
        FirebaseUser user =mAuth.getCurrentUser();
        if(user!=null){
            startActivity(new Intent(MainActivity.this, dashboardmain.class));
        }
    }


}

