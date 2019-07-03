package com.example.scheduleh;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private EditText textUsername;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    ProgressBar progressBar;
    private ImageView imageView;
    Uri uriProfileImage;
    String profileImageURL, oldUsername;
    private static final int CHOOSE_IMAGE = 101;
    private static final String TAG = "EditProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        textUsername = findViewById(R.id.newUsername);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();
        imageView = findViewById(R.id.imageview_account_profile);

        fetchUser();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        findViewById(R.id.updateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
                getData();
//                cancelButton();
            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButton();
            }
        });
    }

    public void cancelButton () {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    //working on the back button
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(this);
                parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(parentIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/
    private void fetchUser(){
        final FirebaseUser user = mAuth.getCurrentUser();

        if(user == null){
            Toast.makeText(EditProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }

        if(user != null) {
            //retrieve profile picture
            if (user.getPhotoUrl() != null) {
                //if not null, we will add the photo to the imageview of the profile screen
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .into(imageView);
            }
            //retrieve original name
            if (user.getDisplayName() != null) {
                oldUsername = user.getDisplayName();
                textUsername.setText((user.getDisplayName()));
            }
        }
    }

    private void updateUser() {
        FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();

        String displayName = textUsername.getText().toString();

        if (displayName.isEmpty()) {
            textUsername.setError("Display name is required");
            textUsername.requestFocus();
            return;
        }

        updateUsername();
    }

    private void updateUsername(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(textUsername.getText().toString())
//                    .setPhotoUri(Uri.parse(profileImageURL))
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        //update in users
        db.collection("users").document(user.getUid()).update("displayName", textUsername.getText().toString());
        /* TODO: Add the change to all profiles as well */
        /*
        //update in all collections
        db.collection("users")
                .whereEqualTo("displayName", textUsername.getText().toString())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                    }
                });*/

    }

    //getdata and update data are tgt and diff from the top few update function
    private void getData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }
                    Log.d(TAG, list.toString());
                    updateData(list); // *** new ***
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    private void updateData(ArrayList list) {

        // Get a new write batch
        final WriteBatch batch = db.batch();
        final FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();

        // Iterate through the list
        for (int k = 0; k < list.size(); k++) {

            // Update each list item
            final DocumentReference ref = db.collection("users").document((String) list.get(k));
            db.runTransaction(new Transaction.Function<Object>() {

                @Nullable
                @Override
                public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(ref);

                    //For each document
                    //This if changes all the displayName for the main user
                    if(snapshot.getString("email") == email) {

                        //checks events collection
                        db.collection("users").document(user.getUid()).collection("events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<String> UserEventList = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        UserEventList.add(document.getId());
                                    }

                                    Log.d(TAG, UserEventList.toString());

                                    for(int i = 0; i < UserEventList.size(); i++) {
                                        final DocumentReference UserEventRef = db.collection("users").document(user.getUid()).collection("events").document(UserEventList.get(i));
                                        db.runTransaction(new Transaction.Function<Object>() {
                                            @Nullable
                                            @Override
                                            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                                DocumentSnapshot snapshot = transaction.get(UserEventRef);
                                                if(snapshot.getString("UserId").equals(user.getUid())){
                                                    batch.update(UserEventRef, "displayName", textUsername.getText().toString());
                                                }
                                                return null;
                                            }
                                        });
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                        //checks user jios
                        db.collection("users").document(user.getUid()).collection("user jios").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<String> UserJioList = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        UserJioList.add(document.getId());
                                    }

                                    Log.d(TAG, UserJioList.toString());

                                    for(int i = 0; i < UserJioList.size(); i++) {
                                        DocumentReference UserJioRef = db.collection("users").document(user.getUid()).collection("user jios").document(UserJioList.get(i));
                                        batch.update(UserJioRef, "displayName", textUsername.getText().toString());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    }
                    return null;
                }
            });

//            batch.update(ref, "displayName", textUsername.getText().toString());

        }

        // Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Yay its all done in one go!
            }
        });

    }

    //we need to get the image from choose image, and to do this we need to override a method called onactivityresult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check requestcode is the same as choose_image code
        //from here we will get the selected image
        //resultCode == Result_OK means check if request code is ok and check if data != null
        //data.getData() != null means we have our image
        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            //this method returns a URI to the image and we will store this URI in the URI object
            uriProfileImage = data.getData();
            //from this URI profile image, we can get the selected image and we can display it to the imageView
            //getBitmap parameter 1: gets as a content resolver. Parameter 2:uri object
            //getBitmap can generate an exception and that is why we need to wrap it inside try and cache block and can do it by
            //alt enter then select around that try catch Multithreading
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                //now we can use bitmap to set as the image for the image view
                imageView.setImageBitmap(bitmap);

                //now we need upload onto database
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //user will tap on the image and will select image
    private void chooseImage(){
        //to open file
        Intent intent = new Intent();
        //set type of intent
        intent.setType("image/*");
        //we need to get content
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //create a file chooser and select tile for chooser, and start activity for result is the request code (can pass any constant)
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

    private void uploadImageToFirebaseStorage(){
        //storage reference
        //in get reference we will pass a path where we want ot put our image
        final StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("profilepics/"+System.currentTimeMillis() + ".jpg");
        //inside this storage we will put our file
        if(uriProfileImage != null){
            progressBar.setVisibility(View.VISIBLE);
            //to detect completion
            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            //here we will get that download URL of the image uploaded
                            profileImageURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            //display a toast so that we can get the error
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
