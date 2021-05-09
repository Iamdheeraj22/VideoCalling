package com.example.videocalling;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import com.example.videocalling.Classes.Contacts;

public class  FindPersonActivity extends AppCompatActivity {

    EditText search_find_friend;
    RecyclerView find_friends_list;
    String str="";
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_person);

        search_find_friend=findViewById(R.id.search_find_friend);
        find_friends_list=findViewById(R.id.find_friends_list);
        find_friends_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        search_find_friend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(search_find_friend.getText().toString().equals("")){
                    Toast.makeText(FindPersonActivity.this, "Please enter the username!", Toast.LENGTH_SHORT).show();
                }else{
                    str=s.toString();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> contactsFirebaseRecyclerOptions=null;
        if(str.equals(""))
        {
            contactsFirebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(databaseReference,Contacts.class)
                    .build();
        }
        if(!str.equals("")){
            contactsFirebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(databaseReference.orderByChild("firstname")
                                    .startAt(str)
                                        .endAt(str+"\uf8ff")
                            ,Contacts.class)
                    .build();
        }

        FirebaseRecyclerAdapter<Contacts,FindFriendHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Contacts, FindFriendHolder>(Objects.requireNonNull(contactsFirebaseRecyclerOptions)) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendHolder findFriendHolder, int position, @NonNull Contacts model)
                    {
                        findFriendHolder.contacts_username.setText(model.getFirstname()+" "+model.getLastname());
                        if(model.getImageurl().equals("default")){
                            findFriendHolder.contacts_image.setImageResource(R.drawable.person);
                        }else {
                            Glide.with(FindPersonActivity.this).load(model.getImageurl()).into(findFriendHolder.contacts_image);
                        }
                        findFriendHolder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                String visit_user_id=getRef(position).getKey();
                                Intent intent=new Intent(FindPersonActivity.this,UserProfileActivity.class);
                                intent.putExtra("visit_user_id",visit_user_id);
                                intent.putExtra("profile_image",model.getImageurl());
                                intent.putExtra("profile_name",model.getFirstname()+" "+model.getLastname());
                                startActivity(intent);
                            }
                        });
                    }
                    @NonNull
                    @Override
                    public FindFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_design,parent,false);
                        FindFriendHolder viewHolder=new FindFriendHolder(view);
                        return viewHolder;
                    }
                };

        find_friends_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendHolder extends RecyclerView.ViewHolder
    {
        ImageView contacts_image;
        TextView contacts_username;
        ImageButton calling_btn;
        RelativeLayout relativeLayout;
        public FindFriendHolder(@NonNull View itemView)
        {
            super(itemView);
            contacts_image=itemView.findViewById(R.id.contacts_image);
            contacts_username=itemView.findViewById(R.id.contacts_username);
            relativeLayout=itemView.findViewById(R.id.contacts_cardView);
            calling_btn=itemView.findViewById(R.id.calling_btn);
            calling_btn.setVisibility(View.GONE);
        }
    }
}