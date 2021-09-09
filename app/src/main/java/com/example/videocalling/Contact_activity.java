package com.example.videocalling;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class Contact_activity extends AppCompatActivity
{

    CircleImageView circleImageView;
    TextView contactUsername,contactAbout,error;
    String receiverId="";
    DatabaseReference databaseReference;
    String firstName,lastName,image,about;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initViews();

        receiverId=getIntent().getExtras().get("contact").toString();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

        //Get the contact user information
        getAndSetinformation();

    }

    private void getAndSetinformation()
    {
        databaseReference.child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            firstName=snapshot.child("firstname").getValue().toString();
                            lastName=snapshot.child("lastname").getValue().toString();
                            image=snapshot.child("imageurl").getValue().toString();
                            about=snapshot.child("bio").getValue().toString();

                            contactUsername.setText(firstName+" "+lastName);
                        }
                        if(image.equals("default")){
                            circleImageView.setImageResource(R.drawable.person);
                        }else{
                            Glide.with(Contact_activity.this).load(image).into(circleImageView);
                        }
                        contactAbout.setText(about);
                        //Picasso.get().load(image).placeholder(R.drawable.person).into(imageView1);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        error.setVisibility(View.VISIBLE);
                        circleImageView.setVisibility(View.VISIBLE);
                        contactUsername.setVisibility(View.VISIBLE);
                        contactAbout.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_options, menu);
        return true;
    }

    private void initViews() {
        circleImageView=findViewById(R.id.contact_user_image);
        contactUsername=findViewById(R.id.contact_user_userName);
        contactAbout=findViewById(R.id.contact_user_about);
        error=findViewById(R.id.error_textview);
    }
}