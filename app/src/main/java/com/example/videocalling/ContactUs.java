package com.example.videocalling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ContactUs extends AppCompatActivity
{
    DatabaseReference databaseReference;
    TextView textViewEmail,textViewPhone,textViewAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        initViews();
    }
    private void initViews()
    {
        textViewPhone=findViewById(R.id.contactLayout_item3);
        textViewEmail=findViewById(R.id.contactLayout_item5);
        textViewAddress=findViewById(R.id.contactLayout_item6);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDeveloperInformation();
    }

    // Get Developer information
    private void getDeveloperInformation()
    {
        databaseReference= FirebaseDatabase.getInstance().getReference().child("DeveloperInfo");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    // Set the email
                    String email=snapshot.child("email").getValue().toString();
                    textViewEmail.setText("Email us at :"+" "+email);

                    //Set the PhoneNumber
                    String phone=snapshot.child("phone").getValue().toString();
                    textViewPhone.setText("Call us at :"+" "+phone);

                    //Set the Address
                    String city=snapshot.child("city").getValue().toString();
                    String state=snapshot.child("state").getValue().toString();
                    String country=snapshot.child("country").getValue().toString();
                    String pincode=snapshot.child("pincode").getValue().toString();
                    textViewAddress.setText(city+"\n"+state+", "+country+"\n"+"Zipcode :"+pincode);
                }else
                {
                    Toast.makeText(ContactUs.this, "Doesn't have information about developer", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactUs.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}