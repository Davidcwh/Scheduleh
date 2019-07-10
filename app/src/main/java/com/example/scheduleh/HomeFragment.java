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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class HomeFragment extends Fragment implements View.OnClickListener {

    private TextView userDisplayName, displayCurrentDate;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dayEventsRef;
    private EventAdapter adapter;
    private ImageView imageView;
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
        imageView = getView().findViewById(R.id.homeUserProfilePhoto);
        getView().findViewById(R.id.homeSettings).setOnClickListener(this);
        getView().findViewById(R.id.homeFriends).setOnClickListener(this);


        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            //retrieve profile picture
            if (user.getPhotoUrl() != null) {
                //if not null, we will add the photo to the imageview of the profile screen
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .into(imageView);
            }
        } //Display profile pic
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
                .collection("events");

        // Get events of the particular date and sort these events by start time in ascending order
        Query query = dayEventsRef.whereEqualTo("year", year)
                                    .whereEqualTo("month", month)
                                    .whereEqualTo("day", day)
                                    .orderBy("startTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Event> options = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build();

        adapter = new EventAdapter(options);

        final RecyclerView recyclerView = getView().findViewById(R.id.currentDay_events_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        // When an event in the recycler view is long clicked, a popup menu will be shown
        // popup menu consists of options to edit event or add event to user's open jio list
        adapter.setOnLongItemClickListener(new EventAdapter.OnLongItemClickListener() {
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

                            // Add selected event for openJio
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
                    jioInfo.put("priority", event.get("priority"));

                    // Adding event to user's "user jios" collection
                    db.collection("users").document(mAuth.getCurrentUser().getUid())
                            .collection("user jios")
                            .document(event.getId())
                            .set(jioInfo);

                    // Now add the user's id and display name to the set of info
                    // This is because the event will now be added to all friends' "friend jios"
                    jioInfo.put("userId", mAuth.getCurrentUser().getUid());
                    jioInfo.put("displayName", mAuth.getCurrentUser().getDisplayName());

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
