package com.example.myapplication1.ui.chapterpreview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication1.databinding.FragmentChapterPreviewBinding;

public class ChapterPreviewFragment extends Fragment {

    private FragmentChapterPreviewBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ChapterPreviewViewModel chapterPreviewViewModel =
                new ViewModelProvider(this).get(ChapterPreviewViewModel.class);

        binding = FragmentChapterPreviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ViewPager2 viewPager = binding.viewPager;
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}