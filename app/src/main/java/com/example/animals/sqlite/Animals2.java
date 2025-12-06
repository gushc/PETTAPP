package com.example.animals.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Animals2 extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HistorialBusqueda.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "historial";
    private static final String COL_ID = "id";
    private static final String COL_QUERY = "busqueda";
    private static final String COL_USER = "id_usuario"; // <--- NUEVO CAMPO

    public Animals2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER + " INTEGER, " +
                COL_QUERY + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertQuery(String query, int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_NAME, COL_QUERY + " = ? AND " + COL_USER + " = ?",
                    new String[]{query, String.valueOf(idUsuario)});

            // 2. Insertar la nueva
            ContentValues values = new ContentValues();
            values.put(COL_USER, idUsuario);
            values.put(COL_QUERY, query);
            db.insert(TABLE_NAME, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public List<String> getRecentQueries(int idUsuario) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COL_QUERY + " FROM " + TABLE_NAME +
                            " WHERE " + COL_USER + " = ? " +
                            " ORDER BY " + COL_ID + " DESC LIMIT 10",
                    new String[]{String.valueOf(idUsuario)});

            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }
}