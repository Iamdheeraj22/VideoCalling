package com.example.videocalling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    @Override
    protected void onStart() {
        super.onStart();
        // Check the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            Intent intent1 = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent1);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }
}