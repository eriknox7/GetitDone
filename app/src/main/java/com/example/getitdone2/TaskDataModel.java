package com.example.getitdone2;

import java.io.Serializable;

public class TaskDataModel implements Serializable {
    private static final long serialVersionUID = 1L;


    private String id;
    String title;
    String date;
    String time;
    String length;
    String content;

    // Constructor with no arguments for Firebase Firestore.
    public TaskDataModel() {}

    public TaskDataModel(String title, String date, String time, String length, String content) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.length = length;
        this.content = content;
    }

    // Getters and Setters.
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date.substring(0, date.length()-1);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time.substring(0, time.length()-1);
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}