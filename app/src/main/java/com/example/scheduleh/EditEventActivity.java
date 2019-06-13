package com.example.scheduleh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference documentReference;
    private EditText edit_event_eventName;
    private EditText edit_event_startTime;
    private EditText edit_event_endTime;
    private Spinner edit_event_prioritySpinner;
    private Button edit_event_deleteEvent;
    int eventYear;
    int eventMonth;
    int eventDay;
    String eventId;
    int setPriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Setting up authentication and database links
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Retrieving information about event to be edited from the intent passed from the calendar fragment
        Intent intent = getIntent();
        eventYear = intent.getIntExtra("year", 0);
        eventMonth = intent.getIntExtra("month", 0);
        eventDay = intent.getIntExtra("day", 0);
        eventId = intent.getStringExtra("eventId");

        documentReference = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("events").document(eventId);

        // Initializing edit texts and button
        edit_event_eventName = findViewById(R.id.edit_event_eventName_editText);
        edit_event_startTime = findViewById(R.id.edit_event_startTime_editText);
        edit_event_endTime = findViewById(R.id.edit_event_endTime_editText);
        edit_event_deleteEvent = findViewById(R.id.edit_event_deleteEvent_button);
        edit_event_prioritySpinner = findViewById(R.id.edit_event_prioritySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_event_prioritySpinner.setAdapter(adapter);

        edit_event_prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("Low")) {
                    setPriority = 1;
                } else if (selected.equals("Medium")) {
                    setPriority = 2;
                } else if (selected.equals("High")) {
                    setPriority = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Setting the edit texts' initial text to contain the original event information
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null) {
                        Event event = documentSnapshot.toObject(Event.class);
                        edit_event_eventName.setText(event.getEventName());
                        edit_event_startTime.setText(event.getStartTime() + "");
                        edit_event_endTime.setText(event.getEndTime() + "");
                        setPriority = event.getPriority(); // initial priority
                    } else {
                        Toast.makeText(EditEventActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditEventActivity.this, "Failed to retrieve event with " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Delete button functionality
        edit_event_deleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvent();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_event:
                updateEvent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Deletes the event EVERYWHERE in the database
    private void deleteEvent() {

        // #1: deletes the event document saved in the user's events collection
        documentReference.delete();

        // #2: deletes the event document in all friends' "friend jio" collection, if it is there.
        CollectionReference allFriends = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("friend list");
        allFriends.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot friend: task.getResult()) {
                        // for each friend, search their "friend jios" list for the event
                        db.collection("users").document(friend.getId())
                                .collection("friend jios")
                                .document(documentReference.getId())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.getResult().exists()) { // if the event is in the collection, delete it
                                    task.getResult().getReference().delete();
                                }
                            }
                        });
                    }
                }
            }
        });


        // #3: deletes the event document from the user's own "user jios" collection
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("user jios")
                .document(documentReference.getId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    task.getResult().getReference().delete();
                }
            }
        });

        Toast.makeText(EditEventActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Updates the event with the information in the edit texts, wherever the event is in the database
    private void updateEvent() {
        final String eventName = edit_event_eventName.getText().toString();
        final String startTime = edit_event_startTime.getText().toString();
        final String endTime = edit_event_endTime.getText().toString();

        // Checks if edit texts are empty
        if (eventName.trim().isEmpty()) {
            edit_event_eventName.setError("Event name required");
            edit_event_eventName.requestFocus();
            return;
        }
        if (startTime.trim().isEmpty()) {
            edit_event_startTime.setError("Start time required");
            edit_event_startTime.requestFocus();
            return;
        }
        if (endTime.trim().isEmpty()) {
            edit_event_endTime.setError("End time required");
            edit_event_endTime.requestFocus();
            return;
        }

        // Updating the event in the user's events collection
        documentReference.update("eventName", eventName);
        documentReference.update("startTime", Double.parseDouble(startTime));
        documentReference.update("endTime", Double.parseDouble(endTime));
        documentReference.update("priority", setPriority);

        // Updating the event in all of the user's friends' "friend jios" collection, if it is there.
        CollectionReference allFriends = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("friend list");
        allFriends.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot friend: task.getResult()) {
                        // for each friend, search their "friend jios" list for the event
                        db.collection("users").document(friend.getId())
                                .collection("friend jios")
                                .document(documentReference.getId())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.getResult().exists()) { // if the event is in the collection, delete it
                                    DocumentReference ref = task.getResult().getReference();
                                    ref.update("eventName", eventName);
                                    ref.update("startTime", Double.parseDouble(startTime));
                                    ref.update("endTime", Double.parseDouble(endTime));
                                    ref.update("priority", setPriority);
                                }
                            }
                        });
                    }
                }
            }
        });

        // Updating the event in the the user's "user jios" collection, if it is there
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("user jios")
                .document(documentReference.getId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    DocumentReference ref = task.getResult().getReference();
                    ref.update("eventName", eventName);
                    ref.update("startTime", Double.parseDouble(startTime));
                    ref.update("endTime", Double.parseDouble(endTime));
                    ref.update("priority", setPriority);
                }
            }
        });

        Toast.makeText(this, "Event Updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}
