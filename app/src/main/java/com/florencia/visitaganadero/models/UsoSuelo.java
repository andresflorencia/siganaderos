package com.florencia.visitaganadero.models;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.visitaganadero.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class UsoSuelo {
    public Integer idusosuelo, propiedadid, orden;
    public Catalogo tipo_cultivo;
    public Double area_cultivo;
    public String variedad_sembrada, observacion;

    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAG_USOSUELO";

    public UsoSuelo(){
        this.idusosuelo = 0;
        this.propiedadid = 0;
        this.tipo_cultivo = new Catalogo();
        this.area_cultivo =0d;
        this.variedad_sembrada = "";
        this.observacion = "";
        this.orden =0;
    }

    public static boolean removeUsoSuelo(Integer idPropiedad) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM usosuelo WHERE propiedadid = ?", new String[] {idPropiedad.toString()});
            sqLiteDatabase.close();
            Log.d(TAG, "usos de suelo ELIMINADOS propiedadid:" + idPropiedad);
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, "removeUsoSuelo(): " + ec.getMessage());
        }
        return false;
    }

    public static boolean SaveList(Integer idpropiedad, List<UsoSuelo> usos) {
        try {
            removeUsoSuelo(idpropiedad);
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            int i = 0;
            for (UsoSuelo uso:usos) {
                uso.propiedadid = idpropiedad;
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                        "usosuelo(idusosuelo, propiedadid, tipo_cultivo, area_cultivo, variedad_sembrada, observacion, orden) "+
                        "values(?, ?, ?, ?, ?, ?, ?)",
                new String[]{uso.idusosuelo ==0?null: uso.idusosuelo.toString(), uso.propiedadid.toString(), uso.tipo_cultivo.codigocatalogo,
                        uso.area_cultivo.toString(), uso.variedad_sembrada, uso.observacion, String.valueOf(i+1)});
                if (uso.idusosuelo== 0) uso.idusosuelo = SQLite.sqlDB.getLastId();
                i++;
            }
            sqLiteDatabase.close();
            Log.d(TAG,"SAVE LISTA DE USOS DE SUELO OK");
            return true;
        } catch (SQLException ex){
            Log.d(TAG,ex.getMessage());
            return false;
        }
    }

    public static List<UsoSuelo> getLista(Integer idpropiedad){
        List<UsoSuelo> retorno = new ArrayList<>();
        try{
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usosuelo where propiedadid = ? order by orden", new String[]{idpropiedad.toString()});
            UsoSuelo uso;
            if (cursor.moveToFirst()) {
                do {
                    uso = UsoSuelo.AsignaDatos(cursor);
                    if (uso != null) retorno.add(uso);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        }catch (SQLiteException e){
            Log.d(TAG, "getLista(): " + e.getMessage());
        }
        return retorno;
    }

    public static UsoSuelo AsignaDatos(Cursor cursor) {
        UsoSuelo Item = null;
        try {
            Item = new UsoSuelo();
            Item.idusosuelo = cursor.getInt(0);
            Item.propiedadid = cursor.getInt(1);
            Item.tipo_cultivo = Catalogo.get(cursor.getString(2));
            Item.area_cultivo = cursor.getDouble(3);
            Item.variedad_sembrada = cursor.getString(4);
            Item.observacion = cursor.getString(5);
            Item.orden = cursor.getInt(6);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }
}
