package com.example.videocalling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    Button btn4,btn5,btn6;
    TextView forget_password;
    EditText email2,password2;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onStart() {
        super.onStart();
        AlertDialog.Builder alert=new AlertDialog.Builder(LoginActivity.this);
        alert.setMessage("If you are new user then first verify your email(Check your email)")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(LoginActivity.this,"Thank you",Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog=alert.create();
        alertDialog.setTitle("Notice");
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn4=findViewById(R.id.btn4);
        email2=findViewById(R.id.email2);
        password2=findViewById(R.id.password2);
        forget_password=findViewById(R.id.forget);
        firebaseAuth= FirebaseAuth.getInstance();
        btn5=findViewById(R.id.btn5);
        btn6=findViewById(R.id.btn6);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }
        });
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,ForgetActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email=email2.getText().toString();
                String txt_password=password2.getText().toString();
                if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    Toast.makeText(LoginActivity.this,"Please fill all required field!",Toast.LENGTH_SHORT).show();
                }else
                {
                    firebaseAuth.signInWithEmailAndPassword(txt_email,txt_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                if(firebaseAuth.getCurrentUser().isEmailVerified())
                                {
                                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }else {
                                    Toast.makeText(LoginActivity.this,"Please verify emailAddress",Toast.LENGTH_SHORT).show();
                                }
                            }else
                            {
                                Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}