package com.specknet.pdiotapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.specknet.pdiotapp.utils.DatabaseHelper;

public class UserLogin extends AppCompatActivity {

    private TextView textViewWelcomeLogin, usernameDescription, passwordDescription;
    private Button submitButton, registrationButton;  // Include registrationButton here
    private EditText editTextPassword, editTextUsername;
    public static String currentUser = "";
    DatabaseHelper db = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        editTextPassword = findViewById(R.id.editTextTextPassword);
        editTextUsername = findViewById(R.id.editTextText2);

        submitButton = findViewById(R.id.submit);
        registrationButton = findViewById(R.id.registration_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                Boolean userExists = db.readUser(username, password);
                if(userExists){
                    // Show Success Message and Start LoginActivity
                    currentUser = username;
                    Toast.makeText(UserLogin.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserLogin.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    // Show Failure Message and Re-Load Registration
                    Toast.makeText(UserLogin.this, "Login failed! Please try again..", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserLogin.this, UserLogin.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLogin.this, UserRegistration.class);
                startActivity(intent);
                finish();
            }
        });

        // Add OnClickListener for the submitButton if needed
    }

    // Add this method to darken the input fields
    private void darkenInputFields() {
//        editTextPassword.setBackgroundTintList(getResources().getColorStateList(R.color.darkenedLoginColor));
//        editTextUsername.setBackgroundTintList(getResources().getColorStateList(R.color.darkenedLoginColor));
    }
}