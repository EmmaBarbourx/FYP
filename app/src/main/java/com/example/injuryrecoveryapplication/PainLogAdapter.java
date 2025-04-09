package com.example.injuryrecoveryapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PainLogAdapter extends RecyclerView.Adapter<PainLogAdapter.PainLogViewHolder> {

    private final List<PainLog> painLogs;

    public PainLogAdapter(List<PainLog> painLogs) {
        this.painLogs = painLogs;
    }

    @NonNull
    @Override
    public PainLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the pain log item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pain_log, parent, false);
        return new PainLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PainLogViewHolder holder, int position) {
        PainLog painLog = painLogs.get(position);

        // Format timestamp to a readable date and time
        long timestamp = Long.parseLong(painLog.getTimestamp());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(timestamp));

        holder.dateTextView.setText("Date: " + formattedDate);

        // Change color dynamically for pain levels
        int painLevel = Integer.parseInt(painLog.getPainLevel());
        holder.painLevelTextView.setText("Pain Level: " + painLevel);

        if (painLevel >= 7) {
            holder.painLevelTextView.setTextColor(Color.parseColor("#D32F2F")); // Red for high pain
        } else if (painLevel >= 4) {
            holder.painLevelTextView.setTextColor(Color.parseColor("#FFA000")); // Orange for moderate pain
        } else {
            holder.painLevelTextView.setTextColor(Color.parseColor("#388E3C")); // Green for low pain
        }

        holder.notesTextView.setText("Notes: " + (painLog.getNotes().isEmpty() ? "None" : painLog.getNotes()));
    }

    @Override
    public int getItemCount() {
        return painLogs.size();
    }

    static class PainLogViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView painLevelTextView;
        TextView notesTextView;

        public PainLogViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            painLevelTextView = itemView.findViewById(R.id.painLevelTextView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
        }
    }
}
