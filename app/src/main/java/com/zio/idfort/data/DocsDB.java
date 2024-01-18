package com.zio.idfort.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DocsEntity.class}, version = 3)
public abstract class DocsDB extends RoomDatabase {
    public abstract DocsDAO docsdao();
}
