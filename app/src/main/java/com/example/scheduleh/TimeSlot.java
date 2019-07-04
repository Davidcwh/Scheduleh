package com.example.scheduleh;

public class TimeSlot {
    private String startTime;
    private String endTime;
    private int year;
    private int month;
    private int day;
    private int free;
    private int busy;

    public TimeSlot(String startTime, String endTime, int year, int month, int day, int free, int busy) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.year = year;
        this.month = month;
        this.day = day;
        this.free = free;
        this.busy = busy;
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

}
