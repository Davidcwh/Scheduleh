package com.example.scheduleh;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Set;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private String m_Text;

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
        findViewById(R.id.forgetPassword).setOnClickListener(this);
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

    private void forgetPassword(){

        final FirebaseUser user = mAuth.getCurrentUser();
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Please enter your email below")
                .setView(input)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Text = input.getText().toString().trim();
                if (m_Text.isEmpty()) {
                    input.setError("Email is required");
                    input.requestFocus();
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(m_Text).matches()) {
                    input.setError("Invalid email provided, Please enter a valid email");
                    input.requestFocus();
                } else {
                    if (mAuth != null) {
                        Toast.makeText(LoginActivity.this, "Reset password email has been sent to " + m_Text, Toast.LENGTH_LONG).show();
                        mAuth.sendPasswordResetEmail(m_Text);
                        dialog.dismiss();
                    } else {
                        Log.w(" error ", " bad entry ");
                    }
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
            case R.id.forgetPassword:
                forgetPassword();
                break;
        }
    }
}
