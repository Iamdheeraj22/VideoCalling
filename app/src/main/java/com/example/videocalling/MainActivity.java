package com.example.videocalling;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.videocalling.CallingHistory.CallHistory;
import com.example.videocalling.CallingHistory.CallingDatabase;
import com.example.videocalling.Classes.Contacts;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    ImageView findPersonbtn;
    BottomNavigationView navView;
    TextView currentUserName;
    RecyclerView myContactsList;
    ActivityMainBinding binding;
    DatabaseReference contactsRef,userRef;
    SwipeRefreshLayout swipeRefreshLayout;
    FirebaseAuth mAuth;
    String currentUserId;
    String imageUrl="",status="",firstName="",lastName="";
    String callBy="";
    FloatingActionButton featuresBtn,exitBtn,callHistoryBtn;
    Boolean isAllFloatingButtonVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.setting);

        //Call the NavigationView
        NavigationView();

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
                callHistoryBtn.show();
                callHistoryBtn.startAnimation(animation1);
                exitBtn.show();
                exitBtn.startAnimation(animation1);
                isAllFloatingButtonVisible = true;
                featuresBtn.setImageResource(R.drawable.minimize);
            } else {
                callHistoryBtn.hide();
                exitBtn.hide();
                isAllFloatingButtonVisible = false;
                featuresBtn.setImageResource(R.drawable.maximize);
            }
        });
        //Restart the app
        callHistoryBtn.setOnClickListener(v -> {
            //restart();
            Intent intent=new Intent(MainActivity.this,videocall_history.class);
            startActivity(intent);
        });
        //Exit the app
        exitBtn.setOnClickListener(v -> {
            exit();
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        clearCallingRinging();
        setOnlineAndOfflineStatus();
        checkForReceivingCall();
        validateUser();
        getInfromationOfCurrentUser();
        getContacts();
    }

    //get the current user information
    private void getInfromationOfCurrentUser()
    {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName.setText("Hii "+snapshot.child("firstname").getValue().toString()+" "+snapshot.child("lastname").getValue().toString());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Check the online and offline status of the contact
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
        TextView contacts_username,textView_status;
        CircleImageView calling_btn;
        RelativeLayout relativeLayout;
        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textView_status=itemView.findViewById(R.id.status_activity);
            contacts_image=itemView.findViewById(R.id.contacts_image);
            contacts_username=itemView.findViewById(R.id.contacts_username);
            relativeLayout=itemView.findViewById(R.id.contacts_cardView);
            calling_btn=itemView.findViewById(R.id.calling_btn);
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

    //Check the user is valid or not
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

    // Destroy the app
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


    //exit from the app
    private void exit() {
        finish();
    }

    //Restart the app
    private void restart()
    {
        finish();
        Handler handler=new Handler();
        handler.postDelayed(() -> startActivity(getIntent()),1000);
    }

    //initialize the widgets
    private void initViews()
    {
        featuresBtn=findViewById(R.id.features_app);
        callHistoryBtn=findViewById(R.id.call_historyBtn);
        exitBtn=findViewById(R.id.exit_app);
        swipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);
        currentUserName=findViewById(R.id.userId_name);
        isAllFloatingButtonVisible=false;
        callHistoryBtn.hide();
        exitBtn.hide();
        swipeRefreshLayout.setOnRefreshListener(this::getContacts);
        mAuth=FirebaseAuth.getInstance();
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId=mAuth.getCurrentUser().getUid();
    }

    //Processing of the navigation View
    @SuppressLint("NonConstantResourceId")
    private void NavigationView()
    {
        navView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            switch (itemId) {
                case R.id.home:
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    return true;
                case R.id.setting:
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                case R.id.navigation_notifications:
                    startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                    return true;
                case R.id.Logout:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setMessage("Do you want to logout your account?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", (dialog, id) -> {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            })
                            .setNegativeButton("No", (dialog, id) -> {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            });
                    AlertDialog alert = alertDialog.create();
                    alert.setTitle("Account Logout!");
                    alert.show();
                    return true;
            }
            return false;
        });
    }

    //Get all Contacts from firebase database
    private void getContacts()
    {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseRecyclerOptions<Contacts> firebaseRecyclerOptions=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(contactsRef.child(currentUserId),Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(firebaseRecyclerOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts)
                    {
                        swipeRefreshLayout.setRefreshing(false);
                        final String listUserId= getRef(i).getKey();
                        assert listUserId != null;
                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists())
                                {
                                    imageUrl=snapshot.child("imageurl").getValue().toString();
                                    status=snapshot.child("status").getValue().toString();
                                    firstName=snapshot.child("firstname").getValue().toString();
                                    lastName=snapshot.child("lastname").getValue().toString();

                                    contactsViewHolder.contacts_username.setText(firstName+" "+lastName);

                                    if (imageUrl.equals("default")){
                                        contactsViewHolder.contacts_image.setImageResource(R.drawable.person);
                                    }else{
                                        Glide.with(MainActivity.this).load(imageUrl).into(contactsViewHolder.contacts_image);
                                    }
                                    //Picasso.get().load(imageUrl).into(contactsViewHolder.contacts_image);

                                    if(status.equals("Online")){
                                        contactsViewHolder.textView_status.setText("Online");
                                        contactsViewHolder.textView_status.setTextColor(Color.GREEN);
                                    }else if (status.equals("Offline")){
                                        contactsViewHolder.textView_status.setText("Offline");
                                        contactsViewHolder.textView_status.setTextColor(Color.BLACK);
                                    }else{
                                        contactsViewHolder.textView_status.setText("Offline");
                                        contactsViewHolder.textView_status.setTextColor(Color.BLACK);
                                    }
                                }
                                contactsViewHolder.calling_btn.setOnClickListener(v ->
                                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot1)
                                            {
                                                if(snapshot1.exists())
                                                {
                                                    status= snapshot1.child("status").getValue().toString();
                                                    if(status.equals("Online"))
                                                    {
                                                        addHistory(currentUserId,listUserId);
                                                        Intent intent=new Intent(MainActivity.this,calling_activity.class);
                                                        intent.putExtra("visit_user_id",listUserId);
                                                        startActivity(intent);
                                                    }
                                                    if(status.equals("Offline"))
                                                    {
                                                        AlertDialog.Builder alertDialog=new AlertDialog.Builder(MainActivity.this);
                                                        alertDialog.setMessage("User currently offline")
                                                                .setCancelable(false)
                                                                .setPositiveButton("Okay", (dialog, id) -> Toast.makeText(MainActivity.this, "Try after some time...", Toast.LENGTH_SHORT).show());
                                                        AlertDialog alert = alertDialog.create();
                                                        alert.setTitle("Alert!");
                                                        alert.show();
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        }));
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this,error.toException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                        contactsViewHolder.itemView.setOnClickListener(v -> {
                            Intent intent=new Intent(MainActivity.this,Contact_activity.class);
                            intent.putExtra("contact",listUserId);
                            startActivity(intent);
                        });
                    }
                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_design,parent,false);
                        return new ContactsViewHolder(view);
                    }
                };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //Add the calling history
    @SuppressLint("SimpleDateFormat")
    private void addHistory(String senderId, String receiverId)
    {
        Calendar calendar;
        SimpleDateFormat simpledateformat;
        calendar = Calendar.getInstance();
        simpledateformat = new SimpleDateFormat("dd-MM-yyyy");
        String Date = simpledateformat.format(calendar.getTime());
        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                //creating a task
                CallHistory history = new CallHistory();
                history.setReceiverid(receiverId);
                history.setDate(Date);
                //adding to database
                CallingDatabase.getInstance(getApplicationContext()).getAppDatabase()
                        .historyDao()
                        .insertTask(history);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Call Saved", Toast.LENGTH_LONG).show();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();
    }

    private void clearCallingRinging()
    {
            userRef.child(currentUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        userRef.child(currentUserId).child("Ringing").removeValue();
                    }else{
                        Toast.makeText(MainActivity.this, "Sorry user have not receive call from anyone..", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        userRef.child(currentUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    userRef.child(currentUserId).child("Calling").removeValue();
                }else{
                    Toast.makeText(MainActivity.this, "Sorry user not call to anyone..", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}