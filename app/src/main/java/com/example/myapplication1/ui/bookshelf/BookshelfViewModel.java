package com.example.myapplication1.ui.bookshelf;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class BookshelfViewModel extends ViewModel {
    private MutableLiveData<List<com.example.myapplication1.data.Book>> books;
    private MutableLiveData<com.example.myapplication1.data.Book> selectedBook;

    public BookshelfViewModel() {
        books = new MutableLiveData<>();
        selectedBook = new MutableLiveData<>();
        books.setValue(new ArrayList<>()); // 初始化为空列表
    }

    public LiveData<List<com.example.myapplication1.data.Book>> getBooks() {
        return books;
    }

    public LiveData<com.example.myapplication1.data.Book> getSelectedBook() {
        return selectedBook;
    }

    public void setBooks(List<com.example.myapplication1.data.Book> bookList) {
        books.setValue(bookList);
    }

    public void selectBook(com.example.myapplication1.data.Book book) {
        selectedBook.setValue(book);
    }
}