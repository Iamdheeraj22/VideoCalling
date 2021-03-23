package com.example.videocalling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    String receiverUserId="",receiverUserImage="",receiverUserName="";
    String senderUserId="",senderUserImage="",senderUserName="",checker="";
    DatabaseReference userRef;
    String callingId="",ringingId="";
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

        end_Call_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mediaPlayer.stop();
                checker="";
                CancelUSerCalling();
            }
        });
        accept_Call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                 final HashMap<String,Object> callingPickMap=new HashMap<>();
                 callingPickMap.put("picked","picked");

                 userRef.child(senderUserId).child("Ringing")
                         .updateChildren(callingPickMap)
                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 if(task.isComplete()){
                                     Intent intent=new Intent(calling_activity.this,videoCallActivity.class);
                                     startActivity(intent);
                                 }
                             }
                         });
            }
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
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(receiverUserId).exists()){
                    receiverUserImage=snapshot.child(receiverUserId).child("imageurl").getValue().toString();
                    receiverUserName=snapshot.child(receiverUserId).child("username").getValue().toString();

                    userName.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.person).into(profile_image);
                }
                if(snapshot.child(senderUserId).exists())
                {
                    senderUserImage=snapshot.child(senderUserId).child("imageurl").getValue().toString();
                    senderUserName=snapshot.child(senderUserId).child("username").getValue().toString();
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
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    userRef.child(receiverUserId).child("Ringing")
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                startActivity(new Intent(calling_activity.this,MainActivity.class));
                                finish();
                            }
                        }
                    });
                }else {
                    startActivity(new Intent(calling_activity.this,MainActivity.class));
                    finish();
                }
            }
        });
      /*  //sender side
        userRef.child(senderUserId).child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists() && snapshot.hasChild("calling"))
                        {
                            callingId=snapshot.child("calling").getValue().toString();
                            userRef.child(callingId).child("Ringing")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful()){
                                        userRef.child(senderUserId).child("Calling")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful()){
                                                    startActivity(new Intent(calling_activity.this,MainActivity.class));
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }else {
                            startActivity(new Intent(calling_activity.this,MainActivity.class));
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(calling_activity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //Receiver side
        userRef.child(senderUserId).child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists() && snapshot.hasChild("ringing"))
                        {
                            ringingId=snapshot.child("ringing").getValue().toString();
                            userRef.child(ringingId).child("Calling")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful()){
                                        userRef.child(senderUserId).child("Ringing")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful()){
                                                    startActivity(new Intent(calling_activity.this,MainActivity.class));
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }else {
                            startActivity(new Intent(calling_activity.this,MainActivity.class));
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(calling_activity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });*/
        userRef.child(senderUserId).child("Ringing")
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    userRef.child(receiverUserId).child("Calling")
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                startActivity(new Intent(calling_activity.this,MainActivity.class));
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }
}