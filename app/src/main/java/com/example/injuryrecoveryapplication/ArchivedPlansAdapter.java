package com.example.injuryrecoveryapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ArchivedPlansAdapter extends RecyclerView.Adapter<ArchivedPlansAdapter.ViewHolder> {

    private static final String TAG = "ArchivedPlansAdapter";
    private List<ArchivedPlan> archivedPlans;
    private OnPlanClickListener onPlanClickListener;

    // Interface for click events on an archived plan
    public interface OnPlanClickListener {
        void onPlanClick(ArchivedPlan plan);
    }

    public ArchivedPlansAdapter(List<ArchivedPlan> archivedPlans, OnPlanClickListener listener) {
        this.archivedPlans = archivedPlans;
        this.onPlanClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_archived_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArchivedPlan plan = archivedPlans.get(position);
        holder.injuryAreaTextView.setText("Area: " + plan.getInjuryArea());
        holder.injuryTypeTextView.setText("Type: " + plan.getInjuryType());
        holder.archiveDateTextView.setText("Archived on: " + plan.getArchivedDate());
        Log.d(TAG, "Binding archived plan at position " + position + ": " + plan.getPlanId());

        // Set click listener on the entire item
        holder.itemView.setOnClickListener(v -> {
            if (onPlanClickListener != null) {
                onPlanClickListener.onPlanClick(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return archivedPlans.size();
    }

    // ViewHolder class for archived plan items
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView injuryAreaTextView;
        TextView injuryTypeTextView;
        TextView archiveDateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            injuryAreaTextView = itemView.findViewById(R.id.textViewInjuryArea);
            injuryTypeTextView = itemView.findViewById(R.id.textViewInjuryType);
            archiveDateTextView = itemView.findViewById(R.id.textViewArchiveDate);
        }
    }
}
