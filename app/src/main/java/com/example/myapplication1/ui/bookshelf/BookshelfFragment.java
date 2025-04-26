package com.example.myapplication1.ui.bookshelf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication1.databinding.FragmentBookshelfBinding;

public class BookshelfFragment extends Fragment {

    private FragmentBookshelfBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BookshelfViewModel bookshelfViewModel =
                new ViewModelProvider(this).get(BookshelfViewModel.class);

        binding = FragmentBookshelfBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerBooks;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}