package com.example.sih2023;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class choose_vehicle extends AppCompatActivity {
    CardView two,three,four,others;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_your_vehicle);
        two=findViewById(R.id.Two_wheller_card);
        three=findViewById(R.id.Three_vehicle_card);
        four=findViewById(R.id.Four_wheller_card);
        others=findViewById(R.id.Other_vehcile_card);
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(choose_vehicle.this, two_wheeler.class);
                startActivity(i);
                finish();
            }
        });
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(choose_vehicle.this, three_wheeler.class);
                startActivity(i);
                finish();
            }
        });
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(choose_vehicle.this, four_wheeler.class);
                startActivity(i);
                finish();
            }
        });
        others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(choose_vehicle.this, other_wheeler.class);
                startActivity(i);
                finish();
            }
        });





    }

}
