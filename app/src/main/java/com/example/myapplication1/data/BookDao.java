package com.example.myapplication1.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    long insertBook(Book book);

    @Update
    void updateBook(Book book);

    @Delete
    void deleteBook(Book book);

    @Query("SELECT * FROM books")
    LiveData<List<Book>> getAllBooks();

    @Query("SELECT * FROM books WHERE id = :bookId")
    LiveData<Book> getBookById(int bookId);
    
    @Query("SELECT * FROM books WHERE id = :bookId")
    Book getBookByIdSync(int bookId);

    @Insert
    void insertChapter(Chapter chapter);

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex")
    LiveData<List<Chapter>> getChaptersByBookId(int bookId);
    
    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND chapterIndex < :currentIndex ORDER BY chapterIndex DESC LIMIT 1")
    Chapter getPreviousChapter(int bookId, int currentIndex);
    
    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND chapterIndex > :currentIndex ORDER BY chapterIndex ASC LIMIT 1")
    Chapter getNextChapter(int bookId, int currentIndex);

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    void deleteChaptersByBookId(int bookId);
}