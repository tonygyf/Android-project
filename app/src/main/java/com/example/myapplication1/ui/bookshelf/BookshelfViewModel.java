package com.example.myapplication1.ui.bookshelf;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication1.data.Book;
import com.example.myapplication1.data.BookDatabase;

import java.util.ArrayList;
import java.util.List;

public class BookshelfViewModel extends AndroidViewModel {
    private final BookDatabase database;
    private final LiveData<List<Book>> books;
    private final MutableLiveData<Book> selectedBook;

    public BookshelfViewModel(Application application) {
        super(application);
        database = BookDatabase.getDatabase(application);
        books = database.bookDao().getAllBooks(); // 直接从数据库加载所有书籍
        selectedBook = new MutableLiveData<>();
    }

    public LiveData<List<Book>> getBooks() {
        return books;
    }

    public LiveData<Book> getSelectedBook() {
        return selectedBook;
    }

    public void selectBook(Book book) {
        selectedBook.postValue(book);
    }
    
    public void deleteBook(Book book) {
        new Thread(() -> {
            database.bookDao().deleteChaptersByBookId(book.getId());
            database.bookDao().deleteBook(book);
        }).start();
    }
}