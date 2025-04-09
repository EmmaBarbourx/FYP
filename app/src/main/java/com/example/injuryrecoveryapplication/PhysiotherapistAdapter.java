package com.example.injuryrecoveryapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.injuryrecoveryapplication.models.Physiotherapist;

import java.util.List;

public class PhysiotherapistAdapter extends RecyclerView.Adapter<PhysiotherapistAdapter.PhysiotherapistViewHolder> {

    private List<Physiotherapist> physiotherapists;

    public PhysiotherapistAdapter(List<Physiotherapist> physiotherapists) {
        this.physiotherapists = physiotherapists;
    }

    @NonNull
    @Override
    public PhysiotherapistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_physiotherapist, parent, false);
        return new PhysiotherapistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhysiotherapistViewHolder holder, int position) {
        Physiotherapist physio = physiotherapists.get(position);// Get the physiotherapist for the current position
        holder.nameTextView.setText(physio.getName());
        holder.addressTextView.setText(physio.getAddress());
        holder.specialtyTextView.setText(physio.getSpecialty());

        // Check if there are any categories to display
        if (physio.getCategories() != null && !physio.getCategories().isEmpty()) {
            StringBuilder categoriesBuilder = new StringBuilder();
            for (Physiotherapist.Category category : physio.getCategories()) {
                categoriesBuilder.append(category.getTitle()).append(", ");
            }
            // Remove the extra comma and space at the end
            if (categoriesBuilder.length() > 2) {
                categoriesBuilder.setLength(categoriesBuilder.length() - 2);
            }
            holder.categoriesTextView.setText(categoriesBuilder.toString());
        } else {
            holder.categoriesTextView.setText("No Categories Available");
        }
    }

    @Override
    public int getItemCount() {
        return physiotherapists.size();
    }

    public void updateData(List<Physiotherapist> newPhysiotherapists) {
        this.physiotherapists = newPhysiotherapists;
        notifyDataSetChanged();
    }

    static class PhysiotherapistViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView addressTextView;
        TextView specialtyTextView;
        TextView categoriesTextView; // New TextView for categories

        public PhysiotherapistViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewPhysioName);
            addressTextView = itemView.findViewById(R.id.textViewPhysioAddress);
            specialtyTextView = itemView.findViewById(R.id.textViewPhysioSpecialty);
            categoriesTextView = itemView.findViewById(R.id.textViewPhysioCategories);
        }
    }
}
