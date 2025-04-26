package com.example.myapplication1.utils;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TxtFileParser {
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("^第[0-9零一二三四五六七八九十百千]+[章节卷集].*");

    public static class ChapterInfo {
        public String title;
        public long startPosition;
        public long endPosition;
        public int index;

        public ChapterInfo(String title, long startPosition, long endPosition, int index) {
            this.title = title;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.index = index;
        }
    }

    // 解析章节，基于字节长度记录
    public static List<ChapterInfo> parseChapters(String filePath) {
        List<ChapterInfo> chapters = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return chapters;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            long position = 0;
            long lastChapterStart = 0;
            int chapterIndex = 0;
            String lastTitle = null;

            while ((line = reader.readLine()) != null) {
                if (CHAPTER_PATTERN.matcher(line.trim()).matches()) {
                    if (lastTitle != null) {
                        chapters.add(new ChapterInfo(lastTitle, lastChapterStart, position, chapterIndex++));
                    }
                    lastTitle = line.trim();
                    lastChapterStart = position;
                }
                // 用UTF-8字节数
                position += line.getBytes("UTF-8").length + System.lineSeparator().getBytes("UTF-8").length;
            }

            if (lastTitle != null) {
                chapters.add(new ChapterInfo(lastTitle, lastChapterStart, position, chapterIndex));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chapters;
    }

    public static List<ChapterInfo> parseChaptersFromUri(Uri uri, Context context) {
        List<ChapterInfo> chapters = new ArrayList<>();
        if (uri == null) return chapters;

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            long position = 0;
            long lastChapterStart = 0;
            int chapterIndex = 0;
            String lastTitle = null;

            while ((line = reader.readLine()) != null) {
                if (CHAPTER_PATTERN.matcher(line.trim()).matches()) {
                    if (lastTitle != null) {
                        chapters.add(new ChapterInfo(lastTitle, lastChapterStart, position, chapterIndex++));
                    }
                    lastTitle = line.trim();
                    lastChapterStart = position;
                }
                position += line.getBytes("UTF-8").length + System.lineSeparator().getBytes("UTF-8").length;
            }

            if (lastTitle != null) {
                chapters.add(new ChapterInfo(lastTitle, lastChapterStart, position, chapterIndex));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chapters;
    }

    // 按范围读取内容
    public static String readChapterContent(String filePath, long startPosition, long endPosition) {
        File file = new File(filePath);
        if (!file.exists()) return "文件不存在";

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            long currentPos = 0;
            while ((line = reader.readLine()) != null) {
                long lineByteLength = line.getBytes("UTF-8").length + System.lineSeparator().getBytes("UTF-8").length;
                if (currentPos >= startPosition && currentPos < endPosition) {
                    content.append(line).append("\n");
                }
                currentPos += lineByteLength;
                if (currentPos >= endPosition) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "读取章节内容出错: " + e.getMessage();
        }
        return content.toString();
    }

    public static String readChapterContentFromUri(Uri uri, Context context, long startPosition, long endPosition) {
        if (uri == null || context == null) return "URI或Context为空";

        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            long currentPos = 0;
            while ((line = reader.readLine()) != null) {
                long lineByteLength = line.getBytes("UTF-8").length + System.lineSeparator().getBytes("UTF-8").length;
                if (currentPos >= startPosition && currentPos < endPosition) {
                    content.append(line).append("\n");
                }
                currentPos += lineByteLength;
                if (currentPos >= endPosition) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "读取章节内容出错: " + e.getMessage();
        }
        return content.toString();
    }
}
