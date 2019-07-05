package com.example.scheduleh;

import java.text.SimpleDateFormat;
import java.util.Date;

// Class used to display results of sync function
public class TimeSlot implements Comparable<TimeSlot>{
    private String startTime;
    private String endTime;
    private int year;
    private int month;
    private int day;
    private int free;
    private int busy;
    private int priority;
    private Date date;

    public TimeSlot(String startTime, String endTime, int year, int month, int day, int free, int busy) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.year = year;
        this.month = month;
        this.day = day;
        this.free = free;
        this.busy = busy;
        this.priority = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            date = simpleDateFormat.parse(day + "/" + month + "/" + year);
        } catch (Exception e) {e.printStackTrace();}
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getBusy() {
        return busy;
    }

    public void setBusy(int busy) {
        this.busy = busy;
    }

    @Override
    public int compareTo(TimeSlot o) {
        if (this.priority != o.priority) {
            return this.priority - o.priority; // higher priority means more busy people/ lesser people able to make it
        } else if (!this.date.equals(o.date)) {
            return this.date.compareTo(o.date);
        } else if (!this.startTime.equals(o.startTime)){
            return this.startTime.compareTo(o.startTime);
        } else {
            return 0;
        }
    }
}
