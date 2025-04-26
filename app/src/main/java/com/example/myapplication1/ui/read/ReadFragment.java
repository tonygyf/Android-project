package com.example.myapplication1.ui.read;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.myapplication1.R;
import com.example.myapplication1.data.Chapter;
import com.example.myapplication1.databinding.FragmentReadBinding;
import com.example.myapplication1.ui.reader.ReaderViewModel;
import com.example.myapplication1.utils.TxtFileParser;



public class ReadFragment extends Fragment {

    private FragmentReadBinding binding;
    private ReaderViewModel readerViewModel;
    private Chapter currentChapter;
    private int currentBookId;
    private String currentFilePath;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        readerViewModel = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);

        binding = FragmentReadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupNavigation();
        loadChapterContent();

        return root;
    }

    private void setupNavigation() {
        binding.btnPrevChapter.setOnClickListener(v -> navigateToPreviousChapter());
        binding.btnNextChapter.setOnClickListener(v -> navigateToNextChapter());
        binding.btnBackToList.setOnClickListener(v -> navigateBackToChapterList());
    }

    private void loadChapterContent() {
        if (getArguments() != null) {
            currentChapter = new Chapter(
                    getArguments().getInt("bookId"),
                    getArguments().getString("title", ""),
                    getArguments().getLong("startPosition"),
                    getArguments().getLong("endPosition"),
                    getArguments().getInt("chapterIndex")
            );
            currentChapter.setId(getArguments().getInt("chapterId"));
            currentBookId = currentChapter.getBookId();
            currentFilePath = getArguments().getString("filePath", "");

            binding.textChapterTitle.setText(currentChapter.getTitle());
            loadTextContent();
        }
    }

    private void loadTextContent() {
        if (currentFilePath == null || currentFilePath.isEmpty()) {
            binding.textContent.setText("无法加载章节内容，文件路径为空");
            return;
        }

        try {
            // 使用TxtFileParser读取章节内容
            String content;
            if (currentFilePath.startsWith("content:")) {
                // 处理URI
                Uri uri = Uri.parse(currentFilePath);
                content = TxtFileParser.readChapterContentFromUri(
                        uri,
                        getContext(),
                        currentChapter.getStartPosition(),
                        currentChapter.getEndPosition());
            } else {
                // 处理传统文件路径
                content = TxtFileParser.readChapterContent(
                        currentFilePath,
                        currentChapter.getStartPosition(),
                        currentChapter.getEndPosition());
            }
            
            binding.textContent.setText(content);
            
            // 更新阅读进度
            readerViewModel.updateReadingProgress(currentBookId, currentChapter.getStartPosition());
        } catch (Exception e) {
            e.printStackTrace();
            binding.textContent.setText("读取内容出错: " + e.getMessage());
        }
    }

    private void navigateToPreviousChapter() {
        readerViewModel.getPreviousChapter(currentBookId, currentChapter.getChapterIndex())
                .observe(getViewLifecycleOwner(), previousChapter -> {
                    if (previousChapter != null) {
                        Bundle args = new Bundle();
                        args.putInt("chapterId", previousChapter.getId());
                        args.putInt("bookId", previousChapter.getBookId());
                        args.putString("title", previousChapter.getTitle());
                        args.putLong("startPosition", previousChapter.getStartPosition());
                        args.putLong("endPosition", previousChapter.getEndPosition());
                        args.putInt("chapterIndex", previousChapter.getChapterIndex());
                        args.putString("filePath", currentFilePath);

                        Navigation.findNavController(requireView()).navigate(
                                R.id.nav_read, args);
                    } else {
                        Toast.makeText(getContext(), "已经是第一章", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToNextChapter() {
        readerViewModel.getNextChapter(currentBookId, currentChapter.getChapterIndex())
                .observe(getViewLifecycleOwner(), nextChapter -> {
                    if (nextChapter != null) {
                        Bundle args = new Bundle();
                        args.putInt("chapterId", nextChapter.getId());
                        args.putInt("bookId", nextChapter.getBookId());
                        args.putString("title", nextChapter.getTitle());
                        args.putLong("startPosition", nextChapter.getStartPosition());
                        args.putLong("endPosition", nextChapter.getEndPosition());
                        args.putInt("chapterIndex", nextChapter.getChapterIndex());
                        args.putString("filePath", currentFilePath);

                        Navigation.findNavController(requireView()).navigate(
                                R.id.nav_read, args);
                    } else {
                        Toast.makeText(getContext(), "已经是最后一章", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateBackToChapterList() {
        Navigation.findNavController(requireView()).navigate(R.id.nav_reader);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}