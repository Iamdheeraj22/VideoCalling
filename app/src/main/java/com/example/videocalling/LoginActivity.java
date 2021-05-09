package com.example.videocalling;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextView text_SignUp;
    EditText email,password;
    MaterialButton signInButton;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setVisibility(View.INVISIBLE);
        AlertDialog.Builder alert=new AlertDialog.Builder(LoginActivity.this);
        alert.setMessage("If you are new user then first verify your email(Check your email)")
                .setPositiveButton("Okay", (dialog, which) -> Toast.makeText(LoginActivity.this,"Thank you",Toast.LENGTH_SHORT).show());
        AlertDialog alertDialog=alert.create();
        alertDialog.setTitle("Notice");
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton=findViewById(R.id.buttonSignIn);
        email=findViewById(R.id.inputEmail);
        password=findViewById(R.id.inputPassword);
        text_SignUp=findViewById(R.id.textSignUp);
        progressBar=findViewById(R.id.signInProgressBar);
        firebaseAuth= FirebaseAuth.getInstance();

        text_SignUp.setOnClickListener(v->{
            sendSignInActivity();
            email.setText("");
            password.setText("");
        });

        signInButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
            String txt_email=email.getText().toString();
            String txt_password=password.getText().toString();
            if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                Toast.makeText(LoginActivity.this,"Please fill all required field!",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                signInButton.setVisibility(View.VISIBLE);
            }else
            {
                progressBar.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.INVISIBLE);
                firebaseAuth.signInWithEmailAndPassword(txt_email,txt_password).addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        if(firebaseAuth.getCurrentUser().isEmailVerified())
                        {
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else {
                            progressBar.setVisibility(View.INVISIBLE);
                            signInButton.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this,"Please verify emailAddress",Toast.LENGTH_SHORT).show();
                        }
                    }else
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        signInButton.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void sendSignInActivity()
    {
        Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        email.setText("");
        password.setText("");
        startActivity(intent);
    }
}