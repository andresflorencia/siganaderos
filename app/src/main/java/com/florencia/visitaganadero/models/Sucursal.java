package com.florencia.visitaganadero.models;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.florencia.visitaganadero.services.SQLite;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Sucursal {
    public String IdSucursal;
    public String RUC;
    public String RazonSocial;
    public String NombreComercial;
    public String NombreSucursal;
    public String Direcion;
    public String CodigoEstablecimiento;
    public String PuntoEmision;
    public String Ambiente;
    public String SucursalPadreID;
    public Integer IdEstablecimiento, IdPuntoEmision;
    public static SQLiteDatabase sqLiteDatabase;

    public Sucursal() {
        this.IdSucursal = "";
        this.RUC = "";
        this.RazonSocial = "";
        this.NombreComercial = "";
        this.NombreSucursal = "";
        this.Direcion = "";
        this.CodigoEstablecimiento = "";
        this.PuntoEmision = "";
        this.Ambiente = "";
        this.SucursalPadreID = "";
        this.IdEstablecimiento=0;
        this.IdPuntoEmision =0;
    }

    public boolean Guardar(){
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                    "sucursal(idsucursal, ruc, razonsocial, nombrecomercial, nombresucursal, direccion, codigoestablecimiento, puntoemision, ambiente, idestablecimiento, idpuntoemision) " +
                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)", new String[]{this.IdSucursal, this.RUC, this.RazonSocial, this.NombreComercial, this.NombreSucursal, this.Direcion, this.CodigoEstablecimiento, this.PuntoEmision, this.Ambiente, this.IdEstablecimiento.toString() , this.IdPuntoEmision.toString()});
            sqLiteDatabase.close();
            return true;
        } catch (SQLException ex) {
            Log.d("TAGSUCURSAL", "Guardar(): " + ex.getMessage());
            return false;
        }
    }

    public static boolean Delete (Integer id){
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.delete("sucursal","idestablecimiento = ?",new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d("TAGSUCURSAL","DELETE SUCURSAL OK");
            return true;
        } catch (SQLException ex){
            Log.d("TAGSUCURSAL", "Delete(): " + ex.getMessage());
            return false;
        }
    }

    static public Sucursal getSucursal(String cod){
        Sucursal Item = null;
        sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM sucursal WHERE idestablecimiento = ?", new String[] { cod });
        if (cursor.moveToFirst()) {
            Item = AsignaDatos(cursor);
        }
        cursor.close();
        sqLiteDatabase.close();
        return Item;
    }

    private static Sucursal AsignaDatos(Cursor cursor)
    {
        Sucursal Item = new Sucursal();
        Item.IdSucursal = cursor.getString(0);
        Item.RUC = cursor.getString(1);
        Item.RazonSocial = cursor.getString(2);
        Item.NombreComercial = cursor.getString(3);
        Item.NombreSucursal = cursor.getString(4);
        Item.Direcion = cursor.getString(5);
        Item.CodigoEstablecimiento = cursor.getString(6);
        Item.PuntoEmision = cursor.getString(7);
        Item.Ambiente = cursor.getString(8);
        Item.IdEstablecimiento = cursor.getInt(10);
        Item.IdPuntoEmision = cursor.getInt(11);
        return Item;
    }

    public static Sucursal AsignaDatos(JsonObject object) throws JsonParseException {
        Sucursal Item = null;
        try {
            if (object != null) {
                Item = new Sucursal();
                Item.IdSucursal = object.get("idsucursal").isJsonNull()?"": object.get("idsucursal").getAsString();
                Item.RUC = object.get("ruc").isJsonNull()?"": object.get("ruc").getAsString();
                Item.RazonSocial = object.get("razonsocial").isJsonNull()?"": object.get("razonsocial").getAsString();
                Item.NombreComercial = object.get("nombrecomercial").isJsonNull()?"":object.get("nombrecomercial").getAsString();
                Item.Direcion = object.get("direccion").isJsonNull()?"":object.get("direccion").getAsString();
                Item.CodigoEstablecimiento = object.get("codigoestablecimiento").isJsonNull()?"":object.get("codigoestablecimiento").getAsString();
                Item.PuntoEmision = object.get("puntoemision").isJsonNull()?"":object.get("puntoemision").getAsString();
                Item.Ambiente = object.get("ambiente").isJsonNull()?"":object.get("ambiente").getAsString();
                Item.IdEstablecimiento = object.get("idestablecimiento").isJsonNull()?0:object.get("idestablecimiento").getAsInt();
                Item.IdPuntoEmision = object.get("idpuntoemision").isJsonNull()?0:object.get("idpuntoemision").getAsInt();
                Item.NombreSucursal = object.get("nombreestablecimiento").isJsonNull()?"":object.get("nombreestablecimiento").getAsString();
                Sucursal.Delete(Item.IdEstablecimiento);
                if(Item.Guardar())
                    Item.actualizasecuencial(object.get("s01").isJsonNull()?0:object.get("s01").getAsInt(), "01");
            }
        }catch (JsonParseException e){
            Log.d("TAGSUCURSAL", "AsignaDatos(): " + e.getMessage());
        }
        return Item;
    }

    private boolean actualizasecuencial(int Secuencial, String TipoComprobante) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("INSERT OR REPLACE INTO secuencial(secuencial, sucursalid, codigoestablecimiento, puntoemision, tipocomprobante) VALUES(?, ?, ?, ?, ?) ", new String[]{String.valueOf(Secuencial), this.IdSucursal.toString(), this.CodigoEstablecimiento, this.PuntoEmision, TipoComprobante});
            sqLiteDatabase.close();
            Log.d("TAGSUCURSAL", "SECUENCIAL ACTUALIZADO");
            return true;
        } catch (Exception ec) {
            Log.d("TAGSUCURSAL", "actualizasecuencial(): " + ec.getMessage());
            ec.printStackTrace();
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return this.NombreSucursal;
    }
}
