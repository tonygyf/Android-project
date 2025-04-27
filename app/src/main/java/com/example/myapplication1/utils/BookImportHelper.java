package com.example.myapplication1.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.myapplication1.data.Book;
import com.example.myapplication1.data.BookDatabase;
import com.example.myapplication1.data.Chapter;
import com.example.myapplication1.utils.TxtFileParser.ChapterInfo;

import java.io.File;
import java.util.List;

// 导入工具类，用于将TXT文件导入到数据库中

public class BookImportHelper {

    public static void importBook(Context context, String title, String filePath) {
        new Thread(() -> {
            try {
                BookDatabase db = BookDatabase.getDatabase(context);

                // 1. 检查是否已存在同名书籍
                List<Book> existingBooks = db.bookDao().getAllBooksSync();
                boolean exists = false;
                int bookId = 0;
                
                for (Book existingBook : existingBooks) {
                    if (existingBook.getTitle().equals(title)) {
                        exists = true;
                        bookId = existingBook.getId();
                        // 删除旧章节
                        db.bookDao().deleteChaptersByBookId(bookId);
                        break;
                    }
                }

                // 2. 创建或更新 Book 对象
                Book book = new Book(title, filePath);
                book.setFileSize(new File(filePath).length());
                book.setEncoding("UTF-8");
                
                if (exists) {
                    book.setId(bookId);
                    db.bookDao().updateBook(book);
                } else {
                    // 保存 Book，拿到生成的 bookId
                    bookId = (int) db.bookDao().insertBook(book);
                }

                // 3. 解析章节
                List<ChapterInfo> chapterInfos = TxtFileParser.parseChapters(filePath);

                // 4. 遍历章节，插入 Chapter 表
                for (ChapterInfo info : chapterInfos) {
                    Chapter chapter = new Chapter(
                        bookId,
                        info.title,
                        info.startPosition,
                        info.endPosition,
                        info.index
                    );
                    db.bookDao().insertChapter(chapter);
                }

                // 5. 导入完成，主线程提示
                final String message = exists ? "《" + title + "》更新完成！" : "《" + title + "》导入完成！";
                showToast(context, message);

            } catch (Exception e) {
                e.printStackTrace();
                showToast(context, "导入失败: " + e.getMessage());
            }
        }).start();
    }

    private static void showToast(Context context, String message) {
        // 因为子线程不能直接操作 UI，所以用 Handler 切到主线程
        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
        mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }
}
