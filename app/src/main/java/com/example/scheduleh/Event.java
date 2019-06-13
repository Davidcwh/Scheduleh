package com.example.scheduleh;

public class Event {
    private String eventName;
    private double startTime;
    private double endTime;
    private int year;
    private int month;
    private int day;
    private int priority;

    private String userId;
    private String displayName;

    public Event() {
        //empty constructor needed
    }

    public Event(String eventName, double startTime, double endTime, int year, int month, int day, String userId, String displayName) {
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.year = year;
        this.month = month;
        this.day = day;
        this.userId = userId;
        this.displayName = displayName;
        this.priority = 1;
    }



    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
