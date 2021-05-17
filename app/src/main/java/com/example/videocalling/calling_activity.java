package com.example.videocalling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class calling_activity extends AppCompatActivity
{
    ImageView profile_image,accept_Call_btn,end_Call_btn;
    TextView userName;
    String receiverUserId="",receiverUserImage="",receiverUserfirstName="",receiverUserLastName="";
    String senderUserId="",senderUserImage="",senderUserFirstName="",checker="",senderUserLastName="";
    DatabaseReference userRef;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        profile_image=findViewById(R.id.calling_user_image);
        accept_Call_btn=findViewById(R.id.make_call);
        end_Call_btn=findViewById(R.id.end_call);
        userName=findViewById(R.id.calling_user_name);

        mediaPlayer=MediaPlayer.create(this,R.raw.ringtone);

        senderUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        end_Call_btn.setOnClickListener(v -> {
            mediaPlayer.stop();
            checker="";
            CancelUSerCalling();
        });
        accept_Call_btn.setOnClickListener(v -> {
            mediaPlayer.stop();
            final HashMap<String,Object> callingPickMap=new HashMap<>();
            callingPickMap.put("picked","picked");

            userRef.child(senderUserId).child("Ringing")
                    .updateChildren(callingPickMap)
                    .addOnCompleteListener(task -> {
                        if(task.isComplete()){
                            Intent intent=new Intent(calling_activity.this,videoCallActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        });
        getAndSetReceiverInformation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.stop();
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing"))
                {
                    mediaPlayer.start();
                    final HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("calling",receiverUserId);

                    userRef.child(senderUserId).child("Calling")
                            .updateChildren(hashMap)
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful())
                                {
                                    final HashMap<String,Object> hashMap2=new HashMap<>();
                                    hashMap2.put("ringing",senderUserId);
                                    userRef.child(receiverUserId).child("Ringing")
                                            .updateChildren(hashMap2);
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(calling_activity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.child(senderUserId).hasChild("Ringing") && !snapshot.child(senderUserId).hasChild("Calling"))
                {
                    accept_Call_btn.setVisibility(View.VISIBLE);
                }
                if(snapshot.child(receiverUserId).child("Ringing").hasChild("picked"))
                {
                    Intent intent=new Intent(calling_activity.this,videoCallActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(calling_activity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAndSetReceiverInformation()
    {
        userRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(receiverUserId).exists()){
                    receiverUserImage=snapshot.child(receiverUserId).child("imageurl").getValue().toString();
                    receiverUserfirstName=snapshot.child(receiverUserId).child("firstname").getValue().toString();
                    receiverUserLastName=snapshot.child(receiverUserId).child("firstname").getValue().toString();

                    userName.setText(receiverUserfirstName+" "+receiverUserLastName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.person).into(profile_image);
                }
                if(snapshot.child(senderUserId).exists())
                {
                    senderUserImage=snapshot.child(senderUserId).child("imageurl").getValue().toString();
                    senderUserFirstName=snapshot.child(senderUserId).child("firstname").getValue().toString();
                    senderUserLastName=snapshot.child(senderUserId).child("lastname").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(calling_activity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void CancelUSerCalling()
    {
        userRef.child(senderUserId).child("Calling")
                .removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                userRef.child(receiverUserId).child("Ringing")
                        .removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        startActivity(new Intent(calling_activity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        });
    }
}