package com.example.injuryrecoveryapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WeeklyPlanAdapter extends RecyclerView.Adapter<WeeklyPlanAdapter.WeekViewHolder> {

    private List<Integer> weekNumbers;
    private OnWeekClickListener listener;

    public interface OnWeekClickListener {
        void onWeekClick(int weekNumber);
    }

    public WeeklyPlanAdapter(List<Integer> weekNumbers, OnWeekClickListener listener) {
        this.weekNumbers = weekNumbers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        int week = weekNumbers.get(position);
        holder.weekTextView.setText("Week " + week);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWeekClick(week);
            }
        });
    }

    @Override
    public int getItemCount() {
        return weekNumbers.size();
    }

    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView weekTextView;
        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            weekTextView = itemView.findViewById(R.id.weekTextView);
        }
    }
}

