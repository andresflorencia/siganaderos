package com.florencia.visitaganadero.models;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.visitaganadero.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class FodaPropiedad {
    public Integer idfoda, ganaderoid, propiedadid, tipo;
    public String descripcion, causas, solucion_1, solucion_2, observacion;

    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAG_USOSUELO";

    public FodaPropiedad(){
        this.idfoda = 0;
        this.ganaderoid = 0;
        this.propiedadid = 0;
        this.tipo = 0;
        this.descripcion = "";
        this.causas = "";
        this.solucion_1 = "";
        this.solucion_2 = "";
        this.observacion = "";
    }

    public static boolean removeFODAs(Integer idGanadero, Integer idPropiedad) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM fodapropiedad WHERE ganaderoid = ? and propiedadid = ?", new String[] {idGanadero.toString(), idPropiedad.toString()});
            sqLiteDatabase.close();
            Log.d(TAG, "FODAs ELIMINADOS propiedadid:" + idPropiedad);
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, "removeFODAs(): " + ec.getMessage());
        }
        return false;
    }

    public static boolean SaveList(Integer idganadero, Integer idpropiedad, List<FodaPropiedad> FODAs) {
        try {
            removeFODAs(idganadero, idpropiedad);
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (FodaPropiedad foda:FODAs) {
                foda.ganaderoid = idganadero;
                foda.propiedadid = idpropiedad;
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "fodapropiedad(idfoda, ganaderoid, propiedadid, tipo," +
                                "descripcion, causas, solucion_1, solucion_2, observacion) "+
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{foda.idfoda==0?null: foda.idfoda.toString(), foda.ganaderoid.toString(), foda.propiedadid.toString(),
                                foda.tipo.toString(), foda.descripcion, foda.causas, foda.solucion_1, foda.solucion_2, foda.observacion});
                if (foda.idfoda== 0) foda.idfoda = SQLite.sqlDB.getLastId();
            }
            sqLiteDatabase.close();
            Log.d(TAG,"SAVE LISTA DE FODAs OK");
            return true;
        } catch (SQLException ex){
            Log.d(TAG,ex.getMessage());
            return false;
        }
    }

    public static List<FodaPropiedad> getLista(Integer idpropiedad){
        List<FodaPropiedad> retorno = new ArrayList<>();
        try{
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM fodapropiedad where propiedadid = ? ORDER BY nombrecatalogo", new String[]{idpropiedad.toString()});
            FodaPropiedad foda;
            if (cursor.moveToFirst()) {
                do {
                    foda = FodaPropiedad.AsignaDatos(cursor);
                    if (foda != null) retorno.add(foda);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        }catch (SQLiteException e){
            Log.d(TAG, "getLista(): " + e.getMessage());
        }
        return retorno;
    }

    public static List<FodaPropiedad> getLista(Integer idpropiedad, Integer tipo){
        List<FodaPropiedad> retorno = new ArrayList<>();
        try{
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM fodapropiedad where propiedadid = ? and tipo = ?", new String[]{idpropiedad.toString(),tipo.toString()});
            FodaPropiedad foda;
            if (cursor.moveToFirst()) {
                do {
                    foda = FodaPropiedad.AsignaDatos(cursor);
                    if (foda != null) retorno.add(foda);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        }catch (SQLiteException e){
            Log.d(TAG, "getLista(): " + e.getMessage());
        }
        return retorno;
    }

    public static FodaPropiedad AsignaDatos(Cursor cursor) {
        FodaPropiedad Item = null;
        try {
            Item = new FodaPropiedad();
            Item.idfoda = cursor.getInt(0);
            Item.ganaderoid = cursor.getInt(1);
            Item.propiedadid = cursor.getInt(2);
            Item.tipo = cursor.getInt(3);
            Item.descripcion = cursor.getString(4);
            Item.causas = cursor.getString(5);
            Item.solucion_1 = cursor.getString(6);
            Item.solucion_2 = cursor.getString(7);
            Item.observacion = cursor.getString(8);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }
}
