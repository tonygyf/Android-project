package com.example.myapplication1.ui.bookshelf;

import android.os.Bundle;
import android.util.Log; // 导入 Log 类
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication1.R;
import com.example.myapplication1.data.Book;
import com.example.myapplication1.databinding.FragmentBookshelfBinding;

public class BookshelfFragment extends Fragment {

    private FragmentBookshelfBinding binding;
    private BookshelfViewModel bookshelfViewModel;
    private BookAdapter bookAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        修改这个实例过程，用以使得页面跳转中activity作用域一致
        bookshelfViewModel = new ViewModelProvider(requireActivity()).get(BookshelfViewModel.class);

        binding = FragmentBookshelfBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        observeViewModel();

        return root;
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter();
        binding.recyclerBooks.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.recyclerBooks.setAdapter(bookAdapter);

        bookAdapter.setOnBookClickListener(book -> {
            // 选中书籍，跳转到阅读器界面
            bookshelfViewModel.selectBook(book);
//            增加打印信息
            Log.d("BookshelfFragment", "Selected book: " + book.getTitle() + ", ID: " + book.getId() + ", FilePath: " + book.getFilePath());
            // 添加日志打印 LiveData 的值在导航前
            // Log.d("BookshelfFragment", "Selected book LiveData value before navigation: " + bookshelfViewModel.getSelectedBook().getValue()); // 调试日志，可以保留或移除
            Navigation.findNavController(requireView()).navigate(R.id.nav_reader);
        });
    }

    private void observeViewModel() {
        bookshelfViewModel.getBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                bookAdapter.setBooks(books);
                if (books.isEmpty()) {
                    Toast.makeText(getContext(), "书架为空，请在阅读器中导入书籍", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 添加日志，打印当前导航目的地的标签
        if (Navigation.findNavController(requireView()).getCurrentDestination() != null) {
            Log.d("BookshelfFragment", "Current destination label in onResume: " + Navigation.findNavController(requireView()).getCurrentDestination().getLabel());
        } else {
            Log.d("BookshelfFragment", "Current destination is null in onResume.");
        }
    }
}