package com.example.scheduleh;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewEventActivity extends AppCompatActivity {
    private EditText new_event_eventName;
    private EditText new_event_startTime;
    private EditText new_event_endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        new_event_eventName = findViewById(R.id.new_event_eventName_editText);
        new_event_startTime = findViewById(R.id.new_event_startTime_editText);
        new_event_endTime = findViewById(R.id.new_event_endTime_editText);

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
                saveEvent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveEvent() {
        String eventName = new_event_eventName.getText().toString();
        String startTime = new_event_startTime.getText().toString();
        String endTime = new_event_endTime.getText().toString();

        if (eventName.trim().isEmpty()) {
            new_event_eventName.setError("Event name required");
            new_event_eventName.requestFocus();
            return;
        }
        if (startTime.trim().isEmpty()) {
            new_event_startTime.setError("Start time required");
            new_event_startTime.requestFocus();
            return;
        }
        if (endTime.trim().isEmpty()) {
            new_event_endTime.setError("End time required");
            new_event_endTime.requestFocus();
            return;
        }

        Intent intent = getIntent();
        int eventYear = intent.getIntExtra("year", 0);
        int eventMonth = intent.getIntExtra("month", 0);
        int eventDay = intent.getIntExtra("day", 0);

        CollectionReference eventsRef = FirebaseFirestore.getInstance()
                .collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("years").document(String.valueOf(eventYear))
                .collection("months").document( String.valueOf(eventMonth))
                .collection("days").document(String.valueOf(eventDay))
                .collection("events");

        eventsRef.add(new Event(eventName, Double.parseDouble(startTime), Double.parseDouble(endTime)));
        Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show();
        finish();
    }
}
