package com.example.scheduleh;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FriendRequestsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference friendRequestsRef;
    private FriendRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        setUpRecyclerView();
    }

    // Sets up the recycler view to display all friend requests
    // Note there is not much code here, most of it is in the friend request adapter class
    // (code for clicking confirm/delete friend request etc)
    private void setUpRecyclerView() {
        friendRequestsRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("friend requests");

        Query query = friendRequestsRef.orderBy("id");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FriendRequestAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.friend_requests_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
