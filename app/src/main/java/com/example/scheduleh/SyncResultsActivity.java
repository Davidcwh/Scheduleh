package com.example.scheduleh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

public class SyncResultsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> selectedFriends;
    private ArrayList<Date> dates;
    private ArrayList<TimeSlot> timeSlots;
    private Date startDate;
    private Date endDate;
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
        setContentView(R.layout.activity_sync_results);

        timeSlots = new ArrayList<>();

        // Receiving the start and end dates, the list of friends id to sync with, and the duration of free time desired
        Intent intent = getIntent();
        selectedFriends = intent.getStringArrayListExtra("friends selected");
        startDay = intent.getIntExtra("start day", -1);
        startMonth = intent.getIntExtra("start month", -1);
        startYear= intent.getIntExtra("start year", -1);
        endDay = intent.getIntExtra("end day", -1);
        endMonth = intent.getIntExtra("end month", -1);
        endYear= intent.getIntExtra("end year", -1);
        hours = intent.getIntExtra("hours", -1);
        minutes = intent.getIntExtra("minutes", -1);

        Log.i("received date", "day: " + endDay + ", month: " + endMonth + ", year: " + endYear);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            startDate = simpleDateFormat.parse(startDay + "/" + (startMonth + 1)+ "/" + startYear);
            endDate = simpleDateFormat.parse(endDay + "/" + (endMonth + 1) + "/" + endYear);
        } catch (Exception e) {e.printStackTrace();}
        dates = getDaysBetweenDates(startDate, endDate);

        selectedFriends.add(mAuth.getCurrentUser().getUid()); // add the current user into the list of users to sync schedules of
        // for each date in the required date range
        for (Date currentDate: dates) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = calendar.get(Calendar.YEAR);

            // Generate all timeslots for the date first
            generatePossibleTimeSlots(timeSlots, currentDay, currentMonth, currentYear, hours, minutes);

            // For each user, retrieve their events for the date
            for (String userId: selectedFriends) {
                CollectionReference eventsRef = db.collection("users").document(userId).collection("events");

                Query query = eventsRef.whereEqualTo("year", currentYear)
                        .whereEqualTo("month", currentMonth)
                        .whereEqualTo("day", currentDay)
                        .orderBy("startTime");

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot event: task.getResult()) {
                                Event currentEvent = event.toObject(Event.class);

                                // iterate thru all timeslots for the date and check if the user's event overlaps with it, means this user is busy for that slot
                                for (TimeSlot timeSlot: timeSlots) {
                                    if (overlapTimings(currentEvent.getStartTime(), currentEvent.getEndTime(), timeSlot.getStartTime(), timeSlot.getEndTime()) &&
                                    timeSlot.getYear() == currentEvent.getYear() && timeSlot.getMonth() == currentEvent.getMonth() && timeSlot.getDay() == currentEvent.getDay()){
                                        timeSlot.setPriority(timeSlot.getPriority() + currentEvent.getPriority()); // increase priority for timeslot

                                        if (!timeSlot.containsBusyUser(currentEvent.getUserId())) {
                                            timeSlot.setBusy(timeSlot.getBusy() + 1); // update free and busy count
                                            timeSlot.setFree(timeSlot.getFree() - 1); // for timeslot accordingly
                                            timeSlot.addBusyUser(currentEvent.getUserId());
                                        }

                                    }

                                }
                            }

                            setUpRecyclerView(timeSlots);
                        }
                    }
                });
            }


        }

    }

    private void setUpRecyclerView(final ArrayList<TimeSlot> timeSlots) {
        Collections.sort(timeSlots);
        RecyclerView recyclerView = findViewById(R.id.sync_results_recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView.Adapter mAdapter = new SyncResultAdapter(timeSlots);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

        ((SyncResultAdapter) mAdapter).setOnTimeSlotItemClickListener(new SyncResultAdapter.OnTimeSlotItemClickListener() {
            @Override
            public void OnTimeSlotItemClick(int position) {
                TimeSlot timeSlot = timeSlots.get(position);
                Intent intent = new Intent(getApplicationContext(), BusyFreeListActivity.class);
                intent.putExtra("friends selected", selectedFriends);
                intent.putExtra("friends busy", timeSlot.getBusyUsers());
                intent.putExtra("day", timeSlot.getDay());
                intent.putExtra("month", timeSlot.getMonth());
                intent.putExtra("year", timeSlot.getYear());
                intent.putExtra("start time", timeSlot.getStartTime());
                intent.putExtra("end time", timeSlot.getEndTime());
                startActivity(intent);
            }
        });

    }

    // Generates all possible available time slots of the given time duration and date
    private void generatePossibleTimeSlots(ArrayList<TimeSlot> timeSlots, int currentDay, int currentMonth, int currentYear, int hours, int minutes) {
        int startHour = 0;
        int startMinute = 0;
        int endHour = startHour + hours;
        int endMinute = startMinute + minutes;

        while (startHour <= 23 && (endHour <= 23 || (endHour == 24 && endMinute == 00))) {

            String startTime = "";
            String endTime = "";

            startTime = startHour + ":";
            if (startHour < 10) {
                startTime = "0" + startTime;
            }

            endTime = endHour + ":";
            if (endHour < 10) {
                endTime = "0" + endTime;
            }

            startTime = startTime + startMinute;
            if (startMinute == 0) {
                startTime = startTime + "0";
            }

            endTime = endTime + endMinute;
            if (endMinute == 0) {
                endTime += "0";
            }

            timeSlots.add(new TimeSlot(startTime, endTime, currentYear, currentMonth, currentDay,
                    selectedFriends.size(), 0));

            if (startMinute == 0) {
                startMinute += 30;
            } else {
                startMinute = 0;
                startHour++;
            }

            if (endMinute == 0) {
                endMinute += 30;
            } else {
                endMinute = 0;
                endHour++;
            }
        }
    }

    // Given the start and end timings of two periods in string format, determines if they overlap
    private boolean overlapTimings(String startA, String endA, String startB, String endB) {
        int startAHour = Integer.parseInt(startA.substring(0, 2));
        int startAMinute = Integer.parseInt(startA.substring(3));

        int endAHour = Integer.parseInt(endA.substring(0, 2));
        int endAMinute = Integer.parseInt(endA.substring(3));

        int startBHour = Integer.parseInt(startB.substring(0, 2));
        int startBMinute = Integer.parseInt(startB.substring(3));

        int endBHour = Integer.parseInt(endB.substring(0, 2));
        int endBMinute = Integer.parseInt(endB.substring(3));

        double startTimeA = startAHour;
        if (startAMinute == 30) {
            startTimeA += 0.5;
        }
        double endTimeA = endAHour;
        if (endAMinute == 30) {
            endTimeA += 0.5;
        }
        double startTimeB = startBHour;
        if (startBMinute == 30) {
            startTimeB += 0.5;
        }
        double endTimeB = endBHour;
        if (endBMinute == 30) {
            endTimeB += 0.5;
        }


        return (startTimeA < endTimeB) && (endTimeA > startTimeB);
    }

    // Generates list of dates between the two given dates inclusive
    private ArrayList<Date> getDaysBetweenDates(Date startDate, Date endDate) {
        ArrayList<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate)) {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        Date result = calendar.getTime();
        dates.add(result);
        return dates;
    }
}
