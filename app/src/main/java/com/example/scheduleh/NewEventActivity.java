package com.example.scheduleh;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import net.steamcrafted.lineartimepicker.dialog.LinearDatePickerDialog;
import net.steamcrafted.lineartimepicker.dialog.LinearTimePickerDialog;

public class NewEventActivity extends AppCompatActivity {
    private EditText new_event_eventName;
    private Button select_startTime;
    private Button select_endTime;
    private TextView new_event_startTime;
    private TextView new_event_endTime;
    private Spinner new_event_prioritySpinner;
    LinearTimePickerDialog dialogStartTime;
    LinearTimePickerDialog dialogEndTime;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    String setPriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        new_event_eventName = findViewById(R.id.new_event_eventName_editText);
        new_event_startTime = findViewById(R.id.new_event_startTime_textView);
        new_event_endTime = findViewById(R.id.new_event_endTime_textView);
        select_startTime = findViewById(R.id.new_event_startTime_button);
        select_endTime = findViewById(R.id.new_event_endTime_button);

        dialogStartTime = LinearTimePickerDialog.Builder.with(this)
                .setTextColor(Color.parseColor("#ffffff"))
                .setButtonCallback(new LinearTimePickerDialog.ButtonCallback() {
                    @Override
                    public void onPositive(DialogInterface dialog, int hour, int minutes) {
                        String setHour = hour + "";
                        String setMinutes = minutes + "";

                        if (minutes == 0) {
                            setMinutes = "00";
                        }
                        if (hour < 10) {
                            setHour = "0" + hour;
                        }

                        new_event_startTime.setText(setHour + ":" + setMinutes);
                    }

                    @Override
                    public void onNegative(DialogInterface dialog) {

                    }
                })
                .build();
        select_startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogStartTime.show();
            }
        });

        dialogEndTime = LinearTimePickerDialog.Builder.with(this)
                .setTextColor(Color.parseColor("#ffffff"))
                .setButtonCallback(new LinearTimePickerDialog.ButtonCallback() {
                    @Override
                    public void onPositive(DialogInterface dialog, int hour, int minutes) {
                        String setHour = hour + "";
                        String setMinutes = minutes + "";

                        if (minutes == 0) {
                            setMinutes = "00";
                        }
                        if (hour < 10) {
                            setHour = "0" + hour;
                        }

                        new_event_endTime.setText(setHour + ":" + setMinutes);
                    }

                    @Override
                    public void onNegative(DialogInterface dialog) {

                    }
                })
                .build();
        select_endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEndTime.show();
            }
        });


        new_event_prioritySpinner = findViewById(R.id.new_event_prioritySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        new_event_prioritySpinner.setAdapter(adapter);

        new_event_prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setPriority = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
            case R.id.save_event: // When save icon is clicked
                saveEvent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Saves the new event into the database
    private void saveEvent() {
        String eventName = new_event_eventName.getText().toString();
        String startTime = new_event_startTime.getText().toString();
        String endTime = new_event_endTime.getText().toString();

        // Ensures event fields are non-empty
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

        // Retrieving the date selected on the calendar from an intent
        Intent intent = getIntent();
        int eventYear = intent.getIntExtra("year", 0);
        int eventMonth = intent.getIntExtra("month", 0);
        int eventDay = intent.getIntExtra("day", 0);

        // Reference to collection of events
        CollectionReference eventsRef = FirebaseFirestore.getInstance()
                .collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("events");

        // creating event object for the new event
        Event newEvent = new Event(eventName, startTime, endTime, eventYear, eventMonth, eventDay,
                currentUser.getUid(), currentUser.getDisplayName());
        if (setPriority != null) {
            if (setPriority.equals("High")) {
                newEvent.setPriority(3);
            } else if (setPriority.equals("Medium")) {
                newEvent.setPriority(2);
            }
        }

        // Adding the new event into the database
        eventsRef.add(newEvent);
        Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show();
        finish();
    }
}
