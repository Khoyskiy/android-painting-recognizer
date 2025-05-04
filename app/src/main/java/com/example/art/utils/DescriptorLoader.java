package com.example.art.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.art.data.DatabaseHelper;
import com.example.art.model.PaintingDescriptor;

import java.util.ArrayList;
import java.util.List;

public class DescriptorLoader {
    private final Context context;

    public DescriptorLoader(Context context) {
        this.context = context;
    }

    public List<PaintingDescriptor> loadAllDescriptors() {
        List<PaintingDescriptor> list = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM paintings", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                byte[] blob = cursor.getBlob(2);
                list.add(new PaintingDescriptor(id, name, blob));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
