package com.example.videocalling;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    EditText edit_bio,edit_name;
    Button info_submit;
    ImageView imageView1;
    private static final int galleryPick=1;
    Uri ImageUri;
    StorageReference UserProfileRef;
    String downloadUri;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        edit_bio=findViewById(R.id.edit_bio);
        edit_name=findViewById(R.id.edit_name);
        imageView1=findViewById(R.id.imageview1);
        info_submit=findViewById(R.id.info_submit);
        progressDialog=new ProgressDialog(this);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        imageView1.setOnClickListener(v -> {
            Intent galleryIntent=new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,galleryPick);
        });
        info_submit.setOnClickListener(v -> saveUserData());
        RetrieveUserData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        ImageUri=data.getData();
        imageView1.setImageURI(ImageUri);
    }

    private void saveUserData()
    {
        String name=edit_name.getText().toString();
        String bio=edit_bio.getText().toString();

        if(ImageUri.equals("default"))
        {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("imageurl"))
                    {
                        saveTheInfoWithoutImage();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    Toast.makeText(ProfileActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else if(name.equals("")){
            Toast.makeText(this, "Username is mandatory!", Toast.LENGTH_SHORT).show();
        }else if(bio.equals("")){
            Toast.makeText(this, "Bio is mandatory!", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            final StorageReference filePath=
                    UserProfileRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask=filePath.putFile(ImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    downloadUri=filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        downloadUri=task.getResult().toString();

                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("id",FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("username",name);
                        hashMap.put("about",bio);
                        hashMap.put("imageurl",downloadUri);

                        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(hashMap)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful())
                                    {
                                        Toast.makeText(ProfileActivity.this,"Information Successfully Update!",Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                    }
                }
            });
        }
    }

    private void saveTheInfoWithoutImage()
    {
        String name=edit_name.getText().toString();
        String bio=edit_bio.getText().toString();

        progressDialog.setTitle("Account Settings");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("id",FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("username",name);
        hashMap.put("about",bio);
        hashMap.put("imageurl",downloadUri);

        if(name.equals("")){
            Toast.makeText(this, "Username is mandatory!", Toast.LENGTH_SHORT).show();
        }else if(bio.equals("")){
            Toast.makeText(this, "Bio is mandatory!", Toast.LENGTH_SHORT).show();
        }else {
            databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(hashMap)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ProfileActivity.this,"Information Successfully Update!",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        }
    }
    private void RetrieveUserData()
    {
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String image=snapshot.child("imageurl").getValue().toString();
                            String name=snapshot.child("username").getValue().toString();
                            String bio=snapshot.child("about").getValue().toString();

                            edit_name.setText(name);
                            edit_bio.setText(bio);
                                Picasso.get().load(image).placeholder(R.drawable.person).into(imageView1);

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}