package com.example.easychat.database;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Insert;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    void insert(ItemEntity item);

    @Query("SELECT * FROM items")
    List<ItemEntity> getAllItems();

    @Insert
    void insertDetail(ItemDetailEntity itemDetail);

    @Query("SELECT * FROM items_detail WHERE parent_id = :pId")
    List<ItemDetailEntity> getSubItem(int pId);

}