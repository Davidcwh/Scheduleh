package com.example.scheduleh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;

public class BusyFreeListActivity extends AppCompatActivity {

    private ArrayList<String> selectedFriends;
    private ArrayList<String> busyFriends;
    private ArrayList<String> freeFriends;
    private int day;
    private int month;
    private int year;
    private String startTime;
    private String endTime;

    private TextView date_textView;
    private TextView time_textView;
    private RecyclerView free_recyclerView;
    private RecyclerView busy_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busy_free_list);

        date_textView = findViewById(R.id.date_textView);
        time_textView = findViewById(R.id.time_textView);
        free_recyclerView = findViewById(R.id.free_recyclerView);
        busy_recyclerView = findViewById(R.id.busy_recyclerView);

        Intent intent = getIntent();
        selectedFriends = intent.getStringArrayListExtra("friends selected");
        busyFriends = intent.getStringArrayListExtra("friends busy");
        day = intent.getIntExtra("day", -1);
        month = intent.getIntExtra("month", -1);
        year = intent.getIntExtra("year", -1);
        startTime = intent.getStringExtra("start time");
        endTime = intent.getStringExtra("end time");

        date_textView.setText(day + "/" + month + "/" + year);
        time_textView.setText(startTime + " - " + endTime);

        freeFriends = new ArrayList<>();
        for (String userId: selectedFriends) {
            if (!busyFriends.contains(userId)) {
                freeFriends.add(userId);
            }
        }

        setUpRecyclerView(freeFriends, free_recyclerView);
        setUpRecyclerView(busyFriends, busy_recyclerView);
    }

    private void setUpRecyclerView(final ArrayList<String> users, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView.Adapter mAdapter = new BusyFreeAdapter(users);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

    }

}
