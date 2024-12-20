package com.example.languagetranslator.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;
    private long timestamp;
    private String language;

    public Note(String title, String content, String language) {
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.language = language;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}


