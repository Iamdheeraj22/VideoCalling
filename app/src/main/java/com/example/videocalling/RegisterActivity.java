package com.example.videocalling;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    Button btn3;
    EditText uname, email1, createpassword, confirmpassword;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn3 = findViewById(R.id.btn3);
        uname = findViewById(R.id.uname);
        email1 = findViewById(R.id.email1);
        progressDialog=new ProgressDialog(this);
        createpassword = findViewById(R.id.createpassword);
        confirmpassword = findViewById(R.id.confirmpassword);
        firebaseAuth = FirebaseAuth.getInstance();
        btn3.setOnClickListener(v -> {
            String txt_username = uname.getText().toString();
            String txt_email = email1.getText().toString();
            String create_password = createpassword.getText().toString();
            String confirm_password = createpassword.getText().toString();
            if (TextUtils.isEmpty(txt_username)) {
                Toast.makeText(RegisterActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(txt_email)) {
                Toast.makeText(RegisterActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(create_password) && TextUtils.isEmpty(confirm_password)) {
                Toast.makeText(RegisterActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else if (create_password.length() < 7) {
                Toast.makeText(RegisterActivity.this, "Password atleast 8 digits", Toast.LENGTH_SHORT).show();
            } else if (!confirm_password.equals(create_password)) {
                Toast.makeText(RegisterActivity.this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
            } else {
                RegisterUser(txt_username, txt_email, confirm_password);
                progressDialog.setMessage("Creating the new account ,Please wait few moments");
                progressDialog.show();
            }
        });
    }

    private void RegisterUser(String username, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                String UserId = firebaseUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", UserId);
                hashMap.put("username", username);
                hashMap.put("email", email);
                hashMap.put("imageurl", "default");
                hashMap.put("status", "Offline");
                hashMap.put("bio", "Available");
                //hashMap.put("search", username.toUpperCase());

                databaseReference.setValue(hashMap).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, task11.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}