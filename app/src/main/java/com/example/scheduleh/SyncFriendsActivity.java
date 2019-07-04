package com.example.scheduleh;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class SyncFriendsActivity extends AppCompatActivity {

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

        sync_friends_startDate_button = findViewById(R.id.sync_friends_startDate_button);
        sync_friends_endDate_button = findViewById(R.id.sync_friends_endDate_button);
        sync_friends_startDate_textView = findViewById(R.id.sync_friends_startDate_textView);
        sync_friends_endDate_textView = findViewById(R.id.sync_friends_endDate_textView);


        Calendar calendar = Calendar.getInstance();
        dialogStartDate = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                sync_friends_startDate_textView.setText(dayOfMonth + "/" + month + "/" + year);

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
                sync_friends_endDate_textView.setText(dayOfMonth + "/" + month + "/" + year);

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

    }
}
