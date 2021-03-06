package com.example.videocalling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class UserProfileActivity extends AppCompatActivity {

    ImageView imageView;
    Button btn1,btn2;
    TextView textView;
    String receiverUserId="",receiverUserName="",receiverUserImage="";
    FirebaseAuth mAuth;
    String senderUserId;
    String currentState="new";
    DatabaseReference friendRequestRef,contactsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        receiverUserName=getIntent().getExtras().get("profile_name").toString();
        receiverUserImage=getIntent().getExtras().get("profile_image").toString();
        Picasso.get().load(receiverUserImage).into(imageView);
        textView.setText(receiverUserName);

        checkUser();
        manageButtonClickEvent();
    }

    private void checkUser()
    {
        if(senderUserId.equals(receiverUserId)) {
            btn1.setVisibility(View.GONE);
        }
    }

    //Initialize the all widget
    private void initViews()
    {
        imageView=findViewById(R.id.user_profile_image);
        textView=findViewById(R.id.user_profile_name);
        btn1=findViewById(R.id.user_profile_btn1);
        btn2=findViewById(R.id.user_profile_btn2);
        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();
        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
    }
    //Manage the Button Click event
    private void manageButtonClickEvent()
    {
        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserId)){
                    String requestType=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(requestType.equals("sent")){
                        currentState="request_sent";
                        btn1.setText("Cancel Friend Request");
                    }else if(requestType.equals("received")){
                        currentState="request_received";
                        btn1.setText("Accept Friend Request");

                        btn2.setVisibility(View.VISIBLE);
                        btn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }
                else {
                    contactsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(receiverUserId)){
                                        currentState="friends";
                                        btn1.setText("Delete Contact");
                                    }
                                    else {
                                        currentState="new";
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(currentState.equals(receiverUserId)){
            btn1.setVisibility(View.GONE);
        }else{
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                            if(currentState.equals("new")){
                                sendFriendRequest();
                            }
                            if(currentState.equals("request_sent")){
                                CancelFriendRequest();
                            }
                            if(currentState.equals("request_received")){
                                    AcceptFriendRequest();
                            }
                            if(currentState.equals("request_sent")){
                                CancelFriendRequest();
                            }
                    }
            });
        }
    }
    // Send the friend request
    private void sendFriendRequest()
    {
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            currentState="request_sent";
                                            btn1.setText("Cancel Friend Request");
                                            Toast.makeText(UserProfileActivity.this, "Friend Request Sent....", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
    }
    //Cancel the Friend Request
    private void CancelFriendRequest(){
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                currentState="new";
                                                btn1.setText("Add Friend");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    private void AcceptFriendRequest(){
        contactsRef.child(senderUserId).child(receiverUserId)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                friendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @SuppressLint("SetTextI18n")
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        currentState="friends";
                                                                                        btn1.setText("Delete Contact");

                                                                                        btn2.setVisibility(View.GONE);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}