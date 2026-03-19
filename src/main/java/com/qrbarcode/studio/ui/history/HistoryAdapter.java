package com.qrbarcode.studio.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.qrbarcode.studio.R;
import com.qrbarcode.studio.data.ScanRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ScanRecord> records = new ArrayList<>();
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean isMultiSelectMode = false;
    private OnItemClickListener clickListener;
    private OnSelectionChangedListener selectionChangedListener;

    public interface OnItemClickListener {
        void onClick(ScanRecord record);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void setRecords(List<ScanRecord> records) {
        this.records = records;
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public void setMultiSelectMode(boolean enabled) {
        isMultiSelectMode = enabled;
        if (!enabled) selectedIds.clear();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvContent, tvFormat, tvTimestamp;
        Chip chipType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_history_item);
            tvContent = itemView.findViewById(R.id.tv_history_content);
            tvFormat = itemView.findViewById(R.id.tv_history_format);
            tvTimestamp = itemView.findViewById(R.id.tv_history_timestamp);
            chipType = itemView.findViewById(R.id.chip_type);
        }

        void bind(ScanRecord record) {
            tvContent.setText(record.content);
            tvFormat.setText(record.format);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            tvTimestamp.setText(sdf.format(new Date(record.timestamp)));

            chipType.setText(record.type);
            chipType.setChipBackgroundColorResource(
                    "SCANNED".equals(record.type) ? R.color.chip_scanned : R.color.chip_generated);

            boolean isSelected = selectedIds.contains(record.id);
            cardView.setCardBackgroundColor(isSelected
                    ? itemView.getContext().getColor(R.color.selected_bg)
                    : itemView.getContext().getColor(R.color.card_bg));

            itemView.setOnClickListener(v -> {
                if (isMultiSelectMode) {
                    toggleSelection(record.id);
                } else if (clickListener != null) {
                    clickListener.onClick(record);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (!isMultiSelectMode) {
                    isMultiSelectMode = true;
                }
                toggleSelection(record.id);
                return true;
            });
        }

        private void toggleSelection(int id) {
            if (selectedIds.contains(id)) {
                selectedIds.remove(id);
            } else {
                selectedIds.add(id);
            }
            notifyItemChanged(getAdapterPosition());
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(selectedIds.size());
            }
        }
    }
}
