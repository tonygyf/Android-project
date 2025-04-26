package com.example.myapplication1.ui.reader;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication1.data.Book;
import com.example.myapplication1.data.BookDatabase;
import com.example.myapplication1.data.Chapter;
import com.example.myapplication1.utils.TxtFileParser;

import java.util.ArrayList;
import java.util.List;

public class ReaderViewModel extends AndroidViewModel {
    private final BookDatabase database;
    private final MutableLiveData<Book> currentBook = new MutableLiveData<>();
    private final MutableLiveData<List<Chapter>> chapters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Chapter> selectedChapter = new MutableLiveData<>();

    public ReaderViewModel(Application application) {
        super(application);
        database = BookDatabase.getDatabase(application);
    }

    public void loadBook(String uriString) {
        try {
            Uri uri = Uri.parse(uriString);
            ContentResolver contentResolver = getApplication().getContentResolver();

            // 获取文件名
            String fileName = "未知文件";
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }

            // 获取文件大小
            long fileSize = 0;
            try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "r")) {
                if (pfd != null) {
                    fileSize = pfd.getStatSize();
                }
            }

            Book book = new Book(fileName, uriString);
            book.setFileSize(fileSize);

            new Thread(() -> {
                long bookId = database.bookDao().insertBook(book);
                book.setId((int) bookId);
                currentBook.postValue(book);

                // 解析章节
                List<TxtFileParser.ChapterInfo> chapterInfos = TxtFileParser.parseChaptersFromUri(uri, getApplication());
                List<Chapter> chapterList = new ArrayList<>();
                for (TxtFileParser.ChapterInfo info : chapterInfos) {
                    Chapter chapter = new Chapter(
                            (int) bookId,
                            info.title,
                            info.startPosition,
                            info.endPosition,
                            info.index
                    );
                    database.bookDao().insertChapter(chapter);
                    chapterList.add(chapter);
                }
                chapters.postValue(chapterList); // 把解析后的章节列表同步到LiveData
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LiveData<Book> getCurrentBook() {
        return currentBook;
    }

    public LiveData<List<Chapter>> getChapters() {
        return chapters;
    }

    public void updateReadingProgress(int bookId, long position) {
        new Thread(() -> {
            Book book = database.bookDao().getBookByIdSync(bookId);
            if (book != null) {
                book.setLastReadPosition(position);
                database.bookDao().updateBook(book);
            }
        }).start();
    }

    public LiveData<Chapter> getPreviousChapter(int bookId, int currentIndex) {
        MutableLiveData<Chapter> result = new MutableLiveData<>();
        new Thread(() -> {
            Chapter previousChapter = database.bookDao().getPreviousChapter(bookId, currentIndex);
            result.postValue(previousChapter);
        }).start();
        return result;
    }

    public LiveData<Chapter> getNextChapter(int bookId, int currentIndex) {
        MutableLiveData<Chapter> result = new MutableLiveData<>();
        new Thread(() -> {
            Chapter nextChapter = database.bookDao().getNextChapter(bookId, currentIndex);
            result.postValue(nextChapter);
        }).start();
        return result;
    }

    public LiveData<Chapter> getSelectedChapter() {
        return selectedChapter;
    }

    public void selectChapter(Chapter chapter) {
        selectedChapter.setValue(chapter);
    }
}
