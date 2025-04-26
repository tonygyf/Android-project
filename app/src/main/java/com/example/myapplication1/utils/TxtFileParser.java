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
                position += line.length() + System.lineSeparator().length();
            }

            // 添加最后一章
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
                position += line.length() + System.lineSeparator().length();
            }

            // 添加最后一章
            if (lastTitle != null) {
                chapters.add(new ChapterInfo(lastTitle, lastChapterStart, position, chapterIndex));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chapters;
    }
    
    public static String readChapterContent(String filePath, long startPosition, long endPosition) {
        // 检查是否是URI字符串
        if (filePath.startsWith("content:")) {
            try {
                Uri uri = Uri.parse(filePath);
                return readChapterContentFromUri(uri, null, startPosition, endPosition);
            } catch (Exception e) {
                e.printStackTrace();
                return "读取章节内容出错: " + e.getMessage();
            }
        }
        
        // 传统文件路径处理
        File file = new File(filePath);
        if (!file.exists()) return "文件不存在";
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            reader.skip(startPosition);
            long remainingBytes = endPosition - startPosition;
            char[] buffer = new char[1024];
            int read;

            while (remainingBytes > 0 && (read = reader.read(buffer, 0, (int) Math.min(buffer.length, remainingBytes))) != -1) {
                content.append(buffer, 0, read);
                remainingBytes -= read;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "读取章节内容出错: " + e.getMessage();
        }
        
        return content.toString();
    }
    
    public static String readChapterContentFromUri(Uri uri, Context context, long startPosition, long endPosition) {
        if (uri == null) return "URI为空";
        
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = context != null ? 
                context.getContentResolver().openInputStream(uri) : 
                new java.net.URL(uri.toString()).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            reader.skip(startPosition);
            long remainingBytes = endPosition - startPosition;
            char[] buffer = new char[1024];
            int read;

            while (remainingBytes > 0 && (read = reader.read(buffer, 0, (int) Math.min(buffer.length, remainingBytes))) != -1) {
                content.append(buffer, 0, read);
                remainingBytes -= read;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "读取章节内容出错: " + e.getMessage();
        }
        
        return content.toString();
    }
}