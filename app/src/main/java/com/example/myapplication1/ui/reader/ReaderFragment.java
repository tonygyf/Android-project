package com.example.myapplication1.ui.reader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Import Log class
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication1.R;
import com.example.myapplication1.data.Book;
import com.example.myapplication1.data.BookDatabase;
import com.example.myapplication1.data.Chapter;
import com.example.myapplication1.databinding.FragmentReaderBinding;
import com.example.myapplication1.ui.bookshelf.BookshelfViewModel;

import java.util.List;

public class ReaderFragment extends Fragment {

    private FragmentReaderBinding binding;
    private ReaderViewModel readerViewModel;
    private ChapterAdapter chapterAdapter;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            String mimeType = requireContext().getContentResolver().getType(uri);
                            if (mimeType != null && mimeType.equals("text/plain")) {
                                readerViewModel.loadBook(uri.toString());
                            } else {
                                Toast.makeText(getContext(), "请选择TXT文件", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "文件读取错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        readerViewModel = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);

        binding = FragmentReaderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        setupFileSelection();
        observeViewModel();
        loadSelectedBook();

        return root;
    }

    private void loadSelectedBook() {
        BookshelfViewModel bookshelfViewModel = new ViewModelProvider(requireActivity()).get(BookshelfViewModel.class);
        bookshelfViewModel.getSelectedBook().observe(getViewLifecycleOwner(), book -> {
            if (book != null) {
                // 添加日志确认接收到选中的书籍
                Log.d("ReaderFragment", "Received selected book: " + book.getTitle() + ", ID: " + book.getId() + ", FilePath: " + book.getFilePath());

                readerViewModel.setCurrentBook(book);
                new Thread(() -> {
                    BookDatabase database = BookDatabase.getDatabase(requireContext());
                    List<Chapter> chapters = database.bookDao().getChaptersByBookIdSync(book.getId());
                    if (chapters != null && !chapters.isEmpty()) {
                        // 添加日志确认加载到章节
                        Log.d("ReaderFragment", "Loaded " + chapters.size() + " chapters for book: " + book.getTitle());
                        readerViewModel.setChapters(chapters);
                    } else {
                        // 添加日志说明未加载到章节
                        Log.d("ReaderFragment", "No chapters loaded for book: " + book.getTitle());
                    }
                }).start();
            } else {
                // 添加日志说明未接收到书籍
                Log.d("ReaderFragment", "No book selected in BookshelfViewModel.");
            }
        });
    }

    private void setupRecyclerView() {
        chapterAdapter = new ChapterAdapter();
        binding.recyclerChapters.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerChapters.setAdapter(chapterAdapter);

        chapterAdapter.setOnChapterClickListener(chapter -> {
            Book book = readerViewModel.getCurrentBook().getValue();
            if (book != null) {
                Bundle args = new Bundle();
                args.putInt("chapterId", chapter.getId());
                args.putInt("bookId", chapter.getBookId());
                args.putString("title", chapter.getTitle());
                args.putLong("startPosition", chapter.getStartPosition());
                args.putLong("endPosition", chapter.getEndPosition());
                args.putInt("chapterIndex", chapter.getChapterIndex());
                args.putString("filePath", book.getFilePath());

                // 添加日志打印发送的 Bundle 内容
                Log.d("ReaderFragment", "Navigating to nav_read with args:");
                for (String key : args.keySet()) {
                    Log.d("ReaderFragment", "Key: " + key + ", Value: " + args.get(key));
                }

                Navigation.findNavController(requireView()).navigate(R.id.nav_read, args);
            }
        });
    }

    private void setupFileSelection() {
        binding.btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(intent);
        });
    }

    private void observeViewModel() {
        readerViewModel.getCurrentBook().observe(getViewLifecycleOwner(), book -> {
            if (book != null) {
                binding.textCurrentBook.setText("当前书籍: " + book.getTitle());
            }
        });

        readerViewModel.getChapters().observe(getViewLifecycleOwner(), chapters -> {
            if (chapters != null) {
                chapterAdapter.setChapters(chapters);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}