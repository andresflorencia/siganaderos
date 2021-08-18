package com.florencia.visitaganadero.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.models.Configuracion;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.florencia.visitaganadero.utils.Utils;
import com.shasin.notificationbanner.Banner;

public class ConfigActivity extends AppCompatActivity {

    private static final String TAG = "TAG_CONFIGACTIVITY";
    View rootView;
    Toolbar toolbar;
    EditText txtURLBase, txtNumFotosGanadero, txtNumFotosPropiedad;
    CheckBox ckSSL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        rootView = findViewById(android.R.id.content);

        init();
    }

    private void init() {
        txtURLBase = findViewById(R.id.txtUrlBase);
        txtNumFotosGanadero = findViewById(R.id.txtNumFotosGanadero);
        txtNumFotosPropiedad = findViewById(R.id.txtNumFotosPropiedad);
        ckSSL = findViewById(R.id.ckSSL);

        if(SQLite.configuracion!=null){
            txtURLBase.setText(SQLite.configuracion.urlbase);
            txtNumFotosGanadero.setText(SQLite.configuracion.maxfotoganadero.toString());
            txtNumFotosPropiedad.setText(SQLite.configuracion.maxfotopropiedad.toString());
            ckSSL.setChecked(SQLite.configuracion.hasSSL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        menu.findItem(R.id.option_new).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.option_save:
                ValidarDatos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ValidarDatos() {
        try{
            if(txtURLBase.getText().toString().trim().equals("")){
                Banner.make(rootView,this,Banner.ERROR, "Debe especificar la URL.",Banner.BOTTOM, 3000).show();
                txtURLBase.requestFocus();
                return;
            }else if(!Utils.isOnlineNet(txtURLBase.getText().toString().trim())){
                Banner.make(rootView,this,Banner.ERROR, "No es posible conectar con la URL ingresada. Verifique su conexión.",Banner.BOTTOM, 3000).show();
                return;
            }

            if(!Utils.isNumeric(txtNumFotosGanadero.getText().toString().trim())){
                Banner.make(rootView,this,Banner.ERROR, "Debe especificar un valor válido para el número máximo de fotos del ganadero.",Banner.BOTTOM, 3000).show();
                txtURLBase.requestFocus();
                return;
            }
            if(!Utils.isNumeric(txtNumFotosPropiedad.getText().toString().trim())){
                Banner.make(rootView,this,Banner.ERROR, "Debe especificar un valor válido para el número máximo de fotos de la propiedad.",Banner.BOTTOM, 3000).show();
                txtURLBase.requestFocus();
                return;
            }

            EditText txtPassword;
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_seguridad_dialog,
                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            txtPassword = view.findViewById(R.id.txtPassword);
            ((TextView)view.findViewById(R.id.lblTitle)).setText("Confirmación");
            ((TextView)view.findViewById(R.id.lblMessage)).setText("Especifique la contraseña de seguridad: ");
            ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_lock);
            ((Button)view.findViewById(R.id.btnCancel)).setText("Cancelar");
            ((Button)view.findViewById(R.id.btnConfirm)).setText("Confirmar");
            final android.app.AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(txtPassword.getText().toString().equals(Constants.CLAVE_SEGURIDAD)) {
                        GuardarDatos();
                        alertDialog.dismiss();
                    }else{
                        Banner.make(rootView,ConfigActivity.this, Banner.ERROR, "Clave incorrecta, intente nuevamente.", Banner.TOP,2000).show();
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

        }catch (Exception e){
            Log.d(TAG, "ValidarDatos(): " + e.getMessage());
        }
    }

    private void GuardarDatos(){
        try{
            Configuracion newConfig = new Configuracion();
            Integer numFG = Integer.valueOf(txtNumFotosGanadero.getText().toString().trim());
            Integer numFP = Integer.valueOf(txtNumFotosPropiedad.getText().toString().trim());
            newConfig.urlbase = txtURLBase.getText().toString().trim();
            newConfig.maxfotoganadero = numFG;
            newConfig.maxfotopropiedad = numFP;
            newConfig.hasSSL = ckSSL.isChecked();
            newConfig.url_ws = (newConfig.hasSSL?Constants.HTTPs:Constants.HTTP)
                    + newConfig.urlbase
                    + (SQLite.configuracion.hasSSL?"":"/erpproduccion")
                    + Constants.ENDPOINT;
            if(newConfig.Save()){
                SQLite.configuracion = newConfig;
                Utils.showSuccessDialog(this, "Configuración",Constants.MSG_DATOS_GUARDADOS,true,false);
                //Banner.make(rootView, this, Banner.SUCCESS, Constants.MSG_DATOS_GUARDADOS, Banner.BOTTOM,2000).show();
                //finish();
            }else{
                //Utils.showErrorDialog(this, "Error",Constants.MSG_DATOS_NO_GUARDADOS);
                Banner.make(rootView, this, Banner.ERROR, Constants.MSG_DATOS_NO_GUARDADOS, Banner.BOTTOM,3000).show();
            }
        }catch (Exception e){
            Log.d(TAG, "GuardarDatos(): " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        toolbar.setTitle("Configuración");
        toolbar.setTitleTextColor(Color.WHITE);
        super.onResume();
    }
}
