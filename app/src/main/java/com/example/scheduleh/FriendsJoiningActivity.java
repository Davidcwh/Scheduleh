package com.example.scheduleh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FriendsJoiningActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference friendJoiningRef;
    private FriendListAdapter adapter;
    private TextView friends_joining_number_textView;
    private String eventId;
    private String eventUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_joining);

        friends_joining_number_textView = findViewById(R.id.friends_joining_number_textView);

        // Getting the event id and user id of the openjio event selected
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        eventUserId = intent.getStringExtra("userId");

        setUpRecyclerView();
    }

    // Sets up the recycler view to display all users who are going for the openjio event selected
    private void setUpRecyclerView() {
        friendJoiningRef = db.collection("users").document(eventUserId)
                .collection("user jios").document(eventId)
                .collection("friends joined");

        Query query = friendJoiningRef.orderBy("displayName");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FriendListAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.friends_joining_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        friendJoiningRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    friends_joining_number_textView.setText(task.getResult().size() + "");
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
