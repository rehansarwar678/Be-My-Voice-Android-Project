package com.example.easychat.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.easychat.R;
import com.example.easychat.adapter.SignsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SignsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signs);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewSigns);

        List<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.thanks);
        imageList.add(R.drawable.family);
        imageList.add(R.drawable.welcome);
        imageList.add(R.drawable.no);
        imageList.add(R.drawable.yes);
        imageList.add(R.drawable.water);
        imageList.add(R.drawable.how_are_you);
        imageList.add(R.drawable.please);
        imageList.add(R.drawable.sorry);
        imageList.add(R.drawable.done);
        imageList.add(R.drawable.bye);

        SignsAdapter signsAdapter = new SignsAdapter(this, imageList);
        recyclerView.setAdapter(signsAdapter);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        findViewById(R.id.backPressBtn).setOnClickListener(v -> finish());
    }
}
