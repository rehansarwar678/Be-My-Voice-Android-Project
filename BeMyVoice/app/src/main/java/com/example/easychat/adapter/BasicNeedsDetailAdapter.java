package com.example.easychat.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.easychat.R;
import com.example.easychat.database.ItemDetailEntity;

import java.util.List;

public class BasicNeedsDetailAdapter extends RecyclerView.Adapter<BasicNeedsDetailAdapter.ViewHolder> {
    private final List<ItemDetailEntity> itemDetailList;
    private BasicNeedsAdapter.OnItemClickListener onItemClickListener;
    public BasicNeedsDetailAdapter(List<ItemDetailEntity> itemDetailList) {
        this.itemDetailList = itemDetailList;
    }
    public void setOnItemClickListener(BasicNeedsAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public BasicNeedsDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.basic_needs_item_detail_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BasicNeedsDetailAdapter.ViewHolder holder, int position) {
        ItemDetailEntity itemDetail = itemDetailList.get(position);
        holder.itemNameTextView.setText(itemDetail.itemName);
        holder.itemDescriptionTextView.setText(itemDetail.itemDescription);
        if (itemDetail.imageUri != null && !itemDetail.imageUri.isEmpty()) {
            Uri imageUri = Uri.parse(itemDetail.imageUri);
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .into(holder.itemIconImageView);
        } else {
            holder.itemIconImageView.setImageResource(R.drawable.deaf);
        }
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemDetailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIconImageView;
        TextView itemNameTextView, itemDescriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIconImageView = itemView.findViewById(R.id.itemImg);
            itemNameTextView = itemView.findViewById(R.id.itemDetailName);
            itemDescriptionTextView = itemView.findViewById(R.id.itemDetailDescription);
        }
    }

}