package com.example.videocalling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Classes.Notification;
import Classes.Contacts;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference friendRequestRef,contactRef,userRef;
    FirebaseAuth mAuth;
    String currentUserId;
    NotificationManagerCompat notificationCompat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationCompat=NotificationManagerCompat.from(this);
        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactRef =FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        recyclerView=findViewById(R.id.Notification_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(friendRequestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,NotificationViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Contacts, NotificationViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull NotificationViewHolder holder, int i, @NonNull Contacts contacts)
                    {
                        holder.btn1.setVisibility(View.VISIBLE);
                        holder.btn2.setVisibility(View.VISIBLE);

                         final String listUserId=getRef(i).getKey();

                        DatabaseReference requestTypeRef=getRef(i).child("request_type").getRef();
                        requestTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists()){
                                    String type=snapshot.getValue().toString();
                                    if(type.equals("received")){
                                        holder.person_image.setVisibility(View.VISIBLE);
                                        assert listUserId != null;
                                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                if(snapshot.hasChild("imageurl")){
                                                    final String imagerUrl=snapshot.child("imageurl").getValue().toString();
                                                    if(!imagerUrl.equals("default")){
                                                        Glide.with(NotificationActivity.this).load(imagerUrl).into(holder.person_image);
                                                    }else{
                                                        holder.person_image.setImageResource(R.drawable.person);
                                                    }
                                                }
                                                final String fullname=snapshot.child("fullname").getValue().toString();
                                                final String username=snapshot.child("username").getValue().toString();
                                                createNotification1(fullname);
                                                holder.user_name.setText(username);
                                                holder.btn1.setOnClickListener(v -> contactRef.child(currentUserId).child(listUserId)
                                                        .child("Contact").setValue("Saved")
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    contactRef.child(listUserId).child(currentUserId)
                                                                            .child("Contact").setValue("Saved")
                                                                            .addOnCompleteListener(task1 -> {
                                                                                if(task1.isSuccessful())
                                                                                {
                                                                                    friendRequestRef.child(currentUserId).child(listUserId)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task1) {
                                                                                                    if(task1.isSuccessful())
                                                                                                    {
                                                                                                        friendRequestRef.child(listUserId).child(currentUserId)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(task11 -> {
                                                                                                                    if(task11.isSuccessful()){
                                                                                                                        Toast.makeText(NotificationActivity.this, "New Contact Saved....", Toast.LENGTH_SHORT).show();
                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }));
                                                holder.btn2.setOnClickListener(v -> friendRequestRef.child(currentUserId).child(listUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(listUserId).child(currentUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(task12 -> {
                                                                                if(task12.isSuccessful()){
                                                                                    Toast.makeText(NotificationActivity.this, "friend request deleted....", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }));
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }});
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design,parent,false);
                        NotificationViewHolder notificationViewHolder=new NotificationViewHolder(view);
                        return notificationViewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder
    {
        TextView user_name;
        Button btn1,btn2;
        ImageView person_image;
        RelativeLayout layout;
        public NotificationViewHolder(@NonNull View itemView)
        {
            super(itemView);
            user_name=itemView.findViewById(R.id.find_friend_username);
            btn1=itemView.findViewById(R.id.request_accept_btn);
            btn2=itemView.findViewById(R.id.request_delete_btn);
            person_image=itemView.findViewById(R.id.find_friend_image);
            layout=itemView.findViewById(R.id.cardView);
        }
    }
    public void createNotification1(String fullname)
    {
            android.app.Notification notification=new NotificationCompat.Builder(this, Notification.FRIEND_REQUEST_CHANNEL_ID)
                    .setSmallIcon(R.drawable.applogo)
                    .setContentTitle("Friend Request!")
                    .setContentText(fullname+" send friend request!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
        notificationCompat.notify(1,notification);
    }
}