package com.example.videocalling;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    TextView text_name,text_bio;
    Button update_bio;
    CircleImageView imageView1;
    private StorageTask uploadTask;
    private static final int IMAGE_REQUEST=1;
    Uri ImageUri;
    String currentUser;
    StorageReference UserProfileRef;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        text_bio=findViewById(R.id.text_bio);
        text_name=findViewById(R.id.text_name);
        imageView1=findViewById(R.id.imageview1);
        update_bio=findViewById(R.id.info_submit);
        progressDialog=new ProgressDialog(this);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        currentUser=FirebaseAuth.getInstance().getCurrentUser().getUid();
        imageView1.setOnClickListener(v -> {
           /* Intent galleryIntent=new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,galleryPick);*/

            PopupMenu popupMenu=new PopupMenu(ProfileActivity.this,imageView1);
            popupMenu.getMenuInflater().inflate(R.menu.profile_change,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.changeImage) {
                        final CharSequence[] options = {"Choose from Gallery","Cancel" };
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setTitle("Add Photo!");
                        builder.setItems(options,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                if(options[which].equals("Choose from Gallery")){
                                    openImage();
                                }
                                else if(options[which].equals("Cancel")){
                                    dialog.dismiss(); }}
                        });
                        builder.show();
                        return true;}
                    return false;}
            });
            popupMenu.show();
        });
        update_bio.setOnClickListener(v -> {
            changeYourBio();
        });
        RetrieveUserData();
    }

    private void changeYourBio()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Write your bio!");
        final View customLayout = getLayoutInflater().inflate(R.layout.about_change, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String bio;
            EditText editText = customLayout.findViewById(R.id.editText);
            bio=editText.getText().toString();
            databaseReference.child(currentUser).child("about").setValue(bio)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isComplete()){
                                text_bio.setText(bio);
                                Toast.makeText(ProfileActivity.this, "Bio updated...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).setNegativeButton("Cancel",null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openImage()
    {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=ProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage(){
        final ProgressDialog progressDialog=new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Uploading...");
        if(ImageUri!=null){
            final StorageReference file=UserProfileRef.child(System.currentTimeMillis()+"."+getFileExtension(ImageUri));
            uploadTask=file.putFile(ImageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if(!task.isSuccessful())
                {
                    throw task.getException();
                }
                return file.getDownloadUrl();
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if(task.isSuccessful()){
                        Uri downloaduri=task.getResult();
                        assert downloaduri != null;
                        String mUri= downloaduri.toString();

                        databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        HashMap<String,Object> map=new HashMap<>();
                        map.put("imageurl",mUri);
                        databaseReference.updateChildren(map);

                        progressDialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(ProfileActivity.this,"no image selected",Toast.LENGTH_SHORT).show();
        }
        progressDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData() !=null){
            ImageUri=data.getData();
            if(uploadTask!=null && uploadTask.isInProgress()){
                Toast.makeText(ProfileActivity.this,"Upload in Progress",Toast.LENGTH_SHORT).show();
            }else
            {
                uploadImage();
            }
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
                            if(image.equals("default"))
                            {
                                imageView1.setImageResource(R.drawable.person);
                            }else{
                                Glide.with(ProfileActivity.this).load(image).into(imageView1);
                            }
                            text_name.setText(name);
                            text_bio.setText(bio);
                                //Picasso.get().load(image).placeholder(R.drawable.person).into(imageView1);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}