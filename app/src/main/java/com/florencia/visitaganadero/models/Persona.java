package com.florencia.visitaganadero.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.visitaganadero.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class Persona {
    public Integer idpersona;
    public String tiponip;
    public String nip;
    public String razonsocial;
    public String nombrecomercial;
    public String direccion;
    public Double lat, lon;
    public String categoria;
    public Integer usuarioid;
    public String fono1, fono2, email, observacion, ruc;
    public Integer codigosistema, actualizado, establecimientoid;
    public String tipopersona, fechanacimiento, tipoproveedor;
    public List<Propiedad> propiedades;
    public List<Foto> fotos;

    public static SQLiteDatabase sqLiteDatabase;

    public Persona(){
        this.idpersona= 0;
        this.tiponip= "";
        this.nip= "";
        this.razonsocial= "";
        this.nombrecomercial= "";
        this.direccion= "";
        this.lat = 0d;
        this.lon = 0d;
        this.categoria= "";
        this.usuarioid= 0;
        this.fono1 = "";
        this.fono2 ="";
        this.email="";
        this.observacion="";
        this.ruc= "";
        this.codigosistema = 0;
        this.actualizado =0;
        this.establecimientoid = 0;
        this.tipopersona = "";
        this.fechanacimiento = "";
        this.tipoproveedor = "";
        this.propiedades = new ArrayList<>();
        this.fotos = new ArrayList<>();
    }

    public static boolean removePersonas(Integer idUsuario) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM persona WHERE codigosistema <> ?", new String[] {"0"});
            sqLiteDatabase.close();
            Log.d("TAGCLIENTE", "CLIENTES ELIMINADOS");
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d("TAGCLIENTE", ec.getMessage());
        }
        return false;
    }

    public boolean Save() {
        try {
            this.sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            if (this.idpersona == 0)
                this.sqLiteDatabase.execSQL("INSERT INTO " +
                        "persona(tiponip, nip, razonsocial, nombrecomercial, direccion, lat, lon, categoria, " +
                                "usuarioid, fono1, fono2, email, observacion, ruc, codigosistema, actualizado, establecimientoid," +
                                "tipopersona, fechanacimiento, tipoproveedor) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{ this.tiponip, this.nip, this.razonsocial, this.nombrecomercial, this.direccion,
                                this.lat.toString(), this.lon.toString(), this.categoria.equals("")?"A":this.categoria, this.usuarioid.toString(), this.fono1,
                                this.fono2, this.email, this.observacion, this.ruc, this.codigosistema.toString(), this.actualizado.toString(), this.establecimientoid.toString(),
                                this.tipopersona, this.fechanacimiento, this.tipoproveedor});
            else
                this.sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "persona(idpersona, tiponip, nip, razonsocial, nombrecomercial, direccion, lat, lon, categoria, " +
                                "usuarioid, fono1, fono2, email, observacion, ruc, codigosistema, actualizado, establecimientoid," +
                                "tipopersona, fechanacimiento, tipoproveedor) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new String[]{ this.idpersona.toString(), this.tiponip, this.nip, this.razonsocial, this.nombrecomercial, this.direccion,
                                this.lat.toString(), this.lon.toString(), this.categoria.equals("")?"A":this.categoria, this.usuarioid.toString(), this.fono1,
                                this.fono2, this.email, this.observacion, this.ruc, this.codigosistema.toString(), this.actualizado.toString(), this.establecimientoid.toString(),
                                this.tipopersona, this.fechanacimiento, this.tipoproveedor});
            if (this.idpersona == 0) this.idpersona = SQLite.sqlDB.getLastId();

            Foto.SaveList(this.idpersona, 0, this.fotos);

            this.sqLiteDatabase.close();
            Log.d("TAGCLIENTE","SAVE CLIENTE OK");
            return true;
        } catch (SQLException ex){
            Log.d("TAGCLIENTE",String.valueOf(ex));
            return false;
        }
    }

    public static Persona get(Integer Id, boolean buscadetalle) {
        Persona Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM persona WHERE idpersona  = ?", new String[]{Id.toString()});
            if (cursor.moveToFirst()) Item = Persona.AsignaDatos(cursor, buscadetalle);
            sqLiteDatabase.close();
        }catch (Exception e){
            Log.d("TAGCLIENTE",e.getMessage());
        }
        return Item;
    }

    public static Persona get(String nip, boolean buscadetalle) {
        Persona Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM persona WHERE nip  = ?", new String[]{nip});
            if (cursor.moveToFirst()) Item = Persona.AsignaDatos(cursor, buscadetalle);
            sqLiteDatabase.close();
        }catch (SQLiteException e){
            Log.d("TAGCLIENTE", e.getMessage());
        }
        return Item;
    }

    public static Persona AsignaDatos(Cursor cursor, boolean buscadetalle) {
        Persona Item = null;
        try {
            Item = new Persona();
            Item.idpersona = cursor.getInt(0);
            Item.tiponip = cursor.getString(1);
            Item.nip = cursor.getString(2);
            Item.razonsocial = cursor.getString(3);
            Item.nombrecomercial = cursor.getString(4);
            Item.direccion = cursor.getString(5);
            Item.lat = cursor.getDouble(6);
            Item.lon = cursor.getDouble(7);
            Item.categoria = cursor.getString(8);
            Item.usuarioid = cursor.getInt(9);
            Item.fono1 = cursor.getString(10);
            Item.fono2 = cursor.getString(11);
            Item.email = cursor.getString(12);
            Item.observacion = cursor.getString(13);
            Item.ruc = cursor.getString(14);
            Item.codigosistema = cursor.getInt(15);
            Item.actualizado = cursor.getInt(16);
            Item.establecimientoid= cursor.getInt(17);
            Item.tipopersona= cursor.getString(18);
            Item.fechanacimiento = cursor.getString(19);
            Item.tipoproveedor = cursor.getString(20);
            if(buscadetalle) {
                Item.propiedades = Propiedad.getByPropietario(Item.idpersona);
                Item.fotos = Foto.getLista(Item.idpersona, 0);
            }
        } catch (SQLiteException ec) {
            Log.d("TAGCLIENTE", ec.getMessage());
        }
        return Item;
    }

    public String Validate() {
        String str = "";
        if (this.nip.equals("")) str += "   -   NIP" + '\n';
        if (this.razonsocial.equals("")) str += "  -  Razón Social" + '\n';
        if (this.nombrecomercial.equals("")) str += "   -   Nombre de la tienda o comercio" + '\n';
        if (this.fono1.equals("") && this.fono2.equals("")) str += "   -   Celular o convencional" + '\n';
        if (this.direccion.equals("")) str += "   -   Dirección" + '\n';
        //if (this.lat == 0 || this.lon == 0) str += "   -   Coordenadas. Por favor verificar que el GPS esté activo";
        return str;
    }

    public static List<Persona> getPersonas(Integer idUser, boolean buscadetalle) {
        List<Persona> lista = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM persona ORDER BY razonsocial", null);
            Persona cliente = new Persona();
            if (cursor.moveToFirst()) {
                do {
                    cliente = Persona.AsignaDatos(cursor, buscadetalle);
                    if (cliente != null) lista.add(cliente);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d("TAGCLIENTE",ec.getMessage());
            ec.printStackTrace();
        }
        return lista;
    }

    public static ArrayList<Persona> getPersonasSC(Integer idUser) {
        ArrayList<Persona> items = new ArrayList<Persona>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            //Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM persona where nip <> ? and (codigosistema = 0 or actualizado = 1) ORDER BY razonsocial", new String[]{"9999999999999"});
            Cursor cursor = sqLiteDatabase.rawQuery(
                    "SELECT DISTINCT * FROM persona WHERE codigosistema = 0 OR actualizado = 1 " +
                    "UNION " +
                    "SELECT pe.* FROM persona pe " +
                    "JOIN propiedad pro ON (pro.propietarioid = pe.idpersona OR pro.nip_propietario = pe.nip) AND (pro.codigosistema = 0 OR pro.actualizado = 1)", null);
            Persona cliente;
            if (cursor.moveToFirst()) {
                do {
                    cliente = Persona.AsignaDatos(cursor, true);
                    if (cliente != null) items.add(cliente);
                }while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            ec.printStackTrace();
        }
        return items;
    }

    public static boolean Update(Integer id, ContentValues data) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.update("persona",data, "idpersona = ?",new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d("TAGCLIENTE","UPDATE CLIENTE OK");
            return true;
        } catch (SQLException ex){
            Log.d("TAGCLIENTE", "Update(): " + String.valueOf(ex));
            return false;
        }
    }
}
