package com.florencia.visitaganadero.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.florencia.visitaganadero.MainActivity;
import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.interfaces.UsuarioInterface;
import com.florencia.visitaganadero.models.Canton;
import com.florencia.visitaganadero.models.Catalogo;
import com.florencia.visitaganadero.models.Configuracion;
import com.florencia.visitaganadero.models.Parroquia;
import com.florencia.visitaganadero.models.Permiso;
import com.florencia.visitaganadero.models.Provincia;
import com.florencia.visitaganadero.models.Sucursal;
import com.florencia.visitaganadero.models.Usuario;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.florencia.visitaganadero.utils.Utilidades;
import com.florencia.visitaganadero.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.shasin.notificationbanner.Banner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class actLogin extends AppCompatActivity{

    EditText etUser, etPassword;
    Button btnLogin;
    ImageButton btnConfig;
    private SharedPreferences sPreferencesSesion;
    private OkHttpClient okHttpClient;
    private ProgressDialog pbProgreso;
    View rootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_login);

        SQLite.sqlDB = new SQLite(getApplicationContext());
        Utilidades.createdb(this);
        pbProgreso = new ProgressDialog(this);
        rootView = findViewById(android.R.id.content);

        etUser = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);
        btnConfig = findViewById(R.id.btnConfig);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(onClick);
        btnConfig.setOnClickListener(onClick);

        SQLite.configuracion = Configuracion.GetLast();
        if(SQLite.configuracion==null || SQLite.configuracion.urlbase.equals(""))
        {
            //ConsultaConfig();
            SQLite.configuracion = new Configuracion();
            SQLite.configuracion.urlbase = "erp.alimentosfrescos.info";
            SQLite.configuracion.hasSSL = true;
            SQLite.configuracion.maxfotoganadero = 3;
            SQLite.configuracion.maxfotopropiedad = 3;
            SQLite.configuracion.Save();
        }
        SQLite.configuracion.url_ws = (SQLite.configuracion.hasSSL?Constants.HTTPs:Constants.HTTP)
                + SQLite.configuracion.urlbase
                + (SQLite.configuracion.hasSSL?"":"/erpproduccion")
                + Constants.ENDPOINT;

        sPreferencesSesion = getSharedPreferences("DatosSesion", MODE_PRIVATE);
        if(sPreferencesSesion != null){
            int id = sPreferencesSesion.getInt("idUser",0);
            String pin = sPreferencesSesion.getString("pin","");
            if(id>0){
                this.LoginLocal(id);
            }
            //Utils.showMessage(this, String.valueOf(id));
        }

        Utils.verificarPermisos(this);

        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        etPassword.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    IniciarSesion(v.getContext());
                    return true;
                }
                return false;
            }
        });
    }

    private void ConsultaConfig() {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(actLogin.this);
            dialog.setIcon(getResources().getDrawable(R.drawable.ic_settings));
            dialog.setTitle("Configuración");
            dialog.setMessage("Debe especificar la configuración para continuar");
            dialog.setPositiveButton("Configurar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(actLogin.this, ConfigActivity.class);
                    startActivity(i);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }catch (Exception e) {
            Log.d("TAGLOGIN", e.getMessage());
        }
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnLogin:
                    IniciarSesion(v.getContext());
                    break;
                case R.id.btnConfig:
                    Intent i = new Intent(actLogin.this, ConfigActivity.class);
                    startActivity(i);
                    break;
            }
        }
    };

    private void LoginLocal(Integer id) {
        try {
            Usuario user = Usuario.getUsuario(id);
            if(user != null) {
                SQLite.usuario = user;
                SQLite.usuario.GuardarSesionLocal(this);
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }catch (Exception e){
            Utils.showMessage(this, e.getMessage());
        }
    }

    private void IniciarSesion(final Context context){
        try{
            Usuario miUser = new Usuario();

            String User = etUser.getText().toString().trim();
            String Clave = etPassword.getText().toString();
            if (User.equals("")) {
                etUser.setError("Ingrese el usuario");
                return;
            }
            if(Clave.equals("")){
                etPassword.setError("Ingrese la contraseña");
                return;
            }

            if(!Utils.isOnlineNet(SQLite.configuracion.urlbase)) {
                miUser = Usuario.Login(User, Clave);
                if(miUser == null){
                    Utils.showMessage(context, "Usuario o contraseña incorrecta.");
                    return;
                }else{
                    SQLite.usuario = miUser;
                    SQLite.usuario.GuardarSesionLocal(context);
                    Utils.showMessage(context, "Bienvenido...");
                    Intent i = new Intent(actLogin.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
                return;
            }

            pbProgreso.setTitle("Iniciando sesión");
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
            UsuarioInterface miInterface = retrofit.create(UsuarioInterface.class);

            Call<JsonObject> call=null;
            call=miInterface.IniciarSesion(User,Clave,"");
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(!response.isSuccessful()){
                        Utils.showErrorDialog(actLogin.this,"Error", "Código:" + response.code() + " - " + response.message());
                        pbProgreso.dismiss();
                        return;
                    }
                    try {
                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                Usuario usuario = new Usuario();
                                JsonObject jsonUsuario = obj.getAsJsonObject("usuario");
                                usuario.IdUsuario = jsonUsuario.get("idpersona").getAsInt();
                                usuario.RazonSocial = jsonUsuario.get("razonsocial").getAsString();
                                usuario.Usuario = jsonUsuario.get("usuario").getAsString();
                                usuario.Clave = etPassword.getText().toString();
                                usuario.Perfil = jsonUsuario.get("perfil").getAsInt();
                                usuario.Autorizacion = jsonUsuario.get("auth").getAsInt();
                                usuario.sucursal = Sucursal.AsignaDatos(jsonUsuario.getAsJsonObject("sucursal"));
                                usuario.ParroquiaID = jsonUsuario.get("parroquiaid").getAsInt();

                                JsonArray jsonPermisos = jsonUsuario.get("permisos").getAsJsonArray();
                                //usuario.permisos = new Gson().fromJson(jsonPermisos, usuario.permisos.getClass());
                                if(jsonPermisos!=null){
                                    for(JsonElement element:jsonPermisos){
                                        JsonObject per = element.getAsJsonObject();
                                        Permiso mipermiso = new Permiso();
                                        mipermiso.nombreopcion = per.get("nombreopcion").getAsString();
                                        mipermiso.opcionid = per.get("opcionid").getAsInt();
                                        mipermiso.perfilid = per.get("perfilid").getAsInt();
                                        mipermiso.permisoescritura = per.get("permisoescritura").getAsString();
                                        mipermiso.permisoimpresion = per.get("permisoimpresion").getAsString();
                                        mipermiso.permisomodificacion = per.get("permisomodificacion").getAsString();
                                        mipermiso.permisoborrar = per.get("permisoborrar").getAsString();
                                        usuario.permisos.add(mipermiso);
                                    }
                                }

                                if(usuario.permisos == null || usuario.permisos.size()==0){
                                    Banner.make(rootView, actLogin.this, Banner.ERROR, "Su perfil no tiene permisos asignados. Contacte a soporte.", Banner.BOTTOM, 2500).show();
                                    return;
                                }

                                SQLite.configuracion.maxfotoganadero = jsonUsuario.has("maxfotosganadero")?jsonUsuario.get("maxfotosganadero").getAsInt():3;
                                SQLite.configuracion.maxfotopropiedad = jsonUsuario.has("maxfotospropiedad")?jsonUsuario.get("maxfotospropiedad").getAsInt():3;
                                SQLite.configuracion.Save();

                                if(usuario.Guardar()) {
                                    Catalogo.Delete("TIPOCULTIVO");
                                    Catalogo.Delete("MOTIVOCOMPRA");
                                    JsonArray jsonCatalogo = obj.get("catalogos").getAsJsonArray();
                                    List<Catalogo> listCatalogo= new ArrayList<>();
                                    for (JsonElement ele : jsonCatalogo) {
                                        JsonObject cata = ele.getAsJsonObject();
                                        Catalogo miCatalogo = new Catalogo();
                                        miCatalogo.idcatalogo = cata.get("idcatalogo").getAsInt();
                                        miCatalogo.nombrecatalogo = cata.get("nombrecatalogo").getAsString();
                                        miCatalogo.codigocatalogo = cata.get("codigocatalogo").getAsString();
                                        miCatalogo.codigopadre = cata.get("codigopadre").getAsString();
                                        listCatalogo.add(miCatalogo);
                                    }
                                    Catalogo.SaveLista(listCatalogo);

                                    List<Provincia> listProvincia = new ArrayList<>();
                                    JsonArray jsonProvincias = obj.get("provincias").getAsJsonArray();
                                    for (JsonElement ele : jsonProvincias) {
                                        JsonObject prov = ele.getAsJsonObject();
                                        Provincia miProvincia = new Provincia();
                                        miProvincia.idprovincia = prov.get("idprovincia").getAsInt();
                                        miProvincia.nombreprovincia = prov.get("nombreprovincia").getAsString();
                                        listProvincia.add(miProvincia);
                                    }
                                    Provincia.SaveLista(listProvincia);

                                    JsonArray jsonCantones = obj.get("cantones").getAsJsonArray();
                                    List<Canton> cantones = new ArrayList<>();
                                    for (JsonElement ele : jsonCantones) {
                                        JsonObject prov = ele.getAsJsonObject();
                                        Canton miCanton = new Canton();
                                        miCanton.idcanton= prov.get("idcanton").getAsInt();
                                        miCanton.nombrecanton= prov.get("nombrecanton").getAsString();
                                        miCanton.provinciaid = prov.get("provinciaid").getAsInt();
                                        cantones.add(miCanton);
                                    }
                                    Canton.SaveLista(cantones);

                                    JsonArray jsonParroquias = obj.get("parroquias").getAsJsonArray();
                                    List<Parroquia> parroquias = new ArrayList<>();
                                    for (JsonElement ele : jsonParroquias) {
                                        JsonObject prov = ele.getAsJsonObject();
                                        Parroquia miParroquia = new Parroquia();
                                        miParroquia.idparroquia= prov.get("idparroquia").getAsInt();
                                        miParroquia.nombreparroquia= prov.get("nombreparroquia").getAsString();
                                        miParroquia.cantonid= prov.get("cantonid").getAsInt();
                                        parroquias.add(miParroquia);
                                    }
                                    Parroquia.SaveLista(parroquias);

                                    SQLite.usuario = usuario;
                                    SQLite.usuario.GuardarSesionLocal(context);
                                    pbProgreso.dismiss();
                                    Intent i = new Intent(actLogin.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }else
                                    Banner.make(rootView, actLogin.this,Banner.ERROR, Constants.MSG_DATOS_NO_GUARDADOS, Banner.BOTTOM, 3000).show();
                            } else
                                Utils.showErrorDialog(actLogin.this,  "Error",obj.get("message").getAsString());
                        } else
                            Banner.make(rootView, actLogin.this,Banner.ERROR,  Constants.MSG_USUARIO_CLAVE_INCORRECTO, Banner.BOTTOM, 2500).show();
                    }catch (JsonParseException ex){
                        Log.d("TAG", ex.getMessage());
                    }
                    pbProgreso.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Utils.showErrorDialog(actLogin.this, "Error",t.getMessage());
                    Log.d("TAG", t.getMessage());
                    pbProgreso.dismiss();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Log.d("TAG", e.getMessage());
            Utils.showErrorDialog(actLogin.this, "Error",e.getMessage());
            pbProgreso.dismiss();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}
