package com.example.sih2023;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import de.hdodenhof.circleimageview.CircleImageView;

public class vehicle_info extends AppCompatActivity {
    private TextView Vechile_no_show,Click_to_customize;
    ImageView img;
    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
    private StorageReference reference= FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    FirebaseAuth mAuth;
    FirebaseUser user;
    ProgressBar progressBar;
    CircleImageView Update_Profile;
    TextView Name_Vehicle_info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vechile_info_one_vechile);
        Vechile_no_show=findViewById(R.id.Vechile_no_show);
        String vehicle_no= getIntent().getStringExtra("carnum");
        Vechile_no_show.setText(vehicle_no);
        Click_to_customize=findViewById(R.id.Click_to_customize);
        Update_Profile=findViewById(R.id.Vehicle_info_profile);
        Name_Vehicle_info=findViewById(R.id.Name_Vehicle_info);
        Name_Vehicle_info.setText(getIntent().getStringExtra("name"));
        img=findViewById(R.id.car_anim);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth= FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        String id=user.getUid();
        StorageReference reference2=FirebaseStorage.getInstance().getReference(id+"/"+vehicle_no+".jpg");
        try{
            File localfile=File.createTempFile("tempfile",".jpg");
            reference2.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap= BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    img.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageReference reference3= FirebaseStorage.getInstance().getReference(id+"/"+id+".jpg");
        try{
            File localfile=File.createTempFile("tempfile",".jpg");
            reference3.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap= BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    Update_Profile.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        Click_to_customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent,2);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode==RESULT_OK && data!=null){
            imageUri=data.getData();
            img.setImageURI(imageUri);
            if(imageUri!=null){
                uploadfirebase(imageUri);
            }

        }
    }
    private  void uploadfirebase(Uri uri){
        mAuth= FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        String id=user.getUid();
        String vehicle_no= getIntent().getStringExtra("carnum");
//        +getFileExtension(uri)
        StorageReference fileRef=reference.child(id).child(vehicle_no+".jpg");
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(vehicle_info.this,"Successfully Upload",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(vehicle_info.this,"Upload Failed",Toast.LENGTH_SHORT).show();
            }
        });

    }
//    private  String getFileExtension(Uri muri){
//        ContentResolver cr=getContentResolver();
//        MimeTypeMap mime= MimeTypeMap.getSingleton();
//        return mime.getExtensionFromMimeType(cr.getType(muri));
//
//    }
}
