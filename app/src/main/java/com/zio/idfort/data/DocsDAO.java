package com.zio.idfort.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DocsDAO {

    final String DBName = "DOCS";

    @Query("SELECT * FROM " + DBName)
    List<DocsEntity> getAll();

    @Query("SELECT * FROM " + DBName + " WHERE Name LIKE :Name " +
            "LIMIT 1")
    DocsEntity findByName(String Name);

    @Insert
    void insertDoc(DocsEntity... users);

    @Delete
    void delete(DocsEntity user);

    @Query("DELETE FROM "+DBName)
    void deleteAll();
}
