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

public class Propiedad {
    public Integer idpropiedad, codigosistema;
    public String nombrepropiedad;
    public Integer propietarioid;
    public Persona administrador;
    public String fecha_adquisicion;
    public Double area, lat, lon;
    public String caracteristicas_fisograficas, descripcion_usos_suelo, condiciones_accesibilidad,caminos_principales,
    caminos_secundarios, fuentes_agua, norte, sur, este, oeste, cobertura_forestal, razas_ganado;
    public Integer num_vacas_paridas, num_vacas_preñadas, num_vacas_solteras, num_terneros, num_toros,
    num_equinos, num_aves, num_cerdos, num_mascotas;
    public String otros, nip_administrador, direccion, nip_propietario;
    public Integer actualizado, parroquiaid, usuarioid;
    public List<UsoSuelo> listaUsoSuelo;
    public List<FodaPropiedad> listaFoda;
    public List<FodaPropiedad> limitaciones;
    public List<FodaPropiedad> oportunidades;
    public List<FodaPropiedad> situaciones;
    public List<Foto> fotos;

    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAGPROPIEDAD";

    public Propiedad(){
        this.idpropiedad = 0;
        this.codigosistema = 0;
        this.nombrepropiedad = "";
        this.propietarioid = 0;
        this.administrador = new Persona();
        this.fecha_adquisicion = "";
        this.area = 0d;
        this.caracteristicas_fisograficas="";
        this.descripcion_usos_suelo="";
        this.condiciones_accesibilidad= "";
        this.caminos_principales = "";
        this.caminos_secundarios= "";
        this.fuentes_agua = "";
        this.norte="";
        this.sur = "";
        this.este = "";
        this.oeste = "";
        this.cobertura_forestal = "";
        this.razas_ganado = "";
        this.num_vacas_paridas = 0;
        this.num_vacas_preñadas = 0;
        this.num_vacas_solteras = 0;
        this.num_terneros= 0;
        this.num_toros = 0;
        this.num_equinos=0;
        this.num_aves =0;
        this.num_cerdos = 0;
        this.num_mascotas = 0;
        this.otros = "";
        this.actualizado = 0;
        this.parroquiaid = 0;
        this.usuarioid = 0;
        this.nip_administrador = "";
        this.direccion = "";
        this.lat = 0d;
        this.lon = 0d;
        this.nip_propietario = "";
        this.listaUsoSuelo = new ArrayList<>();
        this.listaFoda = new ArrayList<>();
        this.limitaciones = new ArrayList<>();
        this.oportunidades = new ArrayList<>();
        this.situaciones = new ArrayList<>();
        this.fotos = new ArrayList<>();
    }

    public boolean Save() {
        boolean retorno = false;
        try {
            this.sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();

            if (this.idpropiedad == 0)
                this.sqLiteDatabase.execSQL("INSERT INTO " +
                                "propiedad(codigosistema,nombrepropiedad,propietarioid,administradorid," +
                                "fecha_adquisicion,area,caracteristicas_fisograficas,descripcion_usos_suelo," +
                                "condiciones_accesibilidad,caminos_principales,caminos_secundarios,fuentes_agua,norte," +
                                "sur,este,oeste,cobertura_forestal,razas_ganado,num_vacas_paridas,num_vacas_preñadas," +
                                "num_vacas_solteras,num_terneros,num_toros,num_equinos,num_aves,num_cerdos,num_mascotas, otros, actualizado, parroquiaid," +
                                "usuarioid, nip_administrador, direccion, lat, lon, nip_propietario) " +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?, ?,?,?,?,?,?)",
                        new String[]{ codigosistema.toString(),nombrepropiedad,propietarioid.toString(),administrador.idpersona.toString(),
                                fecha_adquisicion,area.toString(),caracteristicas_fisograficas,descripcion_usos_suelo,
                                condiciones_accesibilidad,caminos_principales,caminos_secundarios,fuentes_agua,norte,
                                sur,este,oeste,cobertura_forestal,razas_ganado,num_vacas_paridas.toString(),num_vacas_preñadas.toString(),
                                num_vacas_solteras.toString(),num_terneros.toString(),num_toros.toString(),num_equinos.toString(),
                                num_aves.toString(),num_cerdos.toString(),num_mascotas.toString(), otros, actualizado.toString(), parroquiaid.toString(),
                                usuarioid.toString(), nip_administrador, direccion, lat.toString(), lon.toString(), nip_propietario});
            else
                this.sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                        "propiedad(idpropiedad, codigosistema,nombrepropiedad,propietarioid,administradorid," +
                            "fecha_adquisicion,area,caracteristicas_fisograficas,descripcion_usos_suelo," +
                            "condiciones_accesibilidad,caminos_principales,caminos_secundarios,fuentes_agua,norte," +
                            "sur,este,oeste,cobertura_forestal,razas_ganado,num_vacas_paridas,num_vacas_preñadas," +
                            "num_vacas_solteras,num_terneros,num_toros,num_equinos,num_aves,num_cerdos,num_mascotas, " +
                            "otros, actualizado, parroquiaid,usuarioid, nip_administrador, direccion, lat, lon, nip_propietario) " +
                            "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?)",
                        new String[]{idpropiedad.toString(), codigosistema.toString(),nombrepropiedad,propietarioid.toString(),administrador.idpersona.toString(),
                                fecha_adquisicion,area.toString(),caracteristicas_fisograficas,descripcion_usos_suelo,
                                condiciones_accesibilidad,caminos_principales,caminos_secundarios,fuentes_agua,norte,
                                sur,este,oeste,cobertura_forestal,razas_ganado,num_vacas_paridas.toString(),num_vacas_preñadas.toString(),
                                num_vacas_solteras.toString(),num_terneros.toString(),num_toros.toString(),num_equinos.toString(),
                                num_aves.toString(),num_cerdos.toString(),num_mascotas.toString(), otros, actualizado.toString(), parroquiaid.toString(),
                                usuarioid.toString(), nip_administrador, direccion, lat.toString(), lon.toString(), nip_propietario});
            if (this.idpropiedad == 0) this.idpropiedad= SQLite.sqlDB.getLastId();
            retorno = true;
            this.sqLiteDatabase.close();
            if(this.listaUsoSuelo.size()>0)
                retorno = retorno && UsoSuelo.SaveList(this.idpropiedad, this.listaUsoSuelo);
            if(this.listaFoda.size()>0)
                retorno = retorno && FodaPropiedad.SaveList(this.propietarioid, this.idpropiedad, this.listaFoda);
            if(this.fotos.size()>0)
                retorno = retorno && Foto.SaveList(this.propietarioid, this.idpropiedad, this.fotos);
            Log.d(TAG,"SAVE PROPIEDAD OK");
        } catch (SQLException ex){
            Log.d(TAG,String.valueOf(ex));
            return false;
        }
        return retorno;
    }

    public static boolean SaveList(List<Propiedad> propiedades) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Propiedad pro:propiedades) {

                if (pro.idpropiedad == 0)
                    sqLiteDatabase.execSQL("INSERT INTO " +
                                    "propiedad(codigosistema,nombrepropiedad,propietarioid,administradorid," +
                                    "fecha_adquisicion,area,caracteristicas_fisograficas,descripcion_usos_suelo," +
                                    "condiciones_accesibilidad,caminos_principales,caminos_secundarios,fuentes_agua,norte," +
                                    "sur,este,oeste,cobertura_forestal,razas_ganado,num_vacas_paridas,num_vacas_preñadas," +
                                    "num_vacas_solteras,num_terneros,num_toros,num_equinos,num_aves,num_cerdos,num_mascotas, otros, actualizado, parroquiaid," +
                                    "usuarioid, nip_administrador, direccion, lat, lon, nip_propietario) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?,?,?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?, ?,?,?,?)",
                            new String[]{pro.codigosistema.toString(), pro.nombrepropiedad, pro.propietarioid.toString(), pro.administrador.idpersona.toString(),
                                    pro.fecha_adquisicion, pro.area.toString(), pro.caracteristicas_fisograficas, pro.descripcion_usos_suelo,
                                    pro.condiciones_accesibilidad, pro.caminos_principales, pro.caminos_secundarios, pro.fuentes_agua, pro.norte,
                                    pro.sur, pro.este, pro.oeste, pro.cobertura_forestal, pro.razas_ganado, pro.num_vacas_paridas.toString(), pro.num_vacas_preñadas.toString(),
                                    pro.num_vacas_solteras.toString(), pro.num_terneros.toString(), pro.num_toros.toString(), pro.num_equinos.toString(),
                                    pro.num_aves.toString(), pro.num_cerdos.toString(), pro.num_mascotas.toString(), pro.otros, pro.actualizado.toString(), pro.parroquiaid.toString(),
                                    pro.usuarioid.toString(), pro.nip_administrador, pro.direccion, pro.lat.toString(), pro.lon.toString(), pro.nip_propietario});
                else
                    sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                    "propiedad(idpropopiedad, codigosistema,nombrepropiedad,propietarioid,administradorid," +
                                    "fecha_adquisicion,area,caracteristicas_fisograficas,descripcion_usos_suelo," +
                                    "condiciones_accesibilidad,caminos_principales,caminos_secundarios,fuentes_agua,norte," +
                                    "sur,este,oeste,cobertura_forestal,razas_ganado,num_vacas_paridas,num_vacas_preñadas," +
                                    "num_vacas_solteras,num_terneros,num_toros,num_equinos,num_aves,num_cerdos,num_mascotas, otros, actualizado, parroquiaid," +
                                    "usuarioid, nip_administrador, direccion, lat, lon, nip_propietario) " +
                                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?, ?,?,?,?)",
                            new String[]{pro.idpropiedad.toString(), pro.codigosistema.toString(), pro.nombrepropiedad, pro.propietarioid.toString(), pro.administrador.idpersona.toString(),
                                    pro.fecha_adquisicion, pro.area.toString(), pro.caracteristicas_fisograficas, pro.descripcion_usos_suelo,
                                    pro.condiciones_accesibilidad, pro.caminos_principales, pro.caminos_secundarios, pro.fuentes_agua, pro.norte,
                                    pro.sur, pro.este, pro.oeste, pro.cobertura_forestal, pro.razas_ganado, pro.num_vacas_paridas.toString(), pro.num_vacas_preñadas.toString(),
                                    pro.num_vacas_solteras.toString(), pro.num_terneros.toString(), pro.num_toros.toString(), pro.num_equinos.toString(),
                                    pro.num_aves.toString(), pro.num_cerdos.toString(), pro.num_mascotas.toString(), pro.otros, pro.actualizado.toString(), pro.parroquiaid.toString(),
                                    pro.usuarioid.toString(), pro.nip_administrador, pro.direccion, pro.lat.toString(), pro.lon.toString(), pro.nip_propietario});
                if (pro.idpropiedad == 0) pro.idpropiedad = SQLite.sqlDB.getLastId();

                UsoSuelo.SaveList(pro.idpropiedad, pro.listaUsoSuelo);
                FodaPropiedad.SaveList(pro.propietarioid, pro.idpropiedad,pro.listaFoda);
                Foto.SaveList(pro.propietarioid, pro.idpropiedad, pro.fotos);
            }
            sqLiteDatabase.close();
            Log.d(TAG,"SAVE LISTA DE PROPIEDADES OK");
            return true;
        } catch (SQLException ex){
            Log.d(TAG,ex.getMessage());
            return false;
        }
    }

    public static Propiedad getById(Integer Id) {
        Propiedad Item = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM propiedad WHERE idpropiedad  = ?", new String[]{Id.toString()});
            if (cursor.moveToFirst()) Item = Propiedad.AsignaDatos(cursor);
            sqLiteDatabase.close();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return Item;
    }

    public static List<Propiedad> getByPropietario(Integer idPropietario) {
        List<Propiedad> retorno = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM propiedad WHERE propietarioid  = ?", new String[]{idPropietario.toString()});
            if (cursor.moveToFirst()) {
                do {
                    Propiedad Item = Propiedad.AsignaDatos(cursor);
                    if(Item!=null)
                        retorno.add(Item);
                }while (cursor.moveToNext());
            }
            sqLiteDatabase.close();
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return retorno;
    }

    public static ArrayList<Propiedad> getPropiedadesSC(Integer idPropietario) {
        ArrayList<Propiedad> items = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM propiedad where propietarioid = ? and (codigosistema = 0 or actualizado = 1)", new String[]{idPropietario.toString()});
            Propiedad propiedad;
            if (cursor.moveToFirst()) {
                do {
                    propiedad = Propiedad.AsignaDatos(cursor);
                    if (propiedad != null) items.add(propiedad);
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
            sqLiteDatabase.update("propiedad",data, "idpropiedad = ?",new String[]{id.toString()});
            sqLiteDatabase.close();
            Log.d(TAG,"UPDATE propiedad OK");
            return true;
        } catch (SQLException ex){
            Log.d(TAG, "Update(): " + String.valueOf(ex));
            return false;
        }
    }

    public static boolean removePropiedades(Integer idUsuario) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            sqLiteDatabase.execSQL("DELETE FROM propiedad WHERE codigosistema <> ? and usuarioid = ?", new String[] {"0", idUsuario.toString()});
            sqLiteDatabase.close();
            Log.d(TAG, "PROPIEDADES ELIMINADOS");
            return true;
        } catch (Exception ec) {
            ec.printStackTrace();
            Log.d(TAG, ec.getMessage());
        }
        return false;
    }

    public static Propiedad AsignaDatos(Cursor cursor) {
        Propiedad Item = null;
        try {
            Item = new Propiedad();
            Item.idpropiedad = cursor.getInt(0);
            Item.codigosistema = cursor.getInt(1);
            Item.nombrepropiedad = cursor.getString(2);
            Item.propietarioid = cursor.getInt(3);
            Item.administrador = Persona.get(cursor.getInt(4),false);
            Item.fecha_adquisicion = cursor.getString(5);
            Item.area = cursor.getDouble(6);
            Item.caracteristicas_fisograficas = cursor.getString(7);
            Item.descripcion_usos_suelo = cursor.getString(8);
            Item.condiciones_accesibilidad = cursor.getString(9);
            Item.caminos_principales = cursor.getString(10);
            Item.caminos_secundarios = cursor.getString(11);
            Item.fuentes_agua = cursor.getString(12);
            Item.norte = cursor.getString(13);
            Item.sur = cursor.getString(14);
            Item.este = cursor.getString(15);
            Item.oeste = cursor.getString(16);
            Item.cobertura_forestal = cursor.getString(17);
            Item.razas_ganado = cursor.getString(18);
            Item.num_vacas_paridas = cursor.getInt(19);
            Item.num_vacas_preñadas = cursor.getInt(20);
            Item.num_vacas_solteras = cursor.getInt(21);
            Item.num_terneros = cursor.getInt(22);
            Item.num_toros = cursor.getInt(23);
            Item.num_equinos = cursor.getInt(24);
            Item.num_aves = cursor.getInt(25);
            Item.num_cerdos = cursor.getInt(26);
            Item.num_mascotas = cursor.getInt(27);
            Item.otros = cursor.getString(28);
            Item.actualizado = cursor.getInt(29);
            Item.parroquiaid = cursor.getInt(30);
            Item.usuarioid = cursor.getInt(31);
            Item.nip_administrador = cursor.getString(32);
            if(Item.administrador==null || Item.administrador.nip.equals(""))
                Item.administrador = Persona.get(Item.nip_administrador,false);
            Item.direccion = cursor.getString(33);
            Item.lat = cursor.getDouble(34);
            Item.lon = cursor.getDouble(35);
            Item.nip_propietario = cursor.getString(36);
            Item.listaUsoSuelo = UsoSuelo.getLista(Item.idpropiedad);
            Item.limitaciones = FodaPropiedad.getLista(Item.idpropiedad,0);
            Item.oportunidades = FodaPropiedad.getLista(Item.idpropiedad,1);
            Item.situaciones = FodaPropiedad.getLista(Item.idpropiedad,2);
            Item.fotos = Foto.getLista(Item.propietarioid, Item.idpropiedad);

            if(Item.limitaciones==null)
                Item.limitaciones = new ArrayList<>();
            if(Item.oportunidades==null)
                Item.oportunidades = new ArrayList<>();
            if(Item.situaciones==null)
                Item.situaciones = new ArrayList<>();
            if(Item.fotos == null)
                Item.fotos = new ArrayList<>();
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }
}
