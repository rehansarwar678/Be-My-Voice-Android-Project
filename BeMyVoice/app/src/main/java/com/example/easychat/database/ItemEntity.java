package com.example.easychat.database;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class ItemEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "item_name")
    public String itemName;

    @ColumnInfo(name = "icon_res_id")
    public int iconResId;

    // Default constructor
    public ItemEntity() {
    }

    // Constructor with parameters for item name and icon resource ID
    public ItemEntity(String itemName, int iconResId) {
        this.itemName = itemName;
        this.iconResId = iconResId;
    }
}
