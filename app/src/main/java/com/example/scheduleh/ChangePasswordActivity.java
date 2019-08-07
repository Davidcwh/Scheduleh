package com.example.scheduleh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText textOldPassword, textNewPassword, textConfirmPassword;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ImageView imageView = findViewById(R.id.imageview_account_profile_password);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            //retrieve profile picture
            if (user.getPhotoUrl() != null) {
                //if not null, we will add the photo to the imageview of the profile screen
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .into(imageView);
            }
        }
        textOldPassword = findViewById(R.id.oldPassword);
        textNewPassword = findViewById(R.id.newPassword);
        textConfirmPassword = findViewById(R.id.ComfirmNewPassword);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.updateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });
        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { cancelButton();
            }
        });
    }

    private void cancelButton () {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateUser() {
        FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();

        String oldPassword = textOldPassword.getText().toString().trim();
        String newPassword = textNewPassword.getText().toString().trim();
        String comfirmPassword = textConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty()) {
            textOldPassword.setError("Password is required");
            textOldPassword.requestFocus();
            return;
        }
        if (oldPassword.equals(newPassword)) {
            textNewPassword.setError("New password is the same as your old password. Please type again");
            textNewPassword.requestFocus();
            return;
        }
        if (newPassword.isEmpty()) {
            textNewPassword.setError("Please enter a new password");
            textNewPassword.requestFocus();
            return;
        }
        if (comfirmPassword.isEmpty()) {
            textConfirmPassword.setError("Please enter confirmation password");
            textNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(comfirmPassword)) {
            textConfirmPassword.setError("Passwords do not match. Please type again");
            textConfirmPassword.requestFocus();
            return;
        }
        if (oldPassword.length() < 6) {
            textOldPassword.setError("Minimum password length is 6");
            textOldPassword.requestFocus();
            return;
        }
        else {
            mAuth.signInWithEmailAndPassword(email, oldPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        textOldPassword.setError("Old password is wrong");
                        textOldPassword.requestFocus();
                        return;
                    } else {
                        changePassword();
                    }
                }
            });
        }
        changePassword();
    }

    private void changePassword(){
        final FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        AuthCredential Credential = EmailAuthProvider.getCredential(email,textOldPassword.getText().toString());
        user.reauthenticate(Credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    user.updatePassword(textNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
//                                Toast.makeText(ChangePasswordActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> removeTokenId = new HashMap<>();
                                removeTokenId.put("tokenId", "");
                                db.collection("users").document(mAuth.getCurrentUser().getUid()).update(removeTokenId);

                                FirebaseAuth.getInstance().signOut();
                                finish();
                                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}

