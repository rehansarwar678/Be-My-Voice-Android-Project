package com.example.easychat.ui.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.easychat.R;
import com.example.easychat.adapter.BasicNeedsDetailAdapter;
import com.example.easychat.database.AppDatabase;
import com.example.easychat.database.ItemDetailEntity;
import com.example.easychat.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @noinspection ALL
 */
public class BasicNeedsDetailActivity extends AppCompatActivity {
    int itemId;
    Uri selectedImageUri;
    Button chooseImageButton;
    Bitmap capturedImageBitmap;
    ImageView selectedImageView;
    private AppDatabase appDatabase;
    private BasicNeedsDetailAdapter adapter;
    private List<ItemDetailEntity> itemDetailList;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGE_FROM_GALLERY_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_needs_detail);

        //L O A D     D A T A
        AsyncTask.execute(() -> runOnUiThread(this::loadItemsFromDatabase));

        //B A C K    P R E S S
        ImageView backPress = findViewById(R.id.backPressDetail);
        backPress.setOnClickListener(v -> finish());

        //A C T I O N    B A R    T E X T
        TextView detailItemNameTextView = findViewById(R.id.detailItemName);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("itemName")) {
            String itemName = intent.getStringExtra("itemName");
            itemId = intent.getIntExtra("itemId", 0);
            detailItemNameTextView.setText(itemName);
        }

        //A D A P T E R
        itemDetailList = new ArrayList<>();
        adapter = new BasicNeedsDetailAdapter(itemDetailList);

        //R E C Y C L E R     V I E W
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDetail);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        //A D D     I T E M S
        Button addBtn = findViewById(R.id.addDetailBtn);
        addBtn.setOnClickListener(v -> showAddItemDialog());

        //D A T A B A S E
        appDatabase = AppDatabase.getDatabase(this);

    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Item Details");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add__detail_item, null);
        builder.setView(dialogView);
        EditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);
        EditText itemNameDescription = dialogView.findViewById(R.id.editTextItemDescription);
        selectedImageView = dialogView.findViewById(R.id.selectImageView);
        chooseImageButton = dialogView.findViewById(R.id.chooseImageButton);
        chooseImageButton.setOnClickListener(v -> {
            chooseDialog();
        });
        builder.setPositiveButton("ADD", (dialog, which) -> {
            String itemName = itemNameEditText.getText().toString();
            String itemDesc = itemNameDescription.getText().toString();
            ItemDetailEntity newItem = new ItemDetailEntity();
            newItem.pId = itemId;
            newItem.itemName = itemName;
            newItem.itemDescription = itemDesc;
            newItem.imageUri = selectedImageUri.toString();
            AsyncTask.execute(() -> {
                appDatabase.itemDao().insertDetail(newItem);
                runOnUiThread(this::loadItemsFromDatabase);
            });
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private AlertDialog alertDialog;

    private void chooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.choose_camera_gallery_layouty, null);
        builder.setView(dialogView);

        LinearLayout camera = dialogView.findViewById(R.id.selectFromCamera);
        LinearLayout gallery = dialogView.findViewById(R.id.selectFromGallery);

        camera.setOnClickListener(v -> {
            dispatchTakePictureIntent();
            alertDialog.dismiss();
        });

        gallery.setOnClickListener(v -> {
            openGallery();
            alertDialog.dismiss();
        });

        alertDialog = builder.create();
        alertDialog.show();
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY_REQUEST);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       /* if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedImageBitmap = (Bitmap) extras.get("data");
            selectedImageView.setVisibility(View.VISIBLE);
            selectedImageView.setImageBitmap(capturedImageBitmap);
            chooseImageButton.setVisibility(View.GONE);
            selectedImageUri = ImageUtils.saveImageAndGetUri(this, capturedImageBitmap);
        }*/
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            handleCameraImageResult(data);
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY_REQUEST && resultCode == RESULT_OK) {
            handleCameraImageResult(data);
        }
    }

    private void handleCameraImageResult(Intent data) {
        Bundle extras = data.getExtras();
        capturedImageBitmap = (Bitmap) extras.get("data");
        selectedImageUri = data.getData();
        processImage();
    }
    private void processImage() {
        selectedImageView.setVisibility(View.VISIBLE);
        selectedImageView.setImageBitmap(capturedImageBitmap);
        chooseImageButton.setVisibility(View.GONE);
        if (capturedImageBitmap != null) {
            selectedImageUri = ImageUtils.saveImageAndGetUri(this, capturedImageBitmap);
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsFromDatabase() {
        new BasicNeedsDetailActivity.LoadItemsTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadItemsTask extends AsyncTask<Void, Void, List<ItemDetailEntity>> {
        @Override
        protected List<ItemDetailEntity> doInBackground(Void... voids) {
            return appDatabase.itemDao().getSubItem(itemId);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(List<ItemDetailEntity> result) {
            itemDetailList.clear();
            itemDetailList.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }

}