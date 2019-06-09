package com.example.scheduleh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;
import com.shrikanthravi.collapsiblecalendarview.data.Day;

import java.util.Calendar;


public class CalendarFragment extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dayEventsRef;
    private EventAdapter adapter;
    private FloatingActionButton addEventButton;
    private TextView displaySelectedDate;
    int selectedYear, selectedMonth, selectedDay;

    CollapsibleCalendar collapsibleCalendar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Getting the current date in order to display today's list of events
        // in the recycler view by default when calendar tab is clicked
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        Log.i(getClass().getName(), "Current Day: "
                + selectedYear + "/" + selectedMonth + "/" + selectedDay);

        displaySelectedDate = getView().findViewById(R.id.display_selectedDate_textView);
        displaySelectedDate.setText(R.string.today);
        setUpRecyclerView(selectedYear, selectedMonth, selectedDay); // Set up the recycler view to display today's events by default


        // Initialising the collapsible calendar view
        collapsibleCalendar = getView().findViewById(R.id.collapsibleCalendarView);
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() { // When a date is selected, recycler view must change to display that date's events
                Day day = collapsibleCalendar.getSelectedDay();
                selectedYear = day.getYear();
                selectedMonth = day.getMonth() + 1;
                selectedDay = day.getDay();

                //If selected day is not today, then change the displayed date in the textView
                Calendar calendar = Calendar.getInstance();
                if (!(selectedYear == calendar.get(Calendar.YEAR) &&
                        selectedMonth == calendar.get(Calendar.MONTH) + 1 &&
                        selectedDay == calendar.get(Calendar.DAY_OF_MONTH))) {
                    displaySelectedDate.setText(selectedDay + " " + convertIntToMonth(selectedMonth) + ", " + selectedYear);
                } else {
                    // else, means today's date was selected again, so just display "today"
                    displaySelectedDate.setText(R.string.today);
                }

                // must stop adapter from listening for updates in the database before setting it up again
                // to display the selected date's events
                adapter.stopListening();
                setUpRecyclerView(selectedYear, selectedMonth, selectedDay);
                adapter.startListening(); // start again after set up complete

                Log.i(getClass().getName(), "Selected Day: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());
            }

            @Override
            public void onItemClick(View v) {

            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {

            }

            @Override
            public void onWeekChange(int position) {

            }
        });


    }

    // Sets up the recycler view to display the events of a particular date - specified by the input year, month and day
    private void setUpRecyclerView(final int year, final int month, final int day) {
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

        RecyclerView recyclerView = getView().findViewById(R.id.day_events_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Initialising the add event button
        addEventButton = getView().findViewById(R.id.add_event_button);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Passes the date through the intent so that the event will be added to the
                // correct date's events collection in the add new event activity
                Intent intent = new Intent(getActivity(), NewEventActivity.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                startActivity(intent);
            }
        });

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
