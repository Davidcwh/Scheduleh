package com.example.scheduleh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    EditText SignUpName, SignUpEmail, SignUpPassword;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SignUpName = findViewById(R.id.SigUpNameEditText);
        SignUpEmail = findViewById(R.id.SignUpEmailEditText);
        SignUpPassword = findViewById(R.id.SignUpPasswordEditText);
        progressBar = findViewById(R.id.SignUpProgressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.SignUpCreateAccountButton).setOnClickListener(this);
        findViewById(R.id.SignUpLinkToLogIn).setOnClickListener(this);
    }


    private void registerUser() {
        final String displayName = SignUpName.getText().toString();
        String email = SignUpEmail.getText().toString().trim();
        String password = SignUpPassword.getText().toString().trim();


        // Email, Password and display name validation checks
        if (displayName.isEmpty()) {
            SignUpName.setError("Display name is required");
            SignUpName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            SignUpEmail.setError("Email is required");
            SignUpEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SignUpEmail.setError("Invalid email provided, Please enter a valid email");
            SignUpEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            SignUpPassword.setError("Password is required");
            SignUpPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            SignUpPassword.setError("Minimum password length is 6");
            SignUpPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "User Registration Successful! Logging you in...", Toast.LENGTH_SHORT).show();

                    FirebaseUser user = mAuth.getCurrentUser();
                    UserProfileChangeRequest updateProfile = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                    user.updateProfile(updateProfile);

                    db.collection("users").document(user.getUid()).set(user);
                    db.collection("users").document(user.getUid()).update("displayName", displayName);

                    finish();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                } else if (task.getException() instanceof FirebaseAuthUserCollisionException){
                    Toast.makeText(getApplicationContext(), "Registration failed: email already registered", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.SignUpCreateAccountButton:
                registerUser();
                break;

            case R.id.SignUpLinkToLogIn:
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }
}
