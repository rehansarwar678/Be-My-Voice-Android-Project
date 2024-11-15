package com.example.easychat.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items_detail")
public class ItemDetailEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "item_detail_name")
    public String itemName;

    @ColumnInfo(name = "item_detail_description")
    public String itemDescription;

    @ColumnInfo(name = "icon_detail_res_id")
    public int iconResId;

    @ColumnInfo(name = "parent_id")
    public int pId;

    @ColumnInfo(name = "image_uri")
    public String imageUri;
}