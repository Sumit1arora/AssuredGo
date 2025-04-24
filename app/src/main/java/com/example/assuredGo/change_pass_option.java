package com.example.sih2023;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class change_pass_option extends AppCompatActivity {
    FirebaseUser user;
    private FirebaseAuth auth;
    ConstraintLayout email_pass_rest;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);
        email_pass_rest=findViewById(R.id.Resent_constarint);
        user= FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        email_pass_rest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail=user.getEmail();
                auth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(change_pass_option.this,"Reset Mail Sent Successfully",Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent i2 =new Intent(getApplicationContext(),login.class);
                            startActivity(i2);
                            finish();

                        }
                    }
                });

            }
        });

    }
}
