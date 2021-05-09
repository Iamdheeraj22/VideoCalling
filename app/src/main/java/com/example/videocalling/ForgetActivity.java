package com.example.videocalling;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetActivity extends AppCompatActivity {
    EditText email;
    MaterialButton forget_password;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setVisibility(View.INVISIBLE);
        forget_password.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        email=findViewById(R.id.inputForgetEmail);
        forget_password=findViewById(R.id.buttonForgetPassword);
        progressBar=findViewById(R.id.signInProgressBar);

        firebaseAuth= FirebaseAuth.getInstance();
        forget_password.setOnClickListener(v -> forgetPassword());
    }

    private void forgetPassword()
    {
        progressBar.setVisibility(View.VISIBLE);
        forget_password.setVisibility(View.INVISIBLE);
        String txt_email=email.getText().toString();
        if(txt_email.equals("")){
            progressBar.setVisibility(View.INVISIBLE);
            forget_password.setVisibility(View.VISIBLE);
            Toast.makeText(ForgetActivity.this,"Please enter the your email!", Toast.LENGTH_SHORT).show();
        }else
        {
            firebaseAuth.sendPasswordResetEmail(txt_email).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(ForgetActivity.this,"Please check your email!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ForgetActivity.this,LoginActivity.class));
                }else {
                    progressBar.setVisibility(View.INVISIBLE);
                    forget_password.setVisibility(View.VISIBLE);
                    String error=task.getException().getMessage();
                    Toast.makeText(ForgetActivity.this,error,Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}