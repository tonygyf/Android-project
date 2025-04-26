package com.example.myapplication1.ui.chapterpreview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChapterPreviewViewModel extends ViewModel {
    private MutableLiveData<List<com.example.myapplication1.data.Chapter>> chapters;
    private int currentBookId;

    public ChapterPreviewViewModel() {
        chapters = new MutableLiveData<>();
        chapters.setValue(new ArrayList<>()); // 初始化为空列表
    }

    public LiveData<List<com.example.myapplication1.data.Chapter>> getChapters() {
        return chapters;
    }

    public void setBookId(int bookId) {
        this.currentBookId = bookId;
    }

    public int getBookId() {
        return currentBookId;
    }

    public void setChapters(List<com.example.myapplication1.data.Chapter> chapterList) {
        chapters.setValue(chapterList);
    }
}