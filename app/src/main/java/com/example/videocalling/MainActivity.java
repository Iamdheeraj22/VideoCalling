package com.example.videocalling;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videocalling.databinding.ActivityMainBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import Classes.Contacts;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    ImageView findPersonbtn;
    BottomNavigationView navView;
    RecyclerView myContactsList;
    private ActivityMainBinding binding;
    DatabaseReference contactsRef,userRef;
    FirebaseAuth mAuth;
    String currentUserId;
    String userName="",imageUrl="",status="";
    String callBy="";
    FloatingActionButton featuresBtn,exitBtn,restartBtn;
    Boolean isAllFloatingButtonVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.setting);

        mAuth=FirebaseAuth.getInstance();
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId=mAuth.getCurrentUser().getUid();
        navView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.setting) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }else if(itemId ==R.id.navigation_notifications){
                startActivity(new Intent(MainActivity.this,NotificationActivity.class));
                return true;
            }else if(itemId == R.id.Logout){
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
                alertDialog.setMessage("Do you want to logout your account?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alertDialog.create();
                alert.setTitle("Account Logout!");
                alert.show();
                return true;
            }else if(itemId == R.id.home){
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                return true;
            }
            return false;
        });
        findPersonbtn=findViewById(R.id.image_contacts_btn);
        myContactsList=findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPersonbtn.setOnClickListener(v -> {
            Intent find=new Intent(MainActivity.this,FindPersonActivity.class);
            startActivity(find);
        });

        //Display the features Button with animation
        featuresBtn.setOnClickListener(v -> {
            Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
            if (!isAllFloatingButtonVisible) {
                restartBtn.show();
                restartBtn.startAnimation(animation1);
                exitBtn.show();
                exitBtn.startAnimation(animation1);
                isAllFloatingButtonVisible = true;
                featuresBtn.setImageResource(R.drawable.minimize);
            } else {
                restartBtn.hide();
                exitBtn.hide();
                isAllFloatingButtonVisible = false;
                featuresBtn.setImageResource(R.drawable.maximize);
            }
        });
        //Restart the app
        restartBtn.setOnClickListener(v -> {
            restart();
        });
        //Exit the app
        exitBtn.setOnClickListener(v -> {
            exit();
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        setOnlineAndOfflineStatus();
        checkForReceivingCall();
        validateUser();
        FirebaseRecyclerOptions<Contacts> firebaseRecyclerOptions=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(firebaseRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts)
                    {
                        final String listUserId= getRef(i).getKey();
                        assert listUserId != null;
                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists())
                                {
                                    userName=snapshot.child("username").getValue().toString();
                                    imageUrl=snapshot.child("imageurl").getValue().toString();
                                    status=snapshot.child("status").getValue().toString();
                                    contactsViewHolder.contacts_username.setText(userName);

                                    if (imageUrl.equals("default")){
                                        contactsViewHolder.contacts_image.setImageResource(R.drawable.person);
                                    }else{
                                        Glide.with(MainActivity.this).load(imageUrl).into(contactsViewHolder.contacts_image);
                                    }
                                    //Picasso.get().load(imageUrl).into(contactsViewHolder.contacts_image);

                                   if(status.equals("Online")){
                                        contactsViewHolder.status_on.setVisibility(View.VISIBLE);
                                        contactsViewHolder.status_off.setVisibility(View.GONE);
                                    }else if (status.equals("Offline")){
                                        contactsViewHolder.status_off.setVisibility(View.VISIBLE);
                                        contactsViewHolder.status_on.setVisibility(View.GONE);
                                    }else{
                                       contactsViewHolder.status_off.setVisibility(View.VISIBLE);
                                       contactsViewHolder.status_on.setVisibility(View.GONE);
                                   }
                                }
                                contactsViewHolder.calling_btn.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                if(snapshot.exists())
                                                {
                                                    status=snapshot.child("status").getValue().toString();

                                                    if(status.equals("Online"))
                                                    {
                                                        Intent intent=new Intent(MainActivity.this,calling_activity.class);
                                                        intent.putExtra("visit_user_id",listUserId);
                                                        startActivity(intent);
                                                    }
                                                    if(status.equals("Offline"))
                                                    {
                                                        AlertDialog.Builder alertDialog=new AlertDialog.Builder(MainActivity.this);
                                                        alertDialog.setMessage("User currently offline")
                                                                .setCancelable(false)
                                                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        Toast.makeText(MainActivity.this, "Try after some time...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                        AlertDialog alert = alertDialog.create();
                                                        alert.setTitle("Alert!");
                                                        alert.show();
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this,error.toException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                        contactsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(MainActivity.this,Contact_activity.class);
                                intent.putExtra("contact",listUserId);
                                startActivity(intent);
                            }
                        });
                    }
                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_design,parent,false);
                        MainActivity.ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    private void setOnlineAndOfflineStatus(){
        userRef.child(currentUserId).child("status").setValue("Online")
                .addOnCompleteListener(task -> {
                    if(task.isComplete())
                    {
                        Toast.makeText(MainActivity.this, "You are online...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    //Set the RecyclerView
    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        ImageView contacts_image;
        TextView contacts_username;
        ImageButton calling_btn;
        RelativeLayout relativeLayout;
        CircleImageView status_on,status_off;
        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            contacts_image=itemView.findViewById(R.id.contacts_image);
            contacts_username=itemView.findViewById(R.id.contacts_username);
            relativeLayout=itemView.findViewById(R.id.contacts_cardView);
            calling_btn=itemView.findViewById(R.id.calling_btn);
            status_on=itemView.findViewById(R.id.status_on);
            status_off=itemView.findViewById(R.id.status_off);
        }
    }
    //Check the receiving Call
    private void checkForReceivingCall()
    {
        userRef.child(currentUserId).child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.hasChild("ringing"))
                        {
                            callBy=snapshot.child("ringing").getValue().toString();
                            Intent intent=new Intent(MainActivity.this,calling_activity.class);
                            intent.putExtra("visit_user_id",callBy);
                            startActivity(intent);
                        }
                    }@Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void validateUser(){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot){
                if(!snapshot.exists()){
                    Intent settingIntent=new Intent(MainActivity.this,ProfileActivity.class);
                    startActivity(settingIntent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
   @Override
    protected void onDestroy() {
        super.onDestroy();
        userRef.child(currentUserId).child("status").setValue("Offline")
                .addOnCompleteListener(task -> {
                    if(task.isComplete()){
                        Toast.makeText(MainActivity.this, "Thank you...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void exit() {
        finish();
    }
    private void restart()
    {
        finish();
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(getIntent());
            }
        },1000);
    }
    private void initViews()
    {
        featuresBtn=findViewById(R.id.features_app);
        restartBtn=findViewById(R.id.restart_app);
        exitBtn=findViewById(R.id.exit_app);
        isAllFloatingButtonVisible=false;
        restartBtn.hide();
        exitBtn.hide();
    }
}