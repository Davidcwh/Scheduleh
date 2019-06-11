package com.example.scheduleh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class FriendsActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference friendListRef;
    private FriendListAdapter adapter;

    private TextView friends_number_textView;
    private EditText add_a_friend_editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friends_number_textView = findViewById(R.id.friends_number_textView);
        findViewById(R.id.friends_request_layout).setOnClickListener(this);
        findViewById(R.id.add_a_friend_button).setOnClickListener(this);

        setUpRecyclerView();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friends_request_layout:
                startActivity(new Intent(this, FriendRequestsActivity.class));
                break;
            case R.id.add_a_friend_button:
                sendFriendRequest();
                break;
        }
    }

    // called when add a friend button is clicked
    // Validation checks before a friend request is send:
    // check if entered email is non empty and if it is not the current user's own email
    // check if the entered email user is an existing user in the database
    // check if the entered email user is not already in the current user's friend list
    // check if a pending friend request has not already been send before by the current user to the entered email user
    // if all checks above are passed, then a friend request is send to the entered email user
    private void sendFriendRequest() {
        add_a_friend_editText = findViewById(R.id.add_a_friend_editText);
        String email = add_a_friend_editText.getText().toString();

        // check if edit text is empty or email entered is the user's own email
        if (email.trim().isEmpty() || email.equals(mAuth.getCurrentUser().getEmail())) {
            add_a_friend_editText.setError("Please enter your friend's email address");
            add_a_friend_editText.requestFocus();
            return;
        } else {

            db.collection("users")
                    .whereEqualTo("email", email) // retrieves users with the entered email
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            // if there is no user with the entered email
                            if(e!=null || queryDocumentSnapshots.size()==0){
                                add_a_friend_editText.setError("User does not exist");
                                add_a_friend_editText.requestFocus();
                            } else { // else, entered email belongs to an existing user
                                for (final DocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                                    // check if entered email user is an existing friend of current user
                                    db.collection("users").document(mAuth.getCurrentUser().getUid())
                                            .collection("friend list")
                                            .document(documentSnapshot.getId())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (!task.getResult().exists()) { // entered email user is not in current user's friend list
                                                        if (!task.getResult().exists()) {
                                                            // check if entered email user already has a pending friend request from current user
                                                            db.collection("users").document(documentSnapshot.getId())
                                                                    .collection("friend requests")
                                                                    .document(mAuth.getCurrentUser().getUid())
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (!task.getResult().exists()) { // No pending friend request
                                                                                // creates id and display name key value pairing to add them to friend request document later
                                                                                Map<String, Object> map = new HashMap<>();
                                                                                map.put("id", mAuth.getCurrentUser().getUid());
                                                                                map.put("displayName", mAuth.getCurrentUser().getDisplayName());

                                                                                // creates a document with id of the current user in the entered email user's friend requests collection
                                                                                db.collection("users").document(documentSnapshot.getId())
                                                                                        .collection("friend requests")
                                                                                        .document(mAuth.getCurrentUser().getUid())
                                                                                        .set(map);
                                                                                Toast.makeText(FriendsActivity.this, "Friend Request sent", Toast.LENGTH_SHORT).show();
                                                                            } else { // else, there is already a pending friend request, so a duplicate is not send
                                                                                Toast.makeText(FriendsActivity.this, "Friend request still pending", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    } else { // else, entered email user is in current user's friend list
                                                        Toast.makeText(FriendsActivity.this, "User is already a friend", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    });

            add_a_friend_editText.getText().clear(); // clear edit text to prevent multiple friend requests send due to multiple clicks
        }

    }

    // Recycler view to display user's friend list
    private void setUpRecyclerView() {
        friendListRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("friend list");

        Query query = friendListRef.orderBy("id");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FriendListAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.friend_list_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        friendListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    friends_number_textView.setText(task.getResult().size() + "");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
