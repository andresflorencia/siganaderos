package com.florencia.visitaganadero.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.visitaganadero.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class Catalogo {
    public Integer idcatalogo;
    public String codigocatalogo, nombrecatalogo, codigopadre;

    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAGCATALOGO";

    public Catalogo(){
        this.idcatalogo = 0;
        this.codigocatalogo = "";
        this.codigopadre = "";
        this.nombrecatalogo = "";
    }

    public static Catalogo get(String codigo) {
        Catalogo catalogo=null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM catalogo where codigocatalogo = ?", new String[]{codigo});
            if (cursor.moveToFirst())
                catalogo = Catalogo.AsignaDatos(cursor);
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "get()" + ec.getMessage());
            ec.printStackTrace();
        }
        return catalogo;
    }

    public static List<Catalogo> getCatalogo(String codigopadre) {
        List<Catalogo> lista = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM catalogo where codigopadre = ? ORDER BY nombrecatalogo", new String[]{codigopadre});
            Catalogo catalogo = new Catalogo();
            if (cursor.moveToFirst()) {
                do {
                    catalogo = Catalogo.AsignaDatos(cursor);
                    if (catalogo != null) lista.add(catalogo);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "getCatalogo" + ec.getMessage());
            ec.printStackTrace();
        }
        return lista;
    }

    public static boolean SaveLista(List<Catalogo> catalogos) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Catalogo item:catalogos) {
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "catalogo(idcatalogo, codigocatalogo, nombrecatalogo, codigopadre)" +
                                "values(?, ?, ?, ?)",
                        new String[]{item.idcatalogo.toString(), item.codigocatalogo, item.nombrecatalogo, item.codigopadre});
            }
            sqLiteDatabase.close();
            Log.d(TAG,"Guardó lista catalogos");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "SaveLista(): " + ex.getMessage());
            return false;
        }
    }

    public static Catalogo AsignaDatos(Cursor cursor) {
        Catalogo Item = null;
        try {
            Item = new Catalogo();
            Item.idcatalogo = cursor.getInt(0);
            Item.codigocatalogo = cursor.getString(1);
            Item.nombrecatalogo = cursor.getString(2);
            Item.codigopadre = cursor.getString(3);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }

    public static boolean Delete(String codigopadre) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM catalogo WHERE codigopadre <> ?", new String[] {codigopadre});
            sqLiteDatabase.close();
            Log.d(TAG, "CATALOGO "+ codigopadre +" ELIMINADOS");
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, ec.getMessage());
        }
        return false;
    }

    @Override
    public String toString() {
        return nombrecatalogo;
    }
}
