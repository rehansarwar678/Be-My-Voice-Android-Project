package com.example.easychat.ui.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.AsyncTask;
import android.widget.Button;
import android.content.Intent;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.annotation.SuppressLint;

import com.example.easychat.R;
import com.example.easychat.database.ItemEntity;
import com.example.easychat.database.AppDatabase;
import com.example.easychat.adapter.BasicNeedsAdapter;
import com.example.easychat.adapter.CustomSpinnerAdapter;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class BasicNeedsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_TIME_KEY = "firstTimeFlag";
    private AppDatabase appDatabase;
    private List<ItemEntity> itemList;
    private BasicNeedsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_needs);

        //L O A D     D A T A
        AsyncTask.execute(() -> runOnUiThread(this::loadItemsFromDatabase));

        //B A C K     P R E S S
        ImageView backPress = findViewById(R.id.backPress);
        backPress.setOnClickListener(v -> finish());

        //A D A P T E R
        itemList = new ArrayList<>();
        adapter = new BasicNeedsAdapter(itemList);

        //R E C Y C L E R     V I E W
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        //C L I C K     L I S T E N E R
        adapter.setOnItemClickListener(position -> {
            ItemEntity clickedItem = itemList.get(position);
            openDetailActivity(clickedItem.itemName, clickedItem.id);
        });

        //A D D     I T E M S
        Button addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> showAddItemDialog());

        //D A T A B A S E
        appDatabase = AppDatabase.getDatabase(this);

    }

    private boolean isFirstTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(FIRST_TIME_KEY, true);
    }

    private void setFirstTimeFlag() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIRST_TIME_KEY, false);
        editor.apply();
    }

    private void openDetailActivity(String itemName, int id) {
        Intent intent = new Intent(this, BasicNeedsDetailActivity.class);
        intent.putExtra("itemName", itemName);
        intent.putExtra("itemId", id);
        startActivity(intent);
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Item Details");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);
        EditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);
        Spinner iconSpinner = dialogView.findViewById(R.id.spinnerIcon);
        List<Integer> iconArray = Arrays.asList(R.drawable.house, R.drawable.market, R.drawable.bff, R.drawable.kitchen, R.drawable.worker);
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, iconArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        iconSpinner.setAdapter(adapter);
        builder.setPositiveButton("ADD", (dialog, which) -> {
            String itemName = itemNameEditText.getText().toString();
            int selectedIconResId = iconArray.get(iconSpinner.getSelectedItemPosition());
            ItemEntity newItem = new ItemEntity();
            newItem.itemName = itemName;
            newItem.iconResId = selectedIconResId;
            AsyncTask.execute(() -> {
                appDatabase.itemDao().insert(newItem);
                runOnUiThread(this::loadItemsFromDatabase);
            });
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsFromDatabase() {
        if (isFirstTime()) {
            addFirstTimeOnly();
            setFirstTimeFlag();
        }
        new LoadItemsTask().execute();
    }

    private void addFirstTimeOnly() {
        ItemEntity home = new ItemEntity();
        home.itemName = "House";
        home.iconResId = R.drawable.house;
        AsyncTask.execute(() -> appDatabase.itemDao().insert(home));

        ItemEntity kitchen = new ItemEntity();
        kitchen.itemName = "Kitchen";
        kitchen.iconResId = R.drawable.kitchen;
        AsyncTask.execute(() -> appDatabase.itemDao().insert(kitchen));

        ItemEntity market = new ItemEntity();
        market.itemName = "Market";
        market.iconResId = R.drawable.market;
        AsyncTask.execute(() -> appDatabase.itemDao().insert(market));

        ItemEntity office = new ItemEntity();
        office.itemName = "Office";
        office.iconResId = R.drawable.worker;
        AsyncTask.execute(() -> appDatabase.itemDao().insert(office));
        ItemEntity friends = new ItemEntity();
        friends.itemName = "Friends";
        friends.iconResId = R.drawable.bff;
        AsyncTask.execute(() -> appDatabase.itemDao().insert(friends));
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadItemsTask extends AsyncTask<Void, Void, List<ItemEntity>> {
        @Override
        protected List<ItemEntity> doInBackground(Void... voids) {
            return appDatabase.itemDao().getAllItems();
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(List<ItemEntity> result) {
            Log.d("LoadItemsTask", "Loaded items from the database: " + result.size());
            itemList.addAll(result);
            adapter.notifyDataSetChanged();
        }

    }

}