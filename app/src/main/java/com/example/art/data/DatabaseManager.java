package com.example.art.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.art.model.PaintingDescriptor;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.openDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public List<PaintingDescriptor> getAllPaintings() {
        List<PaintingDescriptor> paintings = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT id, name, descriptor FROM paintings", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                byte[] descriptor = cursor.getBlob(cursor.getColumnIndexOrThrow("descriptor"));
                paintings.add(new PaintingDescriptor(id, name, descriptor));
            } while (cursor.moveToNext());

            cursor.close();
        }
        Log.d("DB", "✅ Loaded paintings: " + paintings.size());
        return paintings;
    }
}
