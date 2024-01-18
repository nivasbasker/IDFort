package com.zio.idfort.data;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DOCS")
public class DocsEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "document")
    private String document_name;
    @ColumnInfo(name = "uri")
    private String uri;

    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "name")
    private String name;

    public DocsEntity(@NonNull String document_name, String uri, String id, String name) {
        this.document_name = document_name;
        this.uri = uri;
        this.id = id;
        this.name = name;
    }

    public DocsEntity() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    public String getDocument_name() {
        return document_name;
    }

    public void setDocument_name(@NonNull String document_name) {
        this.document_name = document_name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
