package com.example.myapplication1.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.myapplication1.data.Book;
import com.example.myapplication1.data.BookDatabase;
import com.example.myapplication1.data.Chapter;
import com.example.myapplication1.utils.TxtFileParser.ChapterInfo;

import java.io.File;
import java.util.List;

public class BookImportHelper {

    public static void importBook(Context context, String title, String filePath) {
        new Thread(() -> {
            try {
                BookDatabase db = BookDatabase.getDatabase(context);

                // 1. 创建 Book 对象
                Book book = new Book(title, filePath);
                book.setFileSize(new File(filePath).length());
                book.setEncoding("UTF-8");

                // 2. 保存 Book，拿到生成的 bookId
                long bookId = db.bookDao().insertBook(book);

                // 3. 解析章节
                List<ChapterInfo> chapterInfos = TxtFileParser.parseChapters(filePath);

                // 4. 遍历章节，插入 Chapter 表
                for (ChapterInfo info : chapterInfos) {
                    Chapter chapter = new Chapter();
                    chapter.setBookId((int) bookId); // 注意转成 int
                    chapter.setTitle(info.title);    // 使用 setTitle 正确方法
                    chapter.setStartPosition(info.startPosition);
                    chapter.setEndPosition(info.endPosition);
                    chapter.setChapterIndex(info.index);

                    db.bookDao().insertChapter(chapter);
                }

                // 5. 导入完成，主线程提示
                showToast(context, "《" + title + "》导入完成！");

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
