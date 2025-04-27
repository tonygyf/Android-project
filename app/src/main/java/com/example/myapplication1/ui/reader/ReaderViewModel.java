package com.example.myapplication1.ui.reader;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication1.data.Book;
import com.example.myapplication1.data.BookDatabase;
import com.example.myapplication1.data.Chapter;
import com.example.myapplication1.utils.TxtFileParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

            // 获取文件大小 (可选，复制后大小可能变)
            final long fileSize;
            try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "r")) {
                fileSize = (pfd != null) ? pfd.getStatSize() : 0;
            }

            // 在这里不再直接创建 Book，而是先复制文件
            new Thread(() -> copyFileToInternalStorage(uri, fileName)).start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ReaderViewModel", "Error loading book URI", e);
        }
    }

    // 新增方法：复制文件到内部存储
    private void copyFileToInternalStorage(Uri sourceUri, String fileName) {
        Log.d("ReaderViewModel", "Copying file to internal storage: " + fileName);
        File internalFile = null;
        try {
            ContentResolver contentResolver = getApplication().getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(sourceUri);

            if (inputStream != null) {
                File internalDir = new File(getApplication().getFilesDir(), "books");
                if (!internalDir.exists()) {
                    internalDir.mkdirs();
                }
                internalFile = new File(internalDir, fileName);
                FileOutputStream outputStream = new FileOutputStream(internalFile);

                byte[] buffer = new byte[1024];
                int readBytes;
                while ((readBytes = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readBytes);
                }

                inputStream.close();
                outputStream.close();

                // 文件复制成功，保存书籍信息到数据库并解析章节
                String internalFilePath = internalFile.getAbsolutePath();
                long fileSize = internalFile.length(); // 获取复制后的文件大小
                Book book = new Book(fileName, internalFilePath); // 使用内部文件路径
                book.setFileSize(fileSize);
                book.setEncoding("UTF-8"); // 默认编码

                saveBookAndParseChapters(book, Uri.fromFile(internalFile)); // 使用内部文件 URI 进行章节解析

            } else {
                Log.e("ReaderViewModel", "Could not open input stream for URI: " + sourceUri.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ReaderViewModel", "Error copying file to internal storage", e);
        }
    }


    private void saveBookAndParseChapters(Book book, Uri uri) {
        Log.d("ReaderViewModel", "saveBookAndParseChapters called for book: " + book.getTitle());
        // 添加日志打印用于解析的 URI
        Log.d("ReaderViewModel", "URI used for chapter parsing: " + uri.toString());

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
                Log.d("ReaderViewModel", "Updating existing book and deleting old chapters for ID: " + book.getId());
                database.bookDao().deleteChaptersByBookId(matchedBook.getId());
                bookId = matchedBook.getId();
            } else {
                bookId = (int) database.bookDao().insertBook(book);
                book.setId(bookId);
                Log.d("ReaderViewModel", "Inserting new book with ID: " + book.getId());
            }

            currentBook.postValue(book);

            Log.d("ReaderViewModel", "Parsing chapters from URI: " + uri.toString());
            // 使用传递进来的内部文件 URI 进行章节解析
            List<TxtFileParser.ChapterInfo> chapterInfos = TxtFileParser.parseChaptersFromUri(uri, getApplication());

            if (chapterInfos != null) {
                Log.d("ReaderViewModel", "Parsed " + chapterInfos.size() + " chapter infos.");
            } else {
                Log.d("ReaderViewModel", "Chapter parsing returned null.");
            }

            List<Chapter> chapterList = new ArrayList<>();
            if (chapterInfos != null && !chapterInfos.isEmpty()) { // Ensure chapterInfos is not null and not empty
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
                Log.d("ReaderViewModel", "Finished inserting " + chapterList.size() + " chapters for book ID: " + bookId);
            } else {
                Log.d("ReaderViewModel", "No chapter infos to insert for book ID: " + bookId);
            }

            chapters.postValue(chapterList);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ReaderViewModel", "Error saving book and parsing chapters", e);
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

    // 新增两个外部调用方法（用于 Fragment 设置）
    public void setCurrentBook(Book book) {
        currentBook.postValue(book);
    }

    public void setChapters(List<Chapter> chapterList) {
        chapters.postValue(chapterList);
    }
}