package com.example.getitdone;

import java.io.Serializable;
import java.util.Objects;

public class TaskDataModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    String title;
    String date;
    String time;
    String length;
    String content;

    // No-arg constructor for Firestore
    public TaskDataModel() {}

    public TaskDataModel(String title, String date, String time, String length, String content) {
        this.title   = title;
        this.date    = date;
        this.time    = time;
        this.length  = length;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title; }

    // FIX: null/empty guard — no more substring crash
    public String getDate() {
        if (date == null || date.isEmpty()) return "";
        return date.endsWith(" ") ? date.substring(0, date.length() - 1) : date;
    }
    public void setDate(String date) { this.date = date; }

    public String getTime() {
        if (time == null || time.isEmpty()) return "";
        return time.endsWith(" ") ? time.substring(0, time.length() - 1) : time;
    }
    public void setTime(String time) { this.time = time; }

    public String getLength() { return length != null ? length : "0"; }
    public void setLength(String length) { this.length = length; }

    public String getContent() { return content != null ? content : ""; }
    public void setContent(String content) { this.content = content; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskDataModel)) return false;
        TaskDataModel that = (TaskDataModel) o;
        return Objects.equals(id, that.id)
                && Objects.equals(title, that.title)
                && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() { return Objects.hash(id, title, content); }
}
