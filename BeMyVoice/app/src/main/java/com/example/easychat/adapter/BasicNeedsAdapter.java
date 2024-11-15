package com.example.easychat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.R;
import com.example.easychat.database.ItemEntity;

import java.util.List;

public class BasicNeedsAdapter extends RecyclerView.Adapter<BasicNeedsAdapter.ViewHolder> {
    private final List<ItemEntity> itemList;

        private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public BasicNeedsAdapter(List<ItemEntity> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.basic_needs_items_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemEntity item = itemList.get(position);
        holder.itemNameTextView.setText(item.itemName);
        holder.itemIconImageView.setImageResource(item.iconResId);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        ImageView itemIconImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemName);
            itemIconImageView = itemView.findViewById(R.id.itemIcon);
        }
    }

}