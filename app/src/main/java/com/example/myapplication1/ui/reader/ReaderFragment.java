package com.example.myapplication1.ui.reader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
                readerViewModel.setCurrentBook(book);
                BookDatabase database = BookDatabase.getDatabase(requireContext());
                database.bookDao().getChaptersByBookId(book.getId()).observe(getViewLifecycleOwner(), chapters -> {
                    if (chapters != null && !chapters.isEmpty()) {
                        readerViewModel.setChapters(chapters);
                    }
                });
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
