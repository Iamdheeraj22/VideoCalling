package com.example.videocalling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    Handler handler;
    @Override
    protected void onStart() {
        super.onStart();
        // Check the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            handler.postDelayed(() -> {
                Intent intent1 = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent1);
                finish();
            },3000);
        }else {
           startActivity(new Intent(getApplicationContext(),LoginActivity.class));
           finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        handler =new Handler();
    }
}