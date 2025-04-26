package com.example.myapplication1.ui.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication1.R;
import com.example.myapplication1.data.Chapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
    private List<Chapter> chapters = new ArrayList<>();
    private OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(Chapter chapter);
    }

    public void setOnChapterClickListener(OnChapterClickListener listener) {
        this.listener = listener;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        holder.bind(chapter);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;

        ChapterViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(android.R.id.text1);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onChapterClick(chapters.get(position));
                }
            });
        }

        void bind(Chapter chapter) {
            titleView.setText(chapter.getTitle());
        }
    }
}