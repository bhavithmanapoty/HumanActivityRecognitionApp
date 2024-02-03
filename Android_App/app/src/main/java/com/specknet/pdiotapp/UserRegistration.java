package com.specknet.pdiotapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.specknet.pdiotapp.utils.DatabaseHelper;


public class UserRegistration extends AppCompatActivity {

    private TextView title, nameDescription, usernameDescriptor, passwordDescription;
    private EditText editTextName, editTextUsername, editTextPassword;
    private Button registrationButton;
    DatabaseHelper db = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);


        // Initialize views
        title = findViewById(R.id.title);
        nameDescription = findViewById(R.id.name_description);
        usernameDescriptor = findViewById(R.id.username_description);
        passwordDescription = findViewById(R.id.password_description);

        editTextName = findViewById(R.id.editTextName);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);

        registrationButton = findViewById(R.id.registration_button);

        // Set onClickListener for the registration button
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement your registration logic here
                String name = editTextName.getText().toString();
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                //Check is user already exists
                if(!db.checkUser(username)){
                    Boolean signupSuccess = db.insertUser(name, username, password);
                    if(signupSuccess){
                        // Show Success Message and Start LoginActivity
                        Toast.makeText(UserRegistration.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserRegistration.this, UserLogin.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        // Show Failure Message and Re-Load Registration
                        Toast.makeText(UserRegistration.this, "Signup failed! Please try again..", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserRegistration.this, UserRegistration.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else{
                    Toast.makeText(UserRegistration.this, "Username Already Exists. Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}