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

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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

        collapsibleCalendar = getView().findViewById(R.id.collapsibleCalendarView);
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = collapsibleCalendar.getSelectedDay();
                selectedYear = day.getYear();
                selectedMonth = day.getMonth() + 1;
                selectedDay = day.getDay();
                setUpRecyclerView(selectedYear, selectedMonth, selectedDay);

                addEventButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), NewEventActivity.class);
                        intent.putExtra("year", selectedYear);
                        intent.putExtra("month", selectedMonth);
                        intent.putExtra("day", selectedDay);
                        startActivity(intent);
                    }
                });

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

    private void setUpRecyclerView(int year, int month, int day) {

        dayEventsRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("years").document(String.valueOf(year))
                .collection("months").document(String.valueOf(month))
                .collection("days").document(String.valueOf(day))
                .collection("events");

        //dayEventsRef = db.collection("test_events");

        Query query = dayEventsRef.orderBy("startTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build();

        adapter = new EventAdapter(options);

        RecyclerView recyclerView = getView().findViewById(R.id.day_events_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }



    @Override
    public void onStart() {
        super.onStart();

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        Log.i(getClass().getName(), "Current Day: "
                + selectedYear + "/" + selectedMonth + "/" + selectedDay);
        setUpRecyclerView(selectedYear, selectedMonth, selectedDay);

        addEventButton = getView().findViewById(R.id.add_event_button);

        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
