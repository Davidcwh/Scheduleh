package com.example.scheduleh;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SyncResultsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> selectedFriends;
    private ArrayList<Date> dates;
    private ArrayList<TimeSlot> timeSlots;
    private ArrayList<Event> events;
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

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            startDate = simpleDateFormat.parse(startDay + "/" + startMonth + "/" + startYear);
            endDate = simpleDateFormat.parse(endDay + "/" + endMonth + "/" + endYear);
        } catch (Exception e) {e.printStackTrace();}
        dates = getDaysBetweenDates(startDate, endDate);

//        selectedFriends.add(mAuth.getCurrentUser().getUid());
//        for (String userId: selectedFriends) {
//            CollectionReference eventsRef = db.collection("users").document(userId)
//                    .collection("events");
//
//            Query query = eventsRef
//                    .whereGreaterThanOrEqualTo("year", startYear)
//                    .whereLessThanOrEqualTo("year", endYear)
//                    .whereGreaterThanOrEqualTo("month", startMonth)
//                    .whereLessThanOrEqualTo("month", endMonth)
//                    .whereGreaterThanOrEqualTo("day", startDay)
//                    .whereLessThanOrEqualTo("day", endDay);
//        }


        setUpRecyclerView(timeSlots);

    }

    private void setUpRecyclerView(ArrayList<TimeSlot> timeSlots) {
        RecyclerView recyclerView = findViewById(R.id.sync_results_recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView.Adapter mAdapter = new SyncResultAdapter(timeSlots);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

    }

    public static ArrayList<Date> getDaysBetweenDates(Date startdate, Date enddate) {
        ArrayList<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startdate);

        while (calendar.getTime().before(enddate)) {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return dates;
    }
}
