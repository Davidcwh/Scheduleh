package com.example.scheduleh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private TextView userDisplayName, displayCurrentDate;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dayEventsRef;
    private EventAdapter adapter;
    int currentYear, currentMonth, currentDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialising click listeners for the settings and friends image views (since they are not buttons)
        getView().findViewById(R.id.homeSettings).setOnClickListener(this);
        getView().findViewById(R.id.homeFriends).setOnClickListener(this);

        userDisplayName = getView().findViewById(R.id.homeUserDisplayName); // Initialise textView to display name
        userDisplayName.setText(mAuth.getCurrentUser().getDisplayName()); // Set user display name to textView

        // Getting the current date information
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Setting the current date text view to display today's date
        displayCurrentDate = getView().findViewById(R.id.display_currentDate_textView);
        displayCurrentDate.setText(currentDay + " " + convertIntToMonth(currentMonth) + ", " + currentYear);

        // Set up recycler view to display today's events
        setUpRecyclerView(currentYear, currentMonth, currentDay);
    }

    // Called when something is clicked
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.homeSettings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;

            case R.id.homeFriends:
                startActivity(new Intent(getActivity(), FriendsActivity.class));
                break;
        }
    }

    // Sets up the recycler view to display the today's events - specified by the input year, month and day
    private void setUpRecyclerView(final int year, final int month, final int day) {
        Log.i(getClass().getName(), "Today's Date: "
                + year + "/" + month + "/" + day);

        // Reference to collection of events to display in recycler view
        dayEventsRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("years").document(String.valueOf(year))
                .collection("months").document(String.valueOf(month))
                .collection("days").document(String.valueOf(day))
                .collection("events");

        // Sort the events by start time in ascending order
        Query query = dayEventsRef.orderBy("startTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build();

        adapter = new EventAdapter(options);

        RecyclerView recyclerView = getView().findViewById(R.id.currentDay_events_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        // When an event in the recycler view is long clicked, the activity to edit or delete that event is brought up
        adapter.setOnLongItemClickListener(new EventAdapter.OnLongItemClickListener() {
            @Override
            public void OnLongItemClick(DocumentSnapshot documentSnapshot, int position) {
                Intent intent = new Intent(getActivity(), EditEventActivity.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                intent.putExtra("eventId", documentSnapshot.getId());
                startActivity(intent);
            }
        });
    }

    // For displaying the selected date
    private String convertIntToMonth(int num) {
        switch (num) {
            default:
                return null;
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
