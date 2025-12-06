package com.example.animals.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class Animals extends SQLiteOpenHelper {
    private static final String nombreDB = "TransitoSQL.db";
    private static final int versionDB = 1;
    private static final String createTableUsuario = "create table if not exists Usuarios(id integer, correo varchar(100), clave varchar(100));";
    private static final String dropTableUsuario = "drop table if exists Usuarios;";
    public Animals(@Nullable Context context) {
        super(context, nombreDB, null, versionDB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableUsuario);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(dropTableUsuario);
        db.execSQL(createTableUsuario);
    }
    public boolean agregarUsuario(int id, String correo, String clave){
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            ContentValues param = new ContentValues();
            param.put("id",id);
            param.put("correo",correo);
            param.put("clave",clave);
            long resultado = db.insert("Usuarios",null,param);
            db.close();
            return  resultado != -1;
        }
        return false;
    }
    public boolean recordar(){
        SQLiteDatabase db = getReadableDatabase();
        if(db != null){
            String consulta = "select * from Usuarios;";
            Cursor cursor = db.rawQuery(consulta, null);
            if(cursor.moveToNext())
                return true;
        }
        return false;
    }
    public String getValur(String key) {
        SQLiteDatabase db = getReadableDatabase();
        final List<String> KEYS = Arrays.asList("id", "correo", "clave");
        if (!KEYS.contains(key)){
            System.err.println("llave no pemritida: " + key);
            return null;
        }
        SQLiteDatabase DB = getReadableDatabase();
        if(db != null){
            Cursor cursor = db.query("Usuarios",new String[]{key},null,null,null,null,null);
            if(cursor.moveToNext())
                return cursor.getString(0);
        }
        return null;
    }
    public boolean updateValue(String key, String value) {
        final List<String> KEYS = Arrays.asList("id", "correo", "clave");
        if (!KEYS.contains(key)) {
            System.err.println("llave no pemritida: " + key);
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            ContentValues param = new ContentValues();
            param.put(key,value);
            int resultado = db.update("Usuarios",param,null,null);
            db.close();
            return resultado >0;
        }
        return false;
    }
    public boolean eliminarUsuario(){
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            String consulta = "delete from Usuarios";
            db.execSQL(consulta);
            db.close();
            return true;
        }
        return false;
    }
}
