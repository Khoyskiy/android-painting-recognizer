package com.example.art.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "paintings_vers_1.db";
    private final String DB_PATH;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
        this.DB_PATH = context.getDatabasePath(DB_NAME).getPath();
        createDatabase();
    }

    private void createDatabase() {
        File dbFile = new File(DB_PATH);

        SharedPreferences prefs = context.getSharedPreferences("db_prefs", Context.MODE_PRIVATE);
        String savedDbName = prefs.getString("db_name", "");

        boolean shouldCopy = !dbFile.exists() || !savedDbName.equals(DB_NAME);

        if (shouldCopy) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDatabase();
                prefs.edit().putString("db_name", DB_NAME).apply(); // зберігаємо нову назву
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void copyDatabase() throws IOException {
        InputStream input = context.getAssets().open(DB_NAME);
        OutputStream output = new FileOutputStream(DB_PATH);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
    }

    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
