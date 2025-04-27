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
            final Uri uri = Uri.parse(uriString);
            final ContentResolver contentResolver = getApplication().getContentResolver();

            // 获取文件名
            final String fileName;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    fileName = nameIndex != -1 ? cursor.getString(nameIndex) : "未知文件";
                } else {
                    fileName = "未知文件";
                }
            }

            // 获取文件大小
            final long fileSize;
            try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "r")) {
                fileSize = (pfd != null) ? pfd.getStatSize() : 0;
            }

            final Book book = new Book(fileName, uriString);
            book.setFileSize(fileSize);
            book.setEncoding("UTF-8"); // 默认编码

            new Thread(() -> saveBookAndParseChapters(book, uri)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBookAndParseChapters(Book book, Uri uri) {
        try {
            List<Book> existingBooks = database.bookDao().getAllBooksSync();
            Book matchedBook = null;
            for (Book existing : existingBooks) {
                if (existing.getTitle().equals(book.getTitle())) {
                    matchedBook = existing;
                    break;
                }
            }

            int bookId;
            if (matchedBook != null) {
                book.setId(matchedBook.getId());
                database.bookDao().updateBook(book);
                database.bookDao().deleteChaptersByBookId(matchedBook.getId());
                bookId = matchedBook.getId();
            } else {
                bookId = (int) database.bookDao().insertBook(book);
                book.setId(bookId);
            }

            currentBook.postValue(book);

            List<TxtFileParser.ChapterInfo> chapterInfos = TxtFileParser.parseChaptersFromUri(uri, getApplication());
            List<Chapter> chapterList = new ArrayList<>();
            for (TxtFileParser.ChapterInfo info : chapterInfos) {
                Chapter chapter = new Chapter(
                        bookId,
                        info.title,
                        info.startPosition,
                        info.endPosition,
                        info.index
                );
                database.bookDao().insertChapter(chapter);
                chapterList.add(chapter);
            }

            chapters.postValue(chapterList);

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
        selectedChapter.postValue(chapter);
    }

    // 👉 新增两个外部调用方法（用于 Fragment 设置）
    public void setCurrentBook(Book book) {
        currentBook.setValue(book);
    }

    public void setChapters(List<Chapter> chapterList) {
        chapters.setValue(chapterList);
    }
}
