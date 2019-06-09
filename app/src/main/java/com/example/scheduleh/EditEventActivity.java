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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference documentReference;
    private EditText edit_event_eventName;
    private EditText edit_event_startTime;
    private EditText edit_event_endTime;
    private Button edit_event_deleteEvent;
    int eventYear;
    int eventMonth;
    int eventDay;
    String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Setting up authentication and database links
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Retrieving information about event to be edited from the intent passed from the calendar fragement
        Intent intent = getIntent();
        eventYear = intent.getIntExtra("year", 0);
        eventMonth = intent.getIntExtra("month", 0);
        eventDay = intent.getIntExtra("day", 0);
        eventId = intent.getStringExtra("eventId");
        documentReference = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("years").document(String.valueOf(eventYear))
                .collection("months").document(String.valueOf(eventMonth))
                .collection("days").document(String.valueOf(eventDay))
                .collection("events").document(eventId);

        // Initializing edit texts and button
        edit_event_eventName = findViewById(R.id.edit_event_eventName_editText);
        edit_event_startTime = findViewById(R.id.edit_event_startTime_editText);
        edit_event_endTime = findViewById(R.id.edit_event_endTime_editText);
        edit_event_deleteEvent = findViewById(R.id.edit_event_deleteEvent_button);

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
                documentReference.delete(); // deletes the event document saved in the database
                Toast.makeText(EditEventActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                finish();
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

    // Updates the event to be edited with the information in the edit texts
    private void updateEvent() {
        String eventName = edit_event_eventName.getText().toString();
        String startTime = edit_event_startTime.getText().toString();
        String endTime = edit_event_endTime.getText().toString();

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

        documentReference.update("eventName", eventName);
        documentReference.update("startTime", Double.parseDouble(startTime));
        documentReference.update("endTime", Double.parseDouble(endTime));
        Toast.makeText(this, "Event Updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}
