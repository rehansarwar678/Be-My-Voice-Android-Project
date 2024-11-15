package com.example.easychat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.R;

import java.util.List;

public class SignsAdapter extends RecyclerView.Adapter<SignsAdapter.SignViewHolder> {

    private final Context context;
    private final List<Integer> imageList;

    public SignsAdapter(Context context, List<Integer> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public SignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sign_language, parent, false);
        return new SignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SignViewHolder holder, int position) {
        int imageResource = imageList.get(position);
        holder.receiveImage.setImageResource(imageResource);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class SignViewHolder extends RecyclerView.ViewHolder {
        ImageView receiveImage;

        public SignViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveImage = itemView.findViewById(R.id.receiveImage);
        }
    }
}
