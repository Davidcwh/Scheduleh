package com.example.scheduleh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.badoualy.datepicker.DatePickerTimeline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OpenjioFragment extends Fragment implements View.OnClickListener{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EventAdapter userEventAdapter;
    private CollectionReference userEventRef;
    private OpenJioAdapter openJioAdapter;
    private CollectionReference openJioRef;
    int selectedYear, selectedMonth, selectedDay;
    DatePickerTimeline timelineCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_openjio, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getView().findViewById(R.id.openJio_myJio_title).setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        setUpUserEventRecyclerView(selectedYear, selectedMonth, selectedDay);
        setUpOpenJioRecyclerView(selectedYear, selectedMonth, selectedDay);

        timelineCalendar = view.findViewById(R.id.openJio_calendar);
        timelineCalendar.setOnDateSelectedListener(new DatePickerTimeline.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int index) {
                selectedYear = year;
                selectedMonth = month + 1;
                selectedDay = day;


                userEventAdapter.stopListening();
                setUpUserEventRecyclerView(selectedYear, selectedMonth, selectedDay);
                userEventAdapter.startListening();

                openJioAdapter.stopListening();
                setUpOpenJioRecyclerView(selectedYear, selectedMonth, selectedDay);
                openJioAdapter.startListening();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openJio_myJio_title:
                startActivity(new Intent(getContext(), UserJioActivity.class));
                break;
        }
    }

    private void setUpUserEventRecyclerView(final int year, final int month, final int day) {
        userEventRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("events");

        // Get events of the particular date and sort these events by start time in ascending order
        Query query = userEventRef.whereEqualTo("year", year)
                .whereEqualTo("month", month)
                .whereEqualTo("day", day)
                .orderBy("startTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build();

        userEventAdapter = new EventAdapter(options);

        final RecyclerView recyclerView = getView().findViewById(R.id.openJio_userEvents_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(userEventAdapter);

        userEventAdapter.setOnLongItemClickListener(new EventAdapter.OnLongItemClickListener() {
            @Override
            public void OnLongItemClick(final DocumentSnapshot documentSnapshot, int position) {

                // Initialising popup menu at the item's position of the recycler view
                PopupMenu popupMenu = new PopupMenu(getContext(), recyclerView.getLayoutManager().findViewByPosition(position));
                popupMenu.inflate(R.menu.event_popup_menu);

                // Setting up the options in the popup menu when clicked
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            // Edit event option
                            case R.id.edit_event_popupMenu:

                                if (!documentSnapshot.get("userId").equals(mAuth.getCurrentUser().getUid())) {
                                    Toast.makeText(getContext(), "Cannot edit your friend's events", Toast.LENGTH_SHORT).show();
                                    return false;
                                }

                                // Opens the edit event activity and sends over the date of the selected event and its id
                                Intent intent = new Intent(getActivity(), EditEventActivity.class);
                                intent.putExtra("year", year);
                                intent.putExtra("month", month);
                                intent.putExtra("day", day);
                                intent.putExtra("eventId", documentSnapshot.getId());
                                startActivity(intent);
                                return true;

                            // Add selected event to user's open jio list
                            case R.id.add_myJios_popupMenu:

                                if (!documentSnapshot.get("userId").equals(mAuth.getCurrentUser().getUid())) {
                                    Toast.makeText(getContext(), "Cannot add your friend's events to your jios", Toast.LENGTH_SHORT).show();
                                    return false;
                                }

                                addEventToOpenJio(documentSnapshot.getId());
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });
    }

    // Adds the event of the input event id to the user's jio collection and to all user's friends' friends jio collection
    private void addEventToOpenJio(String eventId) {

        // reference to event document
        DocumentReference eventRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("events").document(eventId);

        // retrieving event document
        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot event = task.getResult();

                    // Storing all event info
                    final Map<String, Object> jioInfo = new HashMap<>();
                    jioInfo.put("eventName", event.get("eventName"));
                    jioInfo.put("startTime", event.get("startTime"));
                    jioInfo.put("endTime", event.get("endTime"));
                    jioInfo.put("year", event.get("year"));
                    jioInfo.put("month", event.get("month"));
                    jioInfo.put("day", event.get("day"));
                    jioInfo.put("userId", event.get("userId"));
                    jioInfo.put("displayName", event.get("displayName"));

                    // Adding event to user's "user jios" collection
                    db.collection("users").document(mAuth.getCurrentUser().getUid())
                            .collection("user jios")
                            .document(event.getId())
                            .set(jioInfo);

                    // reference to collection of user's friends
                    CollectionReference allFriends = db.collection("users").document(mAuth.getCurrentUser().getUid())
                            .collection("friend list");
                    allFriends.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                // for all friends
                                for (QueryDocumentSnapshot friend: task.getResult()) {
                                    // for each friend, add the event to their "friend jios" list
                                    db.collection("users").document(friend.getId())
                                            .collection("friend jios")
                                            .document(event.getId())
                                            .set(jioInfo);
                                }
                            }
                        }
                    });


                } else {
                    Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpOpenJioRecyclerView(final int year, final int month, final int day) {
        openJioRef = db.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("friend jios");

        // Get events of the particular date and sort these events by start time in ascending order
        Query query = openJioRef.whereEqualTo("year", year)
                .whereEqualTo("month", month)
                .whereEqualTo("day", day)
                .orderBy("startTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build();

        openJioAdapter = new OpenJioAdapter(options);

        final RecyclerView recyclerView = getView().findViewById(R.id.openJio_friendJios_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(openJioAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        userEventAdapter.startListening();
        openJioAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        userEventAdapter.stopListening();
        openJioAdapter.stopListening();
    }
}
