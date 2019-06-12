package com.example.scheduleh;

public class Event {
    private String eventName;
    private double startTime;
    private double endTime;

    int year;
    int month;
    int day;

    public Event() {
        //empty constructor needed
    }

    public Event(String eventName, double startTime, double endTime, int year, int month, int day) {
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public String getEventName() {
        return eventName;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(double duration) {
        this.endTime = duration;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
