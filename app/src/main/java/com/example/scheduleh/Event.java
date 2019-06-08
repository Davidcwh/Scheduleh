package com.example.scheduleh;

public class Event {
    private String eventName;
    private double startTime;
    private double endTime;

    public Event() {
        //empty constructor needed
    }

    public Event(String eventName, double startTime, double endTime) {
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
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


    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(double duration) {
        this.endTime = duration;
    }
}
