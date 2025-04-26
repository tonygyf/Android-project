package com.example.myapplication1.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "chapters",
        foreignKeys = @ForeignKey(entity = Book.class,
                parentColumns = "id",
                childColumns = "bookId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("bookId")})
public class Chapter {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int bookId;
    private String title;
    private long startPosition;
    private long endPosition;
    private int chapterIndex;

    public Chapter(int bookId, String title, long startPosition, long endPosition, int chapterIndex) {
        this.bookId = bookId;
        this.title = title;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.chapterIndex = chapterIndex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getStartPosition() { return startPosition; }
    public void setStartPosition(long position) { this.startPosition = position; }

    public long getEndPosition() { return endPosition; }
    public void setEndPosition(long position) { this.endPosition = position; }

    public int getChapterIndex() { return chapterIndex; }
    public void setChapterIndex(int index) { this.chapterIndex = index; }
}