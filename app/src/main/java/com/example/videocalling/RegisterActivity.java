package com.example.videocalling;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialButton btn3;
    EditText firstName,lastName, email1, createpassword, confirmpassword;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    TextView textView_SignIn;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        btn3.setOnClickListener(v -> {
            String txt_firstname = firstName.getText().toString();
            String txt_lastname=lastName.getText().toString();
            String txt_email = email1.getText().toString();
            String create_password = createpassword.getText().toString();
            String confirm_password = createpassword.getText().toString();
            if (TextUtils.isEmpty(txt_firstname)) {
                Toast.makeText(RegisterActivity.this, "Fill the blank", Toast.LENGTH_SHORT).show();
            }else if (TextUtils.isEmpty(txt_lastname)) {
                Toast.makeText(RegisterActivity.this, "Fill the blank", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(txt_email)) {
                Toast.makeText(RegisterActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(create_password) && TextUtils.isEmpty(confirm_password)) {
                Toast.makeText(RegisterActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else if (create_password.length() < 7) {
                Toast.makeText(RegisterActivity.this, "Password atleast 8 digits", Toast.LENGTH_SHORT).show();
            } else if (!confirm_password.equals(create_password)) {
                Toast.makeText(RegisterActivity.this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
            } else {
                RegisterUser(txt_firstname,txt_lastname, txt_email, confirm_password);
            }
        });
        textView_SignIn.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            firstName.setText("");
            lastName.setText("");
            email1.setText("");
            createpassword.setText("");
            confirmpassword.setText("");
            startActivity(intent);
        });
    }

    private void initViews()
    {
        btn3 = findViewById(R.id.buttonSignUp);
        firstName = findViewById(R.id.textFirstName);
        lastName=findViewById(R.id.textLastName);
        email1 = findViewById(R.id.textEmail);
        textView_SignIn=findViewById(R.id.text_SignIn);
        progressBar=findViewById(R.id.progressDialog);
        createpassword = findViewById(R.id.textPassword);
        confirmpassword = findViewById(R.id.textConfirmPassword);
        firebaseAuth = FirebaseAuth.getInstance();
    }
    private void RegisterUser(String firstName,String lastName, String email, String password) {
        btn3.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                String UserId = firebaseUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", UserId);
                hashMap.put("firstname", firstName);
                hashMap.put("lastname",lastName);
                hashMap.put("email", email);
                hashMap.put("imageurl","default");
                hashMap.put("status", "Offline");
                hashMap.put("bio", "Available");
                //hashMap.put("search", username.toUpperCase());

                databaseReference.setValue(hashMap).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                btn3.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegisterActivity.this, task11.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                btn3.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}