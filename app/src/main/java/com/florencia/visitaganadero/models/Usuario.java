package com.florencia.visitaganadero.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.util.Log;

import com.florencia.visitaganadero.services.GPSTracker;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;

public class Usuario {
    public int IdUsuario;
    public String Usuario;
    public String RazonSocial;
    public String Clave;
    public String Pin;
    public Sucursal sucursal;
    public Integer ParroquiaID;
    public int Autorizacion;
    public int Perfil;

    public List<Permiso> permisos;

    public static SQLiteDatabase sqLiteDatabase;

    public Usuario() {
        this.RazonSocial = "";
        this.Usuario = "";
        this.Clave = "";
        this.Perfil = 0;
        this.Pin = "";
        this.sucursal = new Sucursal();
        this.Autorizacion = 0;
        this.ParroquiaID = 0;
        this.permisos = new ArrayList<>();
        //this.context = context;
    }

    public String Codigo() {return String.valueOf(this.IdUsuario); }

    public boolean Guardar(){
        try {
            Permiso.Delete(this.Perfil);
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                            "usuario(idusuario, razonsocial, usuario, clave, perfil, autorizacion, pin, sucursalid, parroquiaid) " +
                            "values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{
                            String.valueOf(this.IdUsuario),
                            this.RazonSocial,
                            this.Usuario,
                            this.Clave,
                            String.valueOf(this.Perfil),
                            String.valueOf(this.Autorizacion),
                            this.Pin,
                            this.sucursal.IdEstablecimiento == null ? "0": this.sucursal.IdEstablecimiento.toString(),
                            this.ParroquiaID.toString()
                    }
            );
            sqLiteDatabase.close();
            Log.d("TAG", "USUARIO INGRESADO LOCALMENTE");
            return Permiso.SaveLista(this.permisos);
        } catch (SQLException ex){
            Log.d("TAG", ex.getMessage());
            return false;
        }
    }

    static public Usuario getUsuario(Integer id){
        Usuario Item = null;
        sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario WHERE idusuario = ?", new String[]{ id.toString() });
        if (cursor.moveToFirst()) {
            Item = AsignaDatos(cursor);
        }
        cursor.close();
        sqLiteDatabase.close();
        return Item;
    }

    private static Usuario AsignaDatos(Cursor cursor){
        Usuario Item = new Usuario();
        Item.IdUsuario = cursor.getInt(0);
        Item.RazonSocial = cursor.getString(1);
        Item.Usuario = cursor.getString(2);
        Item.Clave = cursor.getString(3);
        Item.Autorizacion = cursor.getInt(4);
        Item.Pin = cursor.getString(5);
        Item.Perfil = cursor.getInt(6);
        Item.sucursal =  Sucursal.getSucursal(String.valueOf(cursor.getInt(7)));
        Item.ParroquiaID = cursor.getInt(8);
        Item.permisos = Permiso.getPermisos(Item.Perfil);

        return Item;
    }

    static public Usuario Login(String Pin)
    {
        Usuario Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario WHERE pin = ?", new String[]{ Pin });
            if (cursor.moveToFirst()) {
                Item = AsignaDatos(cursor);
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch(SQLException ex)
        {
            Log.d("LOGINERROR",String.valueOf(ex));
            ex.printStackTrace();
        }
        Log.d("LOGINITEM",String.valueOf(Item));
        return Item;
    }

    static public Usuario Login(String User, String Password)
    {
        Usuario Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario WHERE usuario = ? and clave = ?", new String[]{ User, Password });
            if (cursor.moveToFirst()) {
                Item = AsignaDatos(cursor);
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch(SQLException ex)
        {
            Log.d("LOGINERROR",String.valueOf(ex));
            ex.printStackTrace();
        }
        Log.d("LOGINITEM",String.valueOf(Item));
        return Item;
    }

    static public List<Usuario> getUsuarios(){
        List<Usuario> Items = new ArrayList<Usuario>();
        sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM usuario", null);
        Usuario Item;
        if (cursor.moveToFirst()) {
            do {
                Item = AsignaDatos(cursor);
                if (Item != null) Items.add(Item);
            } while(cursor.moveToNext());
        }
        cursor.close();
        sqLiteDatabase.close();
        return Items;
    }

    public boolean removeClientes() {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM cliente WHERE usuario = ? AND codigosistema <> 0", new String[] {this.Codigo()});
            sqLiteDatabase.close();
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
        }
        return false;
    }

    public HashMap<String, String> MapLogin() {
        HashMap<String, String> nMap = null;
        nMap = new HashMap<>();
        nMap.put("usuario", this.Usuario);
        nMap.put("clave", this.Clave);
        return nMap;
    }

    public void updatePosicion(int i, int est) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("UPDATE posicion SET estado = ? WHERE idposicion = ?", new String[] {String.valueOf(est), String.valueOf(i)});
            sqLiteDatabase.close();
        } catch (Exception ec) {
            ec.printStackTrace();
        }
    }

    public JsonArray getResumenDocumentos(String fecha){
        JsonArray resumen = new JsonArray();
        try{
            String query =
                "select 'FACTURAS' documento, count(co.idcomprobante) as cantidad, round(ifnull(sum(co.total),0),2) as total from comprobante co where co.tipotransaccion = '01' and estado >= 0 and fechadocumento = '"+fecha+"'" +
                " UNION" +
                " select 'RECEPCIONES', count(co.idcomprobante), round(ifnull(sum(co.total),0),2) from comprobante co where co.tipotransaccion = '8' and estado >= 0 and fechadocumento = '"+fecha+"'" +
                " UNION" +
                " select 'TRANSFERENCIAS', count(co.idcomprobante), round(ifnull(sum(co.total),0),2) from comprobante co where co.tipotransaccion = '4' and estado >= 0 and fechadocumento = '"+fecha+"'" +
                " UNION" +
                " select 'PEDIDOS', count(pe.idpedido), round(ifnull(sum(pe.total),0),2) from pedido pe where pe.estado >= 0 and fechapedido = '"+fecha+"'";

            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            Usuario Item;
            if (cursor.moveToFirst()) {
                do {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("documento", cursor.getString(0));
                    obj.addProperty("cantidad", cursor.getString(1));
                    obj.addProperty("total", cursor.getString(2));
                    resumen.add(obj);
                } while(cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();

        }catch (SQLiteException e){
            Log.d("TAG_USUARIO", "getResumenDocumentos(): " + e.getMessage());
        }
        return resumen;
    }

    public boolean GuardarSesionLocal (Context context){
        //Crea preferencia
        SharedPreferences sharedPreferences= context.getSharedPreferences("DatosSesion", MODE_PRIVATE);
        String conexionactual = sharedPreferences.getString("conexionactual","");
        SharedPreferences.Editor editor=sharedPreferences.edit()
                .putInt("idUser", this.IdUsuario)
                .putString("usuario", this.Usuario)
                .putString("pin", this.Pin)
                .putString("ultimaconexion", conexionactual)
                .putString("conexionactual", Utils.getDateFormat("yyyy-MM-dd HH:mm:ss"));
        return editor.commit();
    }

    public boolean CerrarSesionLocal (Context context){
        //Crea preferencia
        SharedPreferences sharedPreferences= context.getSharedPreferences("DatosSesion", MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit()
                .clear();
        return editor.commit();
    }

    public boolean VerificaPermiso(Context context, String opcion, String permiso){
        boolean retorno = false;
        Permiso mipermiso=null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<Permiso> collect = permisos.stream().filter(i -> i.nombreopcion.trim().equals(opcion.trim().toUpperCase())).
                        collect(Collectors.toList());
                if (collect != null && collect.size() > 0)
                    mipermiso = collect.get(0);
            } else {
                for (Permiso detalle : permisos) {
                    if (detalle.nombreopcion.equals(permiso.trim().toUpperCase())) {
                        mipermiso = detalle;
                        break;
                    }
                }
            }

            if (mipermiso != null) {
                Log.d("TAGPERMISO",opcion);
                switch (permiso) {
                    case "lectura":
                        retorno = mipermiso.permisoimpresion.equals("t");
                        break;
                    case "escritura":
                        retorno = mipermiso.permisoescritura.equals("t");
                        break;
                    case "modificacion":
                        retorno = mipermiso.permisomodificacion.equals("t");
                        break;
                    case "borrar":
                        retorno = mipermiso.permisoborrar.equals("t");
                        break;
                }
            }
        }catch (Exception e){
            Log.d("TAGPERMISO", "VerificarPermiso(): " + e.getMessage());
            retorno = false;
        }

        return  retorno;
    }
}

