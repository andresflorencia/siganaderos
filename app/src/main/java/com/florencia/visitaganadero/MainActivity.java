package com.florencia.visitaganadero;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.florencia.visitaganadero.activities.ConfigActivity;
import com.florencia.visitaganadero.activities.actLogin;
import com.florencia.visitaganadero.fragments.GanaderoFragment;
import com.florencia.visitaganadero.fragments.PrincipalFragment;
import com.florencia.visitaganadero.interfaces.GanaderoInterface;
import com.florencia.visitaganadero.models.FodaPropiedad;
import com.florencia.visitaganadero.models.Foto;
import com.florencia.visitaganadero.models.Persona;
import com.florencia.visitaganadero.models.Propiedad;
import com.florencia.visitaganadero.models.UsoSuelo;
import com.florencia.visitaganadero.services.GPSTracker;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.florencia.visitaganadero.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.shasin.notificationbanner.Banner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG_MAINACTIVITY";
    View rootView;
    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    Fragment fragment;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    public static List<String> listaFragments = new ArrayList<String>();
    private SharedPreferences sPreferencesSesion;
    private TextView txt_Usuario, txtInfo, txtUltimaConexion, txtSucursal;
    private NavigationView navigation;
    private Gson gson = new Gson();
    private ProgressDialog pbProgreso;
    private OkHttpClient okHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Inicio");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        rootView = findViewById(android.R.id.content);
        sPreferencesSesion = getSharedPreferences("DatosSesion", MODE_PRIVATE);

        navigation= findViewById(R.id.navigation_view);
        txt_Usuario=navigation.getHeaderView(0).findViewById(R.id.txt_Usuario);
        txtInfo = navigation.getHeaderView(0).findViewById(R.id.txtInfo);
        txtSucursal = navigation.getHeaderView(0).findViewById(R.id.txtSucursal);
        txtUltimaConexion =  navigation.findViewById(R.id.txtUltimoAcceso);
        if(sPreferencesSesion!=null){
            String ultimoacceso = sPreferencesSesion.getString("ultimaconexion","");
            txtUltimaConexion.setText(ultimoacceso.length()>0? "Último acceso: " + ultimoacceso:"");
        }
        txt_Usuario.setText(SQLite.usuario.RazonSocial);
        txtInfo.setText("Establecimiento: "+ SQLite.usuario.sucursal.CodigoEstablecimiento
                + " - Punto Emisión: " + SQLite.usuario.sucursal.PuntoEmision );
        try{
            Log.d("TAGNAV", SQLite.usuario.sucursal.NombreSucursal);
            txtSucursal.setText(SQLite.usuario.sucursal.NombreSucursal);
        }catch (Exception e){
            Log.d("TAGNAV", e.getMessage());
        }
        navigation.inflateMenu(R.menu.menu_navigation);
        initNavigationDrawer();
        VerificarPermisos();

        fragmentManager = getSupportFragmentManager();
        fragment = new GanaderoFragment();
        String backStateName = fragment.getClass().getName();
        fragment.setArguments(getIntent().getExtras());
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
        listaFragments.add(backStateName);

        pbProgreso = new ProgressDialog(this);

        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        if(SQLite.gpsTracker==null)
            SQLite.gpsTracker = new GPSTracker(this);
    }

    private void agregaFragment(String backStateName){
        fragment.setArguments(getIntent().getExtras());
        fragmentTransaction = fragmentManager.beginTransaction();
        if(!listaFragments.contains(backStateName)) {
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.addToBackStack(backStateName);
            fragmentTransaction.commit();
            listaFragments.add(backStateName);
        }else{
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                boolean retorno=true;
                int id = menuItem.getItemId();
                //getSupportActionBar().setTitle(menuItem.getTitle());
                String backStateName;
                Intent i;

                switch (id) {
                    case R.id.nav_home:
                    case R.id.nav_ganadero:
                        menuItem.setChecked(true);
                        if(id == R.id.nav_home)
                            fragment = new PrincipalFragment();
                        else if(id == R.id.nav_ganadero)
                            fragment = new GanaderoFragment();

                        backStateName = fragment.getClass().getName();
                        agregaFragment(backStateName);
                        break;

                    case R.id.nav_config:
                        i = new Intent(MainActivity.this, ConfigActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_cerrarsesion:
                        if(SQLite.usuario != null) {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
                            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_confirmation_dialog,
                                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
                            builder.setView(view);
                            ((TextView)view.findViewById(R.id.lblTitle)).setText("Salir");
                            ((TextView)view.findViewById(R.id.lblMessage)).setText("¿Está seguro que desea cerrar sesión?");
                            ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_exit);
                            ((Button)view.findViewById(R.id.btnCancel)).setText("Cancelar");
                            ((Button)view.findViewById(R.id.btnConfirm)).setText("Si");
                            final android.app.AlertDialog alertDialog = builder.create();
                            view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(SQLite.usuario.CerrarSesionLocal(getApplicationContext())) {
                                        Intent i = new Intent(MainActivity.this, actLogin.class);
                                        startActivity(i);
                                        alertDialog.dismiss();
                                        finish();
                                    }
                                }
                            });

                            view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) { alertDialog.dismiss();}
                            });

                            if(alertDialog.getWindow()!=null)
                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                            alertDialog.show();
                        }
                        break;

                }
                return retorno;
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        /*menu.findItem(R.id.option_sincronizacomprobantes)
                .setVisible(
                        SQLite.usuario.VerificaPermiso(this,"Punto de Venta","escritura")
                );*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()){
            case R.id.option_sincronizaganderos:
                sincronizaGanaderos(getApplicationContext());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sincronizaGanaderos(final Context context) {
        try{
            if(!Utils.isOnlineNet(SQLite.configuracion.urlbase)) {
                //Utils.showErrorDialog(this,"Error", Constants.MSG_COMPROBAR_CONEXION_INTERNET);
                Banner.make(rootView, this, Banner.ERROR, Constants.MSG_COMPROBAR_CONEXION_INTERNET, Banner.BOTTOM,2000).show();
                return;
            }

            List<Persona> listGanaderos = Persona.getPersonasSC(SQLite.usuario.IdUsuario);
            if(listGanaderos == null) {
                listGanaderos = new ArrayList<>();
            }else{
                for(Persona ganadero: listGanaderos){
                    if(ganadero.fotos==null)
                        continue;
                    for(int i = 0; i<ganadero.fotos.size(); i++){
                        try {
                            String ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;
                            File miFile = new File(ExternalDirectory, ganadero.fotos.get(i).name);
                            Uri path = Uri.fromFile(miFile);
                            ganadero.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                    MainActivity.this.getContentResolver(),
                                    path);
                            ganadero.fotos.get(i).image_base = Utils.convertImageToString(ganadero.fotos.get(i).bitmap);
                        } catch (IOException e) {
                            Log.d(TAG, "NotFound(): " + e.getMessage());
                        }
                    }

                    //CONVERTIR A STRING LAS IMAGENES DE LAS PROPIEDAS
                    if(ganadero.propiedades.size()>0){
                        for(Propiedad propiedad:ganadero.propiedades) {
                            for (int i = 0; i < propiedad.fotos.size(); i++) {
                                try {
                                    String ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;
                                    File miFile = new File(ExternalDirectory, propiedad.fotos.get(i).name);
                                    Uri path = Uri.fromFile(miFile);
                                    propiedad.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                            MainActivity.this.getContentResolver(),
                                            path);
                                    propiedad.fotos.get(i).image_base = Utils.convertImageToString(propiedad.fotos.get(i).bitmap);
                                } catch (IOException e) {
                                    Log.d(TAG, "NotFound(): " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }

            pbProgreso.setTitle("Sincronizando ganaderos");
            pbProgreso.setMessage("Espere un momento...");
            pbProgreso.setCancelable(false);
            pbProgreso.show();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            GanaderoInterface miInterface = retrofit.create(GanaderoInterface.class);

            SQLite.gpsTracker.getLastKnownLocation();
            Map<String,Object> post = new HashMap<>();
            post.put("usuario",SQLite.usuario.Usuario);
            post.put("clave",SQLite.usuario.Clave);
            post.put("ganaderos", listGanaderos);
            //post.put("lat", SQLite.gpsTracker.getLatitude());
            //post.put("lon", SQLite.gpsTracker.getLongitude());
            String json = post.toString();
            Call<JsonObject> call=null;
            call=miInterface.loadGanaderos2(post);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(!response.isSuccessful()){
                        Utils.showMessage(context,"Error: " + response.code());
                        pbProgreso.dismiss();
                        return;
                    }
                    try {

                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                JsonArray jsonGanaderosUpdate = obj.getAsJsonArray("ganaderosupdate");
                                if(jsonGanaderosUpdate!=null){
                                    for(JsonElement ele:jsonGanaderosUpdate){
                                        JsonObject cli = ele.getAsJsonObject();
                                        ContentValues values = new ContentValues();
                                        values.put("codigosistema", cli.get("codigosistema").getAsInt());
                                        values.put("actualizado",0);
                                        Persona.Update(cli.get("idpersona").getAsInt(),values);

                                        JsonArray jsonPropiedadUpdate = cli.getAsJsonArray("propiedades_update");
                                        if(jsonGanaderosUpdate!=null){
                                            for(JsonElement elep:jsonPropiedadUpdate){
                                                JsonObject prop = elep.getAsJsonObject();
                                                values = new ContentValues();
                                                values.put("codigosistema", prop.get("codigosistema").getAsInt());
                                                values.put("actualizado",0);
                                                Propiedad.Update(prop.get("idpropiedad").getAsInt(),values);
                                            }
                                        }
                                    }
                                }

                                JsonArray jsonClientes = obj.getAsJsonArray("ganaderos");
                                if(jsonClientes!=null){
                                    int numClientUpdate = 0;
                                    if(jsonClientes.size()>0) {
                                        Persona.removePersonas(SQLite.usuario.IdUsuario);
                                        Propiedad.removePropiedades(SQLite.usuario.IdUsuario);
                                    }
                                    for (JsonElement ele : jsonClientes) {
                                        JsonObject clie = ele.getAsJsonObject();
                                        Persona miCliente = new Persona();
                                        miCliente.codigosistema = clie.has("idpersona") ? clie.get("idpersona").getAsInt() : 0;
                                        miCliente.tiponip = clie.get("tiponip").isJsonNull() ? "00" : clie.get("tiponip").getAsString();
                                        miCliente.nip = clie.get("nip").isJsonNull() ? "" : clie.get("nip").getAsString();
                                        miCliente.razonsocial = clie.get("razonsocial").isJsonNull() ? "" : clie.get("razonsocial").getAsString();
                                        miCliente.nombrecomercial = clie.get("nombrecomercial").isJsonNull() ? "" : clie.get("nombrecomercial").getAsString();
                                        miCliente.direccion = clie.get("direccion").isJsonNull() ? "" : clie.get("direccion").getAsString();
                                        miCliente.lat = clie.get("lat").isJsonNull() ? 0 : clie.get("lat").getAsDouble();
                                        miCliente.lon = clie.get("lon").isJsonNull() ? 0 : clie.get("lon").getAsDouble();
                                        miCliente.fono1 = clie.get("fono1").isJsonNull() ? "" : clie.get("fono1").getAsString();
                                        miCliente.fono2 = clie.get("fono2").isJsonNull() ? "" : clie.get("fono2").getAsString();
                                        miCliente.usuarioid = clie.get("usuarioid").isJsonNull() ? SQLite.usuario.IdUsuario : clie.get("usuarioid").getAsInt();
                                        miCliente.categoria = clie.get("categoria").isJsonNull() ? "" : clie.get("categoria").getAsString();
                                        miCliente.email = clie.get("email").isJsonNull() ? "" : clie.get("email").getAsString();
                                        miCliente.observacion = clie.get("observacion").isJsonNull() ? "" : clie.get("observacion").getAsString();
                                        miCliente.ruc = clie.get("ruc").isJsonNull() ? "" : clie.get("ruc").getAsString();

                                        JsonArray jsonFotos;
                                        if(clie.has("fotos")) {
                                            jsonFotos = clie.get("fotos").isJsonNull() ? null : clie.getAsJsonArray("fotos");
                                            if (jsonFotos != null) {
                                                for (JsonElement eleFoto : jsonFotos) {
                                                    JsonObject jFoto = eleFoto.isJsonObject() ? eleFoto.getAsJsonObject() : null;
                                                    if (jFoto == null)
                                                        continue;
                                                    Foto miFoto = new Foto();
                                                    miFoto.name = jFoto.get("name").isJsonNull() ? "" : jFoto.get("name").getAsString();
                                                    miFoto.path = jFoto.get("path_phone").isJsonNull() ? "" : jFoto.get("path_phone").getAsString();
                                                    miCliente.fotos.add(miFoto);
                                                }
                                            }
                                        }

                                        if (miCliente.Save()) {

                                            JsonArray jsonPropiedades = clie.get("propiedades").isJsonNull() ? null : clie.getAsJsonArray("propiedades");
                                            if (jsonPropiedades != null) {
                                                for (JsonElement elePro : jsonPropiedades) {
                                                    JsonObject pro = elePro.isJsonObject() ? elePro.getAsJsonObject() : null;
                                                    if (pro == null)
                                                        continue;
                                                    Propiedad miPropiedad = new Propiedad();
                                                    miPropiedad.codigosistema = pro.get("idpropiedad").isJsonNull()?0: pro.get("idpropiedad").getAsInt();
                                                    miPropiedad.nombrepropiedad = pro.get("nombrepropiedad").isJsonNull()?"": pro.get("nombrepropiedad").getAsString();
                                                    miPropiedad.propietarioid = miCliente.idpersona;
                                                    miPropiedad.administrador.idpersona = pro.get("administradorid").isJsonNull()?miCliente.idpersona: pro.get("administradorid").getAsInt();
                                                    miPropiedad.fecha_adquisicion = pro.get("fecha_adquisicion").isJsonNull()?"": pro.get("fecha_adquisicion").getAsString();
                                                    miPropiedad.area = pro.get("area").isJsonNull()?0d: pro.get("area").getAsDouble();
                                                    miPropiedad.caracteristicas_fisograficas = pro.get("caracteristicas_fisograficas").isJsonNull()?"": pro.get("caracteristicas_fisograficas").getAsString();
                                                    miPropiedad.descripcion_usos_suelo = pro.get("descripcion_usos_suelo").isJsonNull() ? "" :pro.get("descripcion_usos_suelo").getAsString();
                                                    miPropiedad.condiciones_accesibilidad = pro.get("condiciones_accesibilidad").isJsonNull() ? "" :pro.get("condiciones_accesibilidad").getAsString();
                                                    miPropiedad.caminos_principales = pro.get("caminos_principales").isJsonNull() ? "" :pro.get("caminos_principales").getAsString();
                                                    miPropiedad.caminos_secundarios = pro.get("caminos_secundarios").isJsonNull() ? "" :pro.get("caminos_secundarios").getAsString();
                                                    miPropiedad.fuentes_agua = pro.get("fuentes_agua").isJsonNull() ? "" :pro.get("fuentes_agua").getAsString();
                                                    miPropiedad.norte = pro.get("norte").isJsonNull() ? "" :pro.get("norte").getAsString();
                                                    miPropiedad.sur = pro.get("sur").isJsonNull() ? "" :pro.get("sur").getAsString();
                                                    miPropiedad.este = pro.get("este").isJsonNull() ? "" :pro.get("este").getAsString();
                                                    miPropiedad.oeste = pro.get("oeste").isJsonNull() ? "" : pro.get("oeste").getAsString();
                                                    miPropiedad.cobertura_forestal = pro.get("cobertura_forestal").isJsonNull() ? "" : pro.get("cobertura_forestal").getAsString();
                                                    miPropiedad.razas_ganado = pro.get("razas_ganado").isJsonNull() ? "" : pro.get("razas_ganado").getAsString();
                                                    miPropiedad.num_vacas_paridas = pro.get("num_vacas_paridas").isJsonNull() ? 0 : pro.get("num_vacas_paridas").getAsInt();
                                                    miPropiedad.num_vacas_preñadas = pro.get("num_vacas_prenadas").isJsonNull() ? 0 : pro.get("num_vacas_prenadas").getAsInt();
                                                    miPropiedad.num_vacas_solteras = pro.get("num_vacas_solteras").isJsonNull() ? 0 : pro.get("num_vacas_solteras").getAsInt();
                                                    miPropiedad.num_terneros = pro.get("num_terneros").isJsonNull() ? 0 : pro.get("num_terneros").getAsInt();
                                                    miPropiedad.num_toros = pro.get("num_toros").isJsonNull() ? 0 : pro.get("num_toros").getAsInt();
                                                    miPropiedad.num_equinos = pro.get("num_equinos").isJsonNull() ? 0 : pro.get("num_equinos").getAsInt();
                                                    miPropiedad.num_aves = pro.get("num_aves").isJsonNull() ? 0 : pro.get("num_aves").getAsInt();
                                                    miPropiedad.num_cerdos = pro.get("num_cerdos").isJsonNull() ? 0 : pro.get("num_cerdos").getAsInt();
                                                    miPropiedad.num_mascotas = pro.get("num_mascotas").isJsonNull() ? 0 : pro.get("num_mascotas").getAsInt();
                                                    miPropiedad.otros = pro.get("otros").isJsonNull() ? "" : pro.get("otros").getAsString();
                                                    miPropiedad.parroquiaid = pro.get("parroquiaid").isJsonNull() ? 0 : pro.get("parroquiaid").getAsInt();
                                                    miPropiedad.usuarioid = pro.get("usuarioid").isJsonNull() ? 0 : pro.get("usuarioid").getAsInt();
                                                    miPropiedad.nip_administrador = pro.get("nip_administrador").isJsonNull() ? miCliente.nip : pro.get("nip_administrador").getAsString();
                                                    miPropiedad.direccion = pro.get("direccion").isJsonNull() ? "" : pro.get("direccion").getAsString();

                                                    JsonArray jsonUsoSuelo = pro.get("usosuelo").isJsonArray() ? pro.getAsJsonArray("usosuelo") : null;
                                                    if (jsonUsoSuelo != null) {
                                                        if (jsonUsoSuelo.size() > 0) {
                                                            for (JsonElement eleUso : jsonUsoSuelo) {
                                                                JsonObject uso = eleUso.isJsonNull() ? null : eleUso.getAsJsonObject();
                                                                if (uso == null)
                                                                    continue;
                                                                UsoSuelo miUso = new UsoSuelo();
                                                                miUso.propiedadid = uso.get("propiedadid").getAsInt();
                                                                miUso.tipo_cultivo.codigocatalogo = uso.get("tipo_cultivo").getAsString();
                                                                miUso.area_cultivo = uso.get("area_cultivo").getAsDouble();
                                                                miUso.variedad_sembrada = uso.get("variedad_sembrada").getAsString();
                                                                miUso.observacion = uso.get("observacion").getAsString();
                                                                miUso.orden = uso.get("orden").getAsInt();
                                                                miPropiedad.listaUsoSuelo.add(miUso);
                                                            }
                                                        }
                                                    }

                                                    JsonArray jsonFoda = pro.get("foda").isJsonArray() ? pro.getAsJsonArray("foda") : null;
                                                    if (jsonFoda != null) {
                                                        if(jsonFoda.size()>0) {
                                                            for (JsonElement eleFoda : jsonFoda) {
                                                                JsonObject foda = eleFoda.isJsonNull()?null: eleFoda.getAsJsonObject();
                                                                if(foda==null)
                                                                    continue;
                                                                FodaPropiedad miFoda = new FodaPropiedad();
                                                                miFoda.ganaderoid = foda.get("ganaderoid").getAsInt();
                                                                miFoda.propiedadid = foda.get("propiedadid").getAsInt();
                                                                miFoda.tipo = foda.get("tipo").getAsInt();
                                                                miFoda.descripcion = foda.get("descripcion").getAsString();
                                                                miFoda.causas = foda.get("causas").getAsString();
                                                                miFoda.solucion_1 = foda.get("solucion_1").getAsString();
                                                                miFoda.solucion_2 = foda.get("solucion_2").getAsString();
                                                                miFoda.observacion = foda.get("observacion").getAsString();
                                                                miPropiedad.listaFoda.add(miFoda);
                                                            }
                                                        }
                                                    }
                                                    if(pro.has("fotos")) {
                                                        jsonFotos = pro.get("fotos").isJsonNull() ? null : pro.getAsJsonArray("fotos");
                                                        if (jsonFotos != null) {
                                                            for (JsonElement eleFoto : jsonFotos) {
                                                                JsonObject jFoto = eleFoto.isJsonObject() ? eleFoto.getAsJsonObject() : null;
                                                                if (jFoto == null)
                                                                    continue;
                                                                Foto miFoto = new Foto();
                                                                miFoto.name = jFoto.get("name").isJsonNull() ? "" : jFoto.get("name").getAsString();
                                                                miFoto.path = jFoto.get("path_phone").isJsonNull() ? "" : jFoto.get("path_phone").getAsString();
                                                                miPropiedad.fotos.add(miFoto);
                                                            }
                                                        }
                                                    }

                                                    miPropiedad.Save();
                                                }
                                            } else {
                                                Log.d("TAG", "no hay propiedades");
                                            }
                                            numClientUpdate++;
                                        }
                                    }
                                    if (numClientUpdate == jsonClientes.size()) {
                                        Utils.showSuccessDialog(MainActivity.this, "Éxito", Constants.MSG_PROCESO_COMPLETADO + "\n" + obj.get("message").getAsString(), false, false);
                                        ((GanaderoFragment)fragment).CargarDatos(false);
                                    }else {
                                        Utils.showErrorDialog(MainActivity.this, "Error", Constants.MSG_PROCESO_NO_COMPLETADO + "\n" + obj.get("message").getAsString());
                                    }
                                }
                            } else
                                Utils.showErrorDialog(MainActivity.this, "Error", obj.get("message").getAsString());
                        } else {
                            Utils.showErrorDialog(MainActivity.this,"Error", Constants.MSG_USUARIO_CLAVE_INCORRECTO);
                        }
                    }catch (JsonParseException ex){
                        Log.d("TAG", ex.getMessage());
                    }
                    pbProgreso.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Utils.showErrorDialog(MainActivity.this, "Error",t.getMessage());
                    Log.d("TAG", t.getMessage());
                    pbProgreso.dismiss();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Log.d("TAG", e.getMessage());
            Utils.showMessage(this,e.getMessage());
            pbProgreso.dismiss();
        }
    }

    private void VerificarPermisos() {
        try {
            for (int i = 0; i < navigation.getMenu().size(); i++) {
                MenuItem menuItem = navigation.getMenu().getItem(i);
                if (menuItem.hasSubMenu()) {
                    /*for (int j = 0; j < menuItem.getSubMenu().size(); j++) {
                        MenuItem menuSubItem = menuItem.getSubMenu().getItem(j);
                        menuSubItem.setVisible(SQLite.usuario.VerificaPermiso(this,menuItem.getTitleCondensed().toString(),"lectura"));
                        break;
                    }*/
                } else {
                    menuItem.setVisible(SQLite.usuario.VerificaPermiso(this, menuItem.getTitleCondensed().toString().toUpperCase(),"lectura"));
                }
            }
            navigation.getMenu().findItem(R.id.nav_home).setVisible(false);

        }catch (Exception e){
            Log.d("TAGPERMISO", "VerificarPermisos(): " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        super.onResume();
    }

    private static long presionado;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(fragmentManager.getBackStackEntryCount()>0) {
                listaFragments.remove(listaFragments.size() - 1);
                super.onBackPressed();
            }else{
                if (presionado + 2000 > System.currentTimeMillis())
                    super.onBackPressed();
                else
                    Utils.showMessage(this, "Vuelve a presionar para salir");
                presionado = System.currentTimeMillis();
            }
        }
    }

}
