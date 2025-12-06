package com.example.animals.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Animals3 extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BorradoresApp.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_PUB = "borradores_publicacion";
    private static final String COL_PUB_USER = "id_usuario";
    private static final String COL_PUB_DESC = "descripcion";
    private static final String COL_PUB_FOTO = "foto_uri";

    private static final String TABLE_PET = "borradores_mascota";
    private static final String COL_PET_USER = "id_usuario";
    private static final String COL_PET_NOMBRE = "nombre";
    private static final String COL_PET_ESPECIE = "especie";
    private static final String COL_PET_RAZA = "raza";
    private static final String COL_PET_SEXO = "sexo";
    private static final String COL_PET_EDAD = "edad";
    private static final String COL_PET_COLOR = "color";
    private static final String COL_PET_FOTO = "foto_uri";

    private static final String TABLE_REC = "borradores_recomendacion";
    private static final String COL_REC_USER = "id_usuario";
    private static final String COL_REC_TITULO = "titulo";
    private static final String COL_REC_DESC = "descripcion";
    private static final String COL_REC_FOTO = "foto_uri";

    private static final String CREATE_TABLE_PUB =
            "CREATE TABLE " + TABLE_PUB + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PUB_USER + " INTEGER, " +
                    COL_PUB_DESC + " TEXT, " +
                    COL_PUB_FOTO + " TEXT)";

    private static final String CREATE_TABLE_PET =
            "CREATE TABLE " + TABLE_PET + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PET_USER + " INTEGER, " +
                    COL_PET_NOMBRE + " TEXT, " +
                    COL_PET_ESPECIE + " TEXT, " +
                    COL_PET_RAZA + " TEXT, " +
                    COL_PET_SEXO + " TEXT, " +
                    COL_PET_EDAD + " TEXT, " +
                    COL_PET_COLOR + " TEXT, " +
                    COL_PET_FOTO + " TEXT)";

    private static final String CREATE_TABLE_REC =
            "CREATE TABLE " + TABLE_REC + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_REC_USER + " INTEGER, " +
                    COL_REC_TITULO + " TEXT, " +
                    COL_REC_DESC + " TEXT, " +
                    COL_REC_FOTO + " TEXT)";

    public Animals3(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PUB);
        db.execSQL(CREATE_TABLE_PET);
        db.execSQL(CREATE_TABLE_REC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUB);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REC); // (NUEVO)
        onCreate(db);
    }


    public void guardarBorradorPublicacion(int idUsuario, String descripcion, String fotoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_PUB, COL_PUB_USER + "=?", new String[]{String.valueOf(idUsuario)});
            ContentValues values = new ContentValues();
            values.put(COL_PUB_USER, idUsuario);
            values.put(COL_PUB_DESC, descripcion);
            values.put(COL_PUB_FOTO, fotoUri);
            db.insert(TABLE_PUB, null, values);
        } catch (Exception e) { e.printStackTrace(); }
        finally { db.close(); }
    }

    public String[] obtenerBorradorPublicacion(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] datos = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_PUB + " WHERE " + COL_PUB_USER + " = ?", new String[]{String.valueOf(idUsuario)});
            if (cursor.moveToLast()) {
                datos = new String[2];
                datos[0] = cursor.getString(2); // descripcion
                datos[1] = cursor.getString(3); // foto
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return datos;
    }

    public void limpiarBorradorPublicacion(int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PUB, COL_PUB_USER + "=?", new String[]{String.valueOf(idUsuario)});
        db.close();
    }

    public void guardarBorradorMascota(int idUsuario, String nombre, String especie, String raza, String sexo, String edad, String color, String fotoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_PET, COL_PET_USER + "=?", new String[]{String.valueOf(idUsuario)});
            ContentValues values = new ContentValues();
            values.put(COL_PET_USER, idUsuario);
            values.put(COL_PET_NOMBRE, nombre);
            values.put(COL_PET_ESPECIE, especie);
            values.put(COL_PET_RAZA, raza);
            values.put(COL_PET_SEXO, sexo);
            values.put(COL_PET_EDAD, edad);
            values.put(COL_PET_COLOR, color);
            values.put(COL_PET_FOTO, fotoUri);
            db.insert(TABLE_PET, null, values);
        } catch (Exception e) { e.printStackTrace(); }
        finally { db.close(); }
    }

    public String[] obtenerBorradorMascota(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] datos = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_PET + " WHERE " + COL_PET_USER + " = ?", new String[]{String.valueOf(idUsuario)});
            if (cursor.moveToLast()) {
                datos = new String[7];
                datos[0] = cursor.getString(2); // nombre
                datos[1] = cursor.getString(3); // especie
                datos[2] = cursor.getString(4); // raza
                datos[3] = cursor.getString(5); // sexo
                datos[4] = cursor.getString(6); // edad
                datos[5] = cursor.getString(7); // color
                datos[6] = cursor.getString(8); // foto
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return datos;
    }

    public void limpiarBorradorMascota(int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PET, COL_PET_USER + "=?", new String[]{String.valueOf(idUsuario)});
        db.close();
    }


    public void guardarBorradorRecomendacion(int idUsuario, String titulo, String descripcion, String fotoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_REC, COL_REC_USER + "=?", new String[]{String.valueOf(idUsuario)});

            ContentValues values = new ContentValues();
            values.put(COL_REC_USER, idUsuario);
            values.put(COL_REC_TITULO, titulo);
            values.put(COL_REC_DESC, descripcion);
            values.put(COL_REC_FOTO, fotoUri);

            db.insert(TABLE_REC, null, values);
        } catch (Exception e) { e.printStackTrace(); }
        finally { db.close(); }
    }

    public String[] obtenerBorradorRecomendacion(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] datos = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_REC + " WHERE " + COL_REC_USER + " = ?", new String[]{String.valueOf(idUsuario)});
            if (cursor.moveToLast()) {
                datos = new String[3];
                // Índices: 0=id, 1=user, 2=titulo, 3=descripcion, 4=foto
                datos[0] = cursor.getString(2); // titulo
                datos[1] = cursor.getString(3); // descripcion
                datos[2] = cursor.getString(4); // foto
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return datos;
    }

    public void limpiarBorradorRecomendacion(int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REC, COL_REC_USER + "=?", new String[]{String.valueOf(idUsuario)});
        db.close();
    }
}