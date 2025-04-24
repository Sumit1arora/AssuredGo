package com.example.sih2023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class login extends AppCompatActivity {
EditText mobile,pass;
Button login;
TextView gotosignup;
    FirebaseAuth mAuth;
    FirebaseUser user;

    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://sih2024-ac37a-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        mobile=findViewById(R.id.Email_id);
        pass=findViewById(R.id.Password1);
        login=findViewById(R.id.Login_button_1);
        gotosignup=findViewById(R.id.Dont_have);
        mAuth=FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login_id=mobile.getText().toString();
                String password=pass.getText().toString();
                if( !TextUtils.isEmpty(password) && !TextUtils.isEmpty(login_id) ){
                    mAuth.signInWithEmailAndPassword(login_id,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                user=mAuth.getCurrentUser();
                                if(user.isEmailVerified()){
                                    Toast.makeText(login.this, "Logged in Sucessfully", Toast.LENGTH_SHORT).show();
                                    AuthResult uid =task.getResult();
                                    String id= Objects.requireNonNull(uid.getUser()).getUid();
                                    navigate(id);
                                }
                                else{
                                    Toast.makeText(login.this, "Verify Your Email First", Toast.LENGTH_SHORT).show();

                                }


                            }
                            else {
                                Toast.makeText(login.this, "Not registered Or password Incorrect", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

//                databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChild(login_id)) {
//                            String getpass = snapshot.child(login_id).child("password").getValue(String.class);
//                            if (password.equals(getpass)) {
//                                Toast.makeText(login.this, "Logged in Sucessfully", Toast.LENGTH_SHORT).show();
//                                Intent i = new Intent(login.this, choose_vehicle.class);
//                                startActivity(i);
//                            } else {
//                                Toast.makeText(login.this, "Check Username or password", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
            }
        });


    }
    private void navigate(String id){
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String check= snapshot.child(id).child("details").child("firsttime").getValue(String.class);
                if(check.equals("1")){
                    databaseReference.child("users").child(id).child("details").child("firsttime").setValue("0");
                    Intent i = new Intent(login.this, choose_vehicle.class);
                    startActivity(i);
                }
                else{

                    Intent i = new Intent(login.this, dashboardmain.class);
                    startActivity(i);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}