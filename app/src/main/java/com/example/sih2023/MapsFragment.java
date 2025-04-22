// File: MapsFragment.java
package com.example.sih2023;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;
import java.io.IOException;

public class MapsFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0.0, currentLng = 0.0;
    CircleImageView Dashboard_Profile;
    DatabaseReference databaseReference = FirebaseDatabase
            .getInstance("https://sih2024-ac37a-default-rtdb.firebaseio.com/")
            .getReference();
    FirebaseUser user;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        Dashboard_Profile = view.findViewById(R.id.Dashboard_Profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        loadProfilePic();

        return view;
    }

    private void loadProfilePic() {
        String uid = user.getUid();
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference(uid + "/" + uid + ".jpg");
        try {
            File tmp = File.createTempFile("profile", "jpg");
            ref.getFile(tmp).addOnSuccessListener(task -> {
                Bitmap bmp = BitmapFactory.decodeFile(tmp.getAbsolutePath());
                Dashboard_Profile.setImageBitmap(bmp);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsername(TextView dash) {
        String uid = user.getUid();
        databaseReference.child("users")
                .child(uid).child("details").child("fullname")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        String name = snap.getValue(String.class);
                        dashboardmain.username = name != null ? name : "";
                        if (name != null && name.length() > 5)
                            dash.setText("Hii, " + name.substring(0, 5) + "..");
                        else if (name != null)
                            dash.setText("Hii, " + name);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        ((SupportMapFragment) getChildFragmentManager()
                                .findFragmentById(R.id.map))
                                .getMapAsync(googleMap ->
                                        drawMarkers(googleMap, location));
                    } else {
                        Toast.makeText(requireContext(),
                                "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawMarkers(GoogleMap map, Location loc) {
        // scale your custom icons
        int h = 150, w = 150;
        Bitmap small = Bitmap.createScaledBitmap(
                ((BitmapDrawable)getResources()
                        .getDrawable(R.drawable.locationpin)).getBitmap(), w, h, false);
        Bitmap police = Bitmap.createScaledBitmap(
                ((BitmapDrawable)getResources()
                        .getDrawable(R.drawable.policemap)).getBitmap(), w, h, false);
        Bitmap hospital = Bitmap.createScaledBitmap(
                ((BitmapDrawable)getResources()
                        .getDrawable(R.drawable.hospitalmap)).getBitmap(), w, h, false);

        LatLng me    = new LatLng(loc.getLatitude(), loc.getLongitude());
        LatLng pol   = new LatLng(32.896966, 74.735483);
        LatLng hosp  = new LatLng(32.842005, 74.815920);
        LatLng hospi  = new LatLng(32.842005, 74.815920);

        map.addMarker(new MarkerOptions()
                .position(me).title("My Location")
                .icon(BitmapDescriptorFactory.fromBitmap(small)));
        map.addMarker(new MarkerOptions()
                .position(pol).title("Police Station")
                .icon(BitmapDescriptorFactory.fromBitmap(police)));
        map.addMarker(new MarkerOptions()
                .position(hosp).title("Hospital")
                .icon(BitmapDescriptorFactory.fromBitmap(hospital)));


        map.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 16f));
    }
}
