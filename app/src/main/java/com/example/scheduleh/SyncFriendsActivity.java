package com.example.scheduleh;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;

public class SyncFriendsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference friendListRef;
    private FriendListAdapter adapter;
    private ArrayList<String> selectedFriends;

    private Button sync_button;
    private Button sync_friends_startDate_button;
    private Button sync_friends_endDate_button;
    private TextView sync_friends_startDate_textView;
    private TextView sync_friends_endDate_textView;
    private DatePickerDialog dialogStartDate;
    private DatePickerDialog dialogEndDate;
    private Spinner set_hours_spinner;
    private Spinner set_minutes_spinner;
    private int startDay;
    private int startMonth;
    private int startYear;
    private int endDay;
    private int endMonth;
    private int endYear;
    private int hours;
    private int minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_friends);

        selectedFriends = new ArrayList<>();

        sync_button = findViewById(R.id.sync_button);
        sync_friends_startDate_button = findViewById(R.id.sync_friends_startDate_button);
        sync_friends_endDate_button = findViewById(R.id.sync_friends_endDate_button);
        sync_friends_startDate_textView = findViewById(R.id.sync_friends_startDate_textView);
        sync_friends_endDate_textView = findViewById(R.id.sync_friends_endDate_textView);

        Calendar calendar = Calendar.getInstance();
        dialogStartDate = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                sync_friends_startDate_textView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);

                startDay = dayOfMonth;
                startMonth = month;
                startYear = year;
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        sync_friends_startDate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogStartDate.show();
            }
        });


        dialogEndDate = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                sync_friends_endDate_textView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);

                endDay = dayOfMonth;
                endMonth = month;
                endYear = year;
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        sync_friends_endDate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEndDate.show();
            }
        });

        set_hours_spinner = findViewById(R.id.set_hours_Spinner);
        ArrayAdapter<CharSequence> adapterHours = ArrayAdapter.createFromResource(this,
                R.array.hours_array, android.R.layout.simple_spinner_dropdown_item);
        adapterHours.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item));
        set_hours_spinner.setAdapter(adapterHours);

        set_hours_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hours = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        set_minutes_spinner = findViewById(R.id.set_minutes_Spinner);
        ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this,
                R.array.minutes_array, android.R.layout.simple_spinner_dropdown_item);
        adapterMinutes.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item));
        set_minutes_spinner.setAdapter(adapterMinutes);

        set_minutes_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                minutes = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setUpRecyclerView();

        sync_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSync();
            }
        });

    }

    private void setUpRecyclerView() {
        friendListRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("friend list");

        Query query = friendListRef.orderBy("id");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FriendListAdapter(options);

        final RecyclerView recyclerView = findViewById(R.id.sync_friends_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new FriendListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DocumentSnapshot documentSnapshot, int position) {
                View itemView = recyclerView.getLayoutManager().getChildAt(position);
                String friendId = documentSnapshot.getId();

                // if friend was already selected, unselect him
                if (selectedFriends.contains(friendId)) {
                    selectedFriends.remove(friendId);
                    itemView.setBackgroundColor(Color.parseColor("#404040"));
                } else { // else, select friend
                    selectedFriends.add(friendId);
                    itemView.setBackgroundColor(Color.parseColor("#00cf78"));
                }

            }
        });
    }

    private void startSync() {
        if (sync_friends_startDate_textView.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a start date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sync_friends_endDate_textView.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an end date", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date startDate = simpleDateFormat.parse(startDay + "/" + startMonth + "/" + startYear);
            Date endDate = simpleDateFormat.parse(endDay + "/" + endMonth + "/" + endYear);

            if (startDate.compareTo(endDate) > 0) {
                Toast.makeText(this, "End date earlier than start date", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {e.printStackTrace();}


        if (hours == 0 && minutes == 0) {
            set_hours_spinner.requestFocus();
            Toast.makeText(this, "Please enter a valid duration", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFriends.isEmpty()) {
            Toast.makeText(this, "Please select at least one friend", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SyncResultsActivity.class);
        intent.putExtra("friends selected", selectedFriends);
        intent.putExtra("start day", startDay);
        intent.putExtra("start month", startMonth);
        intent.putExtra("start year", startYear);
        intent.putExtra("end day", endDay);
        intent.putExtra("end month", endMonth);
        intent.putExtra("end year", endYear);
        intent.putExtra("hours", hours);
        intent.putExtra("minutes", minutes);
        startActivity(intent);
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
