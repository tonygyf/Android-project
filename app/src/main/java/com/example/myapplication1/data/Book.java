package com.example.myapplication1.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String filePath;
    private long lastReadPosition;
    private long fileSize;
    private String encoding;

    public Book(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
        this.lastReadPosition = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getLastReadPosition() { return lastReadPosition; }
    public void setLastReadPosition(long position) { this.lastReadPosition = position; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long size) { this.fileSize = size; }

    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
}