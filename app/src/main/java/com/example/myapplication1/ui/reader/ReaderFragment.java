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
                            Log.e("ReaderFragment", "File reading error during file picking", e); // Add error log
                            Toast.makeText(getContext(), "文件读取错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("ReaderFragment", "onCreateView started.");
        readerViewModel = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);

        binding = FragmentReaderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        setupFileSelection();
        observeViewModel();
        loadSelectedBook(); // Call loadSelectedBook here

        // 添加日志打印 LiveData 的值在 onCreateView 中
        BookshelfViewModel bookshelfViewModel = new ViewModelProvider(requireActivity()).get(BookshelfViewModel.class);
        Log.d("ReaderFragment", "Selected book LiveData value in onCreateView: " + bookshelfViewModel.getSelectedBook().getValue());


        Log.d("ReaderFragment", "onCreateView finished.");
        return root;
    }

    private void loadSelectedBook() {
        Log.d("ReaderFragment", "loadSelectedBook called."); // Add log for loadSelectedBook call
        BookshelfViewModel bookshelfViewModel = new ViewModelProvider(requireActivity()).get(BookshelfViewModel.class);
        bookshelfViewModel.getSelectedBook().observe(getViewLifecycleOwner(), book -> {
            // 添加日志，确认观察者是否被触发
            Log.d("ReaderFragment", "Selected book observer triggered.");

            if (book != null) {
                // 添加日志确认接收到选中的书籍
                Log.d("ReaderFragment", "Received selected book: " + book.getTitle() + ", ID: " + book.getId() + ", FilePath: " + book.getFilePath());

                readerViewModel.setCurrentBook(book);
                new Thread(() -> {
                    Log.d("ReaderFragment", "Loading chapters in background thread."); // Add log for background task
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
                Log.d("ReaderFragment", "No book selected in BookshelfViewModel (book is null).");
            }
        });
    }

    private void setupRecyclerView() {
        Log.d("ReaderFragment", "setupRecyclerView called."); // Add log
        chapterAdapter = new ChapterAdapter();
        binding.recyclerChapters.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerChapters.setAdapter(chapterAdapter);

        chapterAdapter.setOnChapterClickListener(chapter -> {
            Log.d("ReaderFragment", "Chapter clicked: " + chapter.getTitle()); // Add log for chapter click
            Book book = readerViewModel.getCurrentBook().getValue();
            if (book != null) {
                Bundle args = new Bundle();
                args.putInt("chapterId", chapter.getId());
                args.putInt("bookId", book.getId()); // Use book.getId() here
                args.putString("title", chapter.getTitle());
                args.putLong("startPosition", chapter.getStartPosition());
                args.putLong("endPosition", chapter.getEndPosition());
                args.putInt("chapterIndex", chapter.getChapterIndex());
                args.putString("filePath", book.getFilePath());

                // Add logging for the Bundle being sent
                Log.d("ReaderFragment", "Navigating to nav_read with args:");
                for (String key : args.keySet()) {
                    Log.d("ReaderFragment", "Key: " + key + ", Value: " + args.get(key));
                }

                Navigation.findNavController(requireView()).navigate(R.id.nav_read, args);
            } else {
                Log.d("ReaderFragment", "Cannot navigate to nav_read, current book is null.");
            }
        });
    }

    private void setupFileSelection() {
        Log.d("ReaderFragment", "setupFileSelection called."); // Add log
        binding.btnSelectFile.setOnClickListener(v -> {
            Log.d("ReaderFragment", "Select File button clicked."); // Add log
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(intent);
        });
    }

    private void observeViewModel() {
        Log.d("ReaderFragment", "observeViewModel called."); // Add log
        readerViewModel.getCurrentBook().observe(getViewLifecycleOwner(), book -> {
            Log.d("ReaderFragment", "Current book observer triggered."); // Add log
            if (book != null) {
                binding.textCurrentBook.setText("当前书籍: " + book.getTitle());
            } else {
                binding.textCurrentBook.setText("当前书籍: 未选择");
            }
        });

        readerViewModel.getChapters().observe(getViewLifecycleOwner(), chapters -> {
            Log.d("ReaderFragment", "Chapters observer triggered."); // Add log
            if (chapters != null) {
                // 添加日志确认章节数据已设置给 Adapter
                Log.d("ReaderFragment", "Chapters updated in adapter, count: " + chapters.size());
                chapterAdapter.setChapters(chapters);
            } else {
                // 添加日志说明章节数据为 null
                Log.d("ReaderFragment", "Chapters LiveData is null.");
                chapterAdapter.setChapters(null); // Clear adapter if chapters become null
            }
        });
    }

    @Override
    public void onDestroyView() {
        Log.d("ReaderFragment", "onDestroyView called."); // Add log
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("ReaderFragment", "onResume called.");
        // 添加日志打印 LiveData 的值在 onResume 中
        BookshelfViewModel bookshelfViewModel = new ViewModelProvider(requireActivity()).get(BookshelfViewModel.class);
        Log.d("ReaderFragment", "Selected book LiveData value in onResume: " + bookshelfViewModel.getSelectedBook().getValue());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("ReaderFragment", "onPause called."); // Add log
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("ReaderFragment", "onStop called."); // Add log
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("ReaderFragment", "onStart called.");
        // 添加日志打印 LiveData 的值在 onStart 中
        BookshelfViewModel bookshelfViewModel = new ViewModelProvider(requireActivity()).get(BookshelfViewModel.class);
        Log.d("ReaderFragment", "Selected book LiveData value in onStart: " + bookshelfViewModel.getSelectedBook().getValue());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ReaderFragment", "onDestroy called."); // Add log
    }

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ReaderFragment", "onViewCreated called."); // Add log
    }
}