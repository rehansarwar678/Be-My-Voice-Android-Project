package com.example.easychat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.easychat.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<Integer> {

    public CustomSpinnerAdapter(Context context, List<Integer> icons) {
        super(context, R.layout.spinner_item_layout, icons);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_layout, parent, false);
        }
        ImageView iconImageView = convertView.findViewById(R.id.spinnerItemIcon);
        int iconResId = getItem(position);
        iconImageView.setImageResource(iconResId);

        return convertView;
    }

}