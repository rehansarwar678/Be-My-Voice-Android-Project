package com.example.easychat.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easychat.R;
import com.example.easychat.adapter.ImageAdapter;
import com.example.easychat.interfaces.OnItemClickListener;

import java.util.ArrayList;

public class ImportImagesActivity extends AppCompatActivity implements OnItemClickListener {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_images);

        ImageView backPress = findViewById(R.id.backPress);
        backPress.setOnClickListener((v) -> finish());


        int targetSdkVersion = getApplicationInfo().targetSdkVersion;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && targetSdkVersion >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                displayImages();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                displayImages();
            }
        }

    }

    private void displayImages() {
        RecyclerView recyclerView = findViewById(R.id.imageRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ImageAdapter imageAdapter = new ImageAdapter(ImportImagesActivity.this, getAllImages(), this);
        recyclerView.setAdapter(imageAdapter);
    }

    private ArrayList<String> getAllImages() {
        ArrayList<String> imageList = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null)) {
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(columnIndex);
                    imageList.add(imagePath);
                }
            }
        }
        return imageList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayImages();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(String imagePath) {
        Intent intent = new Intent(this, ConfirmImageActivity.class);
        intent.putExtra("imagePath", imagePath);
        startActivity(intent);
        finish();
    }

}