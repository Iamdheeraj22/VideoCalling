package com.example.videocalling;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    TextView text_name,text_bio,text_email;
    CircleImageView imageView1;
    private StorageTask uploadTask;
    private static final int IMAGE_REQUEST=1;
    Uri ImageUri;
    String currentUser;
    Button btn1;
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
        text_email=findViewById(R.id.text_email);
        btn1=findViewById(R.id.contactUs);
        progressDialog=new ProgressDialog(this);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        currentUser=FirebaseAuth.getInstance().getCurrentUser().getUid();

        imageView1.setOnClickListener(v -> {

            PopupMenu popupMenu=new PopupMenu(ProfileActivity.this,imageView1);
            popupMenu.getMenuInflater().inflate(R.menu.profile_change,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.changeImage) {
                    final CharSequence[] options = {"Choose from Gallery","Remove Image","Cancel" };
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
                                dialog.dismiss();
                            }else if(options[which].equals("Remove Image")){
                                removeImage();
                            }
                        }
                    });
                    builder.show();
                    return true;}
                return false;});
            popupMenu.show();
        });
        text_bio.setOnClickListener(v -> {
            changeYourBio();
        });
        btn1.setOnClickListener(v -> {
            Intent intent=new Intent(ProfileActivity.this,ContactUs.class);
            startActivity(intent);
        });
        RetrieveUserData();
    }

    private void removeImage()
    {
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser).child("imageurl");
        databaseReference.setValue("default").addOnCompleteListener(task -> {
            if(task.isComplete()){
                imageView1.setImageResource(R.drawable.person);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(ProfileActivity.this,
                        e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    //Change your bio
    private void changeYourBio()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.about_change, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String bio;
            EditText editText = customLayout.findViewById(R.id.editText_bio);
            bio=editText.getText().toString();
            databaseReference.child(currentUser).child("about").setValue(bio)
                    .addOnCompleteListener(task -> {
                        if(task.isComplete()){
                            text_bio.setText(bio);
                            Toast.makeText(ProfileActivity.this, "Bio updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e ->
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }).setNegativeButton("Cancel",null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Todo :- Change the user profile Image
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
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return file.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
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
                else{
                    Toast.makeText(ProfileActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show());
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
            }else{
                uploadImage();
            }
        }
    }

    private void RetrieveUserData()
    {
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String firstname= snapshot.child("firstname").getValue().toString();
                            String lastname=snapshot.child("lastname").getValue().toString();
                            String image=snapshot.child("imageurl").getValue().toString();
                            String email=snapshot.child("email").getValue().toString();
                            String bio=snapshot.child("bio").getValue().toString();
                            if(image.equals("default")){
                                imageView1.setImageResource(R.drawable.person);
                            }else{
                                Glide.with(ProfileActivity.this).load(image).into(imageView1);
                            }
                            text_name.setText(firstname+" "+lastname);
                            text_bio.setText(bio);
                            text_email.setText(email);
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