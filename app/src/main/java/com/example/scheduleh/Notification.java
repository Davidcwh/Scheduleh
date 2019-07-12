package com.example.scheduleh;

import java.util.Date;

public class Notification {
    private String message;
    private String fromUser;
    private String notificationType;
    private Date date;

    public Notification() {
        // empty constructor needed for recyclerView
    }

    public Notification(String message, String fromUser, String notificationType) {
        this.message = message;
        this.fromUser = fromUser;
        this.notificationType = notificationType;
        this.date = new Date();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
