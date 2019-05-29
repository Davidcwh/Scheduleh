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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText LogInEmail, LogInPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        LogInEmail = findViewById(R.id.LogInEmailEditText);
        LogInPassword = findViewById(R.id.LogInPasswordEditText);

        findViewById(R.id.LogInLinkToSignUp).setOnClickListener(this);
        findViewById(R.id.LogInButton).setOnClickListener(this);
    }

    private void userLogIn() {
        String email = LogInEmail.getText().toString().trim();
        String password = LogInPassword.getText().toString().trim();


        // Email and Password validation checks
        if (email.isEmpty()) {
            LogInEmail.setError("Email is required");
            LogInEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            LogInEmail.setError("Invalid email provided, Please enter a valid email");
            LogInEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            LogInPassword.setError("Password is required");
            LogInPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            LogInPassword.setError("Minimum password length is 6");
            LogInPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    finish();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.LogInLinkToSignUp:
                finish();
                startActivity(new Intent(this, SignUpActivity.class));
                break;

            case R.id.LogInButton:
                userLogIn();
                break;
        }
    }
}
