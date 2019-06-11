package com.example.scheduleh;

// User class to hold user id and display name for recycler view
// user id is the user id from the "users" collection
public class User {
    String id;
    String displayName;

    public User() {
        // Empty constructor needed; do not delete
    }

    public User(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
