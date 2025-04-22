package com.example.sih2023;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class editprofile extends AppCompatActivity {
    EditText name,number;
    private Uri imageUri;
    TextView save,email;
    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView Edit_profile_text;
    CircleImageView Update_Profile;
    private StorageReference reference= FirebaseStorage.getInstance().getReference();

    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://sih2024-ac37a-default-rtdb.firebaseio.com/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        name=findViewById(R.id.profile_name);
        number=findViewById(R.id.profile_number);
        save=findViewById(R.id.Save);
        email=findViewById(R.id.profile_email);
        Edit_profile_text=findViewById(R.id.Edit_profile_text);
        Update_Profile=findViewById(R.id.Update_Profile);

        mAuth= FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        String id=user.getUid();
        StorageReference reference2= FirebaseStorage.getInstance().getReference(id+"/"+id+".jpg");
        try{
            File localfile=File.createTempFile("tempfile",".jpg");
            reference2.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap= BitmapFactory.decodeFile(localfile.getAbsolutePath());
                    Update_Profile.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        String m=user.getEmail();

        Edit_profile_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent,2);
            }
        });


        Log.i("mail",m);
        name.setText(m);
        getdata(id,name,number,email);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num=number.getText().toString();
                String nam=name.getText().toString();





                if(!TextUtils.isEmpty(num) && !TextUtils.isEmpty(nam)){
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            databaseReference.child("users").child(id).child("details").child("fullname").setValue(nam);
                            databaseReference.child("users").child(id).child("details").child("mob").setValue(num);

                            Toast.makeText(editprofile.this,"Updated Sucessfully",Toast.LENGTH_SHORT).show();
                            finish();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });


    }
    public  void getdata(String id, EditText name, EditText number, TextView email){
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nam=snapshot.child(id).child("details").child("fullname").getValue(String.class);
                String num=snapshot.child(id).child("details").child("mob").getValue(String.class);
                String ema=snapshot.child(id).child("details").child("email").getValue(String.class);
                name.setText(nam);
                number.setText(num);
                email.setText(ema);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode==RESULT_OK && data!=null){
            imageUri=data.getData();
            Update_Profile.setImageURI(imageUri);
            if(imageUri!=null){
                uploadfirebase(imageUri);
            }

        }
    }
    private  void uploadfirebase(Uri uri){
        mAuth= FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        String id=user.getUid();
//        +getFileExtension(uri)
        StorageReference fileRef=reference.child(id).child(id+".jpg");
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Toast.makeText(editprofile.this,"Successfully Upload",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(editprofile.this,"Upload Failed",Toast.LENGTH_SHORT).show();
            }
        });

    }

}
