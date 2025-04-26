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

    // 👉 必须加上一个无参构造方法，给 Room 和 new Chapter() 用
    public Chapter() {
    }

    // 👉 这是你原来的构造方法，创建新章节时方便使用
    public Chapter(int bookId, String title, long startPosition, long endPosition, int chapterIndex) {
        this.bookId = bookId;
        this.title = title;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.chapterIndex = chapterIndex;
    }

    // Getter 和 Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getStartPosition() { return startPosition; }
    public void setStartPosition(long startPosition) { this.startPosition = startPosition; }

    public long getEndPosition() { return endPosition; }
    public void setEndPosition(long endPosition) { this.endPosition = endPosition; }

    public int getChapterIndex() { return chapterIndex; }
    public void setChapterIndex(int chapterIndex) { this.chapterIndex = chapterIndex; }
}
