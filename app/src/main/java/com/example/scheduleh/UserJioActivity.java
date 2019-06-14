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

public class UserJioActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UserJioAdapter adapter;
    private CollectionReference userJioRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_jio);

        setUpRecyclerView();
    }

    // Sets up a recycler view to display the current user's events for openjio
    // In this list, the user is able to select options like see the list of friends coming for each jio,
    // and the option to remove the event from openjio
    private void setUpRecyclerView() {
        userJioRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("user jios");

        Query query = userJioRef.whereEqualTo("userId", mAuth.getCurrentUser().getUid()).orderBy("startTime");

        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build();

        adapter = new UserJioAdapter(options);

        final RecyclerView recyclerView = findViewById(R.id.myJio_recyclerView);
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
