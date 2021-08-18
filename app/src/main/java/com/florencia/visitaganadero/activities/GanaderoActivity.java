package com.florencia.visitaganadero.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.visitaganadero.BuildConfig;
import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.adapters.ImageAdapter;
import com.florencia.visitaganadero.adapters.PropiedadAdapter;
import com.florencia.visitaganadero.models.Foto;
import com.florencia.visitaganadero.models.Persona;
import com.florencia.visitaganadero.models.Propiedad;
import com.florencia.visitaganadero.models.TipoIdentificacion;
import com.florencia.visitaganadero.services.GPSTracker;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.florencia.visitaganadero.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GanaderoActivity extends AppCompatActivity {

    public static final int REQUEST_NEW_PROPIEDAD = 10;
    private static final String TAG = "TAGGANADERO_ACTIVITY";
    private static final int REQUEST_NEW_FOTO = 20;
    private static final int REQUEST_SELECCIONA_FOTO = 30;
    Spinner cbTipoDocumento;
    TextView txtNIP, txtRazonSocial, txtNombreComercial, txtLatitud, txtLongitud, txtDireccion,
            txtFono1, txtFono2, txtCorreo, txtObservacion;
    ImageButton btnObtenerDireccion, btnNewFoto;
    Persona miCliente;
    boolean isReturn = false;

    TextView lblMessage, lblTitle, lblInfoPersonal, lblInfoContacto, lblPropiedades, lblFotos;
    LinearLayout lyInfoPersonal, lyInfoContacto, lyPropiedades, lyFotos;
    BottomSheetDialog btsDialog;
    Button btnPositive, btnNegative, btnNewPropiedad;
    View viewSeparator;
    String tipoAccion = "";
    RecyclerView rvPropiedades;
    PropiedadAdapter propiedadAdapter;
    ImageView imgFoto1;

    String path, nameFoto;
    File fileImage;
    Bitmap bitmap;

    RecyclerView rvFotos;
    ImageAdapter imageAdapter;

    List<Foto> listFoto = new ArrayList<>();

    String ExternalDirectory = "";

    public GanaderoActivity() {
    }

    public GanaderoActivity(Persona miCliente) {
        this.miCliente = miCliente;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ganadero);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        if (getIntent().getExtras() != null) {
            Integer idcliente = getIntent().getExtras().getInt("idpersona", 0);
            if (idcliente > 0) {
                this.miCliente = Persona.get(idcliente, true);
            }
            this.isReturn = getIntent().getExtras().getBoolean("nuevo_cliente", false);
            /*ContentValues v = new ContentValues();
            v.put("nip_administrador", "1702915834001");
            v.put("administradorid",4971);
            Propiedad.Update(12,v);*/
        }

        if (this.miCliente != null) {
            this.txtNIP.setTag(this.miCliente.idpersona);
            txtNIP.setEnabled(false);
            this.txtNIP.setText(this.miCliente.nip);
            txtNIP.setEnabled(false);
            this.txtRazonSocial.setText(this.miCliente.razonsocial);
            txtRazonSocial.setEnabled(false);
            this.txtNombreComercial.setText(this.miCliente.nombrecomercial);
            this.txtLatitud.setText(this.miCliente.lat.toString());
            this.txtLongitud.setText(this.miCliente.lon.toString());
            this.txtDireccion.setText(this.miCliente.direccion);
            this.txtFono1.setText(this.miCliente.fono1);
            this.txtFono2.setText(this.miCliente.fono2);
            this.txtCorreo.setText(this.miCliente.email);
            this.txtObservacion.setText(this.miCliente.observacion);

            if (miCliente.tiponip != null) {
                for (int i = 0; i < cbTipoDocumento.getCount(); i++) {
                    TipoIdentificacion ti = (TipoIdentificacion) cbTipoDocumento.getItemAtPosition(i);
                    if (ti.getCodigo().equals(miCliente.tiponip)) {
                        cbTipoDocumento.setSelection(i, true);
                        break;
                    }
                }
                cbTipoDocumento.setEnabled(false);
            }

            if (miCliente.propiedades != null) {
                propiedadAdapter = new PropiedadAdapter(this, miCliente.propiedades);
                rvPropiedades.setAdapter(propiedadAdapter);
            }
            if(miCliente.fotos != null){
                for(int i = 0; i<miCliente.fotos.size(); i++){
                    try {
                        File miFile = new File(ExternalDirectory, miCliente.fotos.get(i).name);
                        Uri path = Uri.fromFile(miFile);
                        miCliente.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                GanaderoActivity.this.getContentResolver(),
                                path);
                        imageAdapter.listFoto.add(miCliente.fotos.get(i));
                    } catch (IOException e) {
                        Log.d(TAG, "NotFound(): " + e.getMessage());
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }
        }
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPositive:
                    if (tipoAccion.equals("MESSAGE")) {
                        btnNegative.setVisibility(View.VISIBLE);
                        viewSeparator.setVisibility(View.VISIBLE);
                        lblTitle.setVisibility(View.VISIBLE);
                        btsDialog.dismiss();
                    }
                    break;
                case R.id.btnNegative:
                    btsDialog.dismiss();
                    break;
                case R.id.btnNewPropiedad:
                    Intent i = new Intent(GanaderoActivity.this, PropiedadActivity.class);
                    i.putExtra("idpropietario", miCliente.idpersona);
                    startActivityForResult(i, REQUEST_NEW_PROPIEDAD);
                    break;
                case R.id.lblInfoPersonal:
                    Utils.EfectoLayout(lyInfoPersonal, lblInfoPersonal);
                    break;
                case R.id.lblInfoContacto:
                    Utils.EfectoLayout(lyInfoContacto, lblInfoContacto);
                    break;
                case R.id.lblPropiedades:
                    Utils.EfectoLayout(lyPropiedades, lblPropiedades);
                    break;
                case R.id.lblFotos:
                    Utils.EfectoLayout(lyFotos, lblFotos);
                    break;
                case R.id.btnObtenerDireccion:
                    //SQLite.gpsTracker = new GPSTracker(v.getContext());
                    if (SQLite.gpsTracker.checkGPSEnabled()) {
                        SQLite.gpsTracker.updateGPSCoordinates();
                        SQLite.gpsTracker.getLastKnownLocation();
                        txtLatitud.setText(String.valueOf(SQLite.gpsTracker.getLatitude()));
                        txtLongitud.setText(String.valueOf(SQLite.gpsTracker.getLongitude()));
                    } else
                        SQLite.gpsTracker.showSettingsAlert(v.getContext());
                    break;
                case R.id.btnNewFoto:
                    if(imageAdapter.listFoto.size() == SQLite.configuracion.maxfotoganadero)
                        Utils.showMessageShort(GanaderoActivity.this, Constants.MSG_MAX_IMAGES + "(");
                    else
                        ElegirOpcionFoto();
                    break;
            }
        }
    };

    private void ElegirOpcionFoto() {
        final CharSequence[] opciones = {"Desde cámara", "Desde galería"};
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Elija una opción");
            builder.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) { //DESDE LA CAMARA
                        openCamera();
                    } else if (which == 1) { //DESDE LA GALERIA
                        boolean exists = false;
                        File miFile = new File(getExternalMediaDirs()[0], Constants.FOLDER_FILES);
                        exists = miFile.exists();
                        if (!exists) {
                            exists = miFile.mkdirs();
                            Log.d(TAG, "NO EXISTE LA CARPETA: " + String.valueOf(exists) + " - " + miFile.canRead());
                        }
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        i.setType("image/*");
                        startActivityForResult(i.createChooser(i, "Seleccione"), REQUEST_SELECCIONA_FOTO);
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        } catch (Exception e) {
            Log.d(TAG, "elegirOpcionFoto(): " + e.getMessage());
        }
    }

    private void openCamera() {
        try {
            boolean exists = false;
            File miFile = new File(getExternalMediaDirs()[0], Constants.FOLDER_FILES);
            exists = miFile.exists();
            if (!exists) {
                exists = miFile.mkdirs();
                Log.d(TAG, "NO EXISTE LA CARPETA: " + String.valueOf(exists) + " - " + miFile.canRead());
                //exists = true;
            }
            if (exists) {
                Long consecutivo = System.currentTimeMillis() / 1000;
                nameFoto = miCliente.nip + "_gana_" + Utils.getDateFormat("yyyyMMddHHmmss")+".jpg";
                //nameFoto = consecutivo.toString() + ".jpg";
                path = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES
                        + File.separator + nameFoto;
                fileImage = new File(path);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(this,
                                BuildConfig.APPLICATION_ID + ".services.GenericFileProvider",
                                fileImage));
                startActivityForResult(i, REQUEST_NEW_FOTO);
            }
        } catch (Exception e) {
            Log.d(TAG, "openCamera(): " + e.getMessage());
        }
    }

    void init(){
        cbTipoDocumento = findViewById(R.id.spTipoDocumento);
        txtNIP = findViewById(R.id.txtNIP);
        txtRazonSocial = findViewById(R.id.txtRazonSocial);
        txtNombreComercial = findViewById(R.id.txtNombreComercial);
        btnObtenerDireccion = findViewById(R.id.btnObtenerDireccion);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtFono1 = findViewById(R.id.txtfono1);
        txtFono2 = findViewById(R.id.txtfono2);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtObservacion = findViewById(R.id.txtObservacion);
        rvPropiedades = findViewById(R.id.rvPropiedades);
        btnNewPropiedad = findViewById(R.id.btnNewPropiedad);
        lblInfoPersonal = findViewById(R.id.lblInfoPersonal);
        lblInfoContacto = findViewById(R.id.lblInfoContacto);
        lblPropiedades = findViewById(R.id.lblPropiedades);
        lyInfoPersonal = findViewById(R.id.lyInfoPersonal);
        lyInfoContacto = findViewById(R.id.lyInfoContacto);
        lyPropiedades = findViewById(R.id.lyPropiedades);
        lblFotos = findViewById(R.id.lblFotos);
        lyFotos = findViewById(R.id.lyFotos);
        btnNewFoto = findViewById(R.id.btnNewFoto);
        rvFotos = findViewById(R.id.rvFotos);
        //imgFoto1 = findViewById(R.id.imgFoto1);
        LlenarTipoNIP();

        imageAdapter = new ImageAdapter(this, listFoto);
        rvFotos.setAdapter(imageAdapter);

        ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;

        btnNewPropiedad.setOnClickListener(onClick);
        lblInfoPersonal.setOnClickListener(onClick);
        lblInfoContacto.setOnClickListener(onClick);
        lblPropiedades.setOnClickListener(onClick);
        btnObtenerDireccion.setOnClickListener(onClick);
        lblFotos.setOnClickListener(onClick);
        btnNewFoto.setOnClickListener(onClick);
    }

    void LlenarTipoNIP(){
        ArrayList<TipoIdentificacion> tipoIdentificaciones = new ArrayList<>();
        tipoIdentificaciones.add(new TipoIdentificacion("00", "SIN IDENTIFICACIÓN"));
        tipoIdentificaciones.add(new TipoIdentificacion("05", "CÉDULA"));
        tipoIdentificaciones.add(new TipoIdentificacion("04", "RUC"));
        tipoIdentificaciones.add(new TipoIdentificacion("06", "PASAPORTE"));
        tipoIdentificaciones.add(new TipoIdentificacion("07", "ID. EXTERIOR"));
        ArrayAdapter<TipoIdentificacion> adapter = new ArrayAdapter<TipoIdentificacion>(
                this, android.R.layout.simple_spinner_item, tipoIdentificaciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbTipoDocumento.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        menu.findItem(R.id.option_new).setVisible(false);
        menu.findItem(R.id.option_reimprimir).setVisible(false);
        menu.findItem(R.id.option_listdocument).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.option_save:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                        (ConstraintLayout) findViewById(R.id.lyDialogContainer));
                builder.setView(view);
                ((TextView)view.findViewById(R.id.lblTitle)).setText("Guardar persona");
                ((TextView)view.findViewById(R.id.lblMessage)).setText("¿Está seguro que desea guardar los datos de esta persona?");
                ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_save);
                ((Button)view.findViewById(R.id.btnCancel)).setText("Cancelar");
                ((Button)view.findViewById(R.id.btnConfirm)).setText("Si");
                final AlertDialog alertDialog = builder.create();
                view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GuardarDatos();
                        alertDialog.dismiss();
                    }
                });

                view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { alertDialog.dismiss();}
                });

                if(alertDialog.getWindow()!=null)
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
                break;
            case R.id.option_new:
                //LimpiarDatos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void GuardarDatos() {
        try {
            if(miCliente == null)
                miCliente = new Persona();
            if(!ValidarDatos()) return;

            miCliente.tiponip = ((TipoIdentificacion)cbTipoDocumento.getSelectedItem()).getCodigo();
            miCliente.nip = txtNIP.getText().toString().trim();
            miCliente.razonsocial = txtRazonSocial.getText().toString().trim();
            miCliente.nombrecomercial = txtNombreComercial.getText().toString().trim();
            SQLite.gpsTracker.getLastKnownLocation();
            miCliente.lat = SQLite.gpsTracker.getLatitude(); //Double.valueOf(txtLatitud.getText().toString().trim());
            miCliente.lon = SQLite.gpsTracker.getLongitude();//Double.valueOf(txtLongitud.getText().toString().trim());
            miCliente.direccion = txtDireccion.getText().toString().trim();
            miCliente.fono1 = txtFono1.getText().toString().trim();
            miCliente.fono2 = txtFono2.getText().toString().trim();
            miCliente.email = txtCorreo.getText().toString().trim();
            miCliente.observacion = txtObservacion.getText().toString().trim();
            miCliente.usuarioid = SQLite.usuario.IdUsuario;
            miCliente.actualizado = 1;
            miCliente.establecimientoid = SQLite.usuario.sucursal.IdEstablecimiento;

            miCliente.fotos.clear();
            miCliente.fotos.addAll(imageAdapter.listFoto);
            if(miCliente.Save()) {
                Utils.showMessage(this, Constants.MSG_DATOS_GUARDADOS);
                if(this.isReturn){
                    setResult(Activity.RESULT_OK,new Intent().putExtra("idpersona",miCliente.idpersona));
                    finish();
                }else {
                    //this.LimpiarDatos();
                    finish();
                }
            }else
                showError(Constants.MSG_DATOS_NO_GUARDADOS);

        }catch (Exception e){
            Log.d(TAG, e.getMessage());
            showError("Error: " + e.getMessage());
        }
    }

    private boolean ValidarDatos() throws Exception{
        if(miCliente.idpersona==0
            && !SQLite.usuario.VerificaPermiso(this,"Registro de Datos", "escritura")){
                showError("No tiene permisos para registrar nuevos clientes.");
                return false;
        }else if(miCliente.idpersona>0
            && !SQLite.usuario.VerificaPermiso(this,"Registro de Datos", "modificacion")){
                showError("No tiene permisos para modificar datos.");
                return false;
        }
        if(txtNIP.getText().toString().trim().equals("")){
            txtNIP.setError("Ingrese una identificación.");
            showError("Ingrese una identificación.");
            return false;
        }
        if(txtRazonSocial.getText().toString().trim().equals("")){
            txtRazonSocial.setError("Ingrese el nombre del cliente.");
            showError("Ingrese el nombre del cliente.");
            return false;
        }
        if(txtDireccion.getText().toString().trim().equals("")){
            txtRazonSocial.setError("Ingrese la dirección del cliente.");
            showError("Ingrese la dirección del cliente.");
            return false;
        }
        if(txtFono1.getText().toString().trim().equals("") && txtFono2.getText().toString().trim().equals("")){
            showError("Especifique al menos un número de contacto.");
            return false;
        }
        return true;
    }

    private void LimpiarDatos(){
        try {
            miCliente = new Persona();
            cbTipoDocumento.setSelection(0, true);
            txtNIP.setText("");
            txtNIP.setTag(0);
            txtRazonSocial.setText("");
            txtNombreComercial.setText("");
            txtLatitud.setText("");
            txtLongitud.setText("");
            txtDireccion.setText("");
            txtFono1.setText("");
            txtFono2.setText("");
            txtCorreo.setText("");
            txtDireccion.setText("");
            txtObservacion.setText("");
        }catch (Exception e){
            Log.d("TAGCLIENTE", "LimpiarDatos(): " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Foto mifoto;
            switch (requestCode){
                case REQUEST_NEW_PROPIEDAD:
                    miCliente.propiedades.clear();
                    miCliente.propiedades = Propiedad.getByPropietario(miCliente.idpersona);
                    propiedadAdapter.listPropiedad.clear();
                    propiedadAdapter.listPropiedad.addAll(miCliente.propiedades);
                    propiedadAdapter.notifyDataSetChanged();
                    break;
                case REQUEST_SELECCIONA_FOTO:

                    mifoto = new Foto();
                    Uri miPath = data.getData();
                    mifoto.uriFoto = miPath;
                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = managedQuery(miPath, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path1= cursor.getString(column_index);

                    try {
                        mifoto.bitmap = MediaStore.Images.Media.getBitmap(GanaderoActivity.this.getContentResolver(),miPath);
                        Long consecutivo = System.currentTimeMillis()/1000;
                        String nombre = miCliente.nip + "_gana_" + Utils.getDateFormat("yyyyMMddHHmmss")+".jpg";
                        path = getExternalMediaDirs()[0]+File.separator+Constants.FOLDER_FILES
                                +File.separator+nombre;

                        Utils.insert_image(mifoto.bitmap, nombre, ExternalDirectory, getContentResolver());
                        mifoto.path = path;
                        mifoto.name = nombre;
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    imageAdapter.listFoto.add(mifoto);
                    imageAdapter.notifyDataSetChanged();
                    //imgFoto1.setImageURI(miPath);
                    break;
                case REQUEST_NEW_FOTO:
                    MediaScannerConnection.scanFile(GanaderoActivity.this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.d(TAG, path);
                                }
                            });
                    bitmap = BitmapFactory.decodeFile(path);
                    Utils.insert_image(bitmap, nameFoto, ExternalDirectory, getContentResolver());
                    mifoto = new Foto();
                    mifoto.bitmap = bitmap;
                    mifoto.path = path;
                    mifoto.name = nameFoto;
                    imageAdapter.listFoto.add(mifoto);
                    imageAdapter.notifyDataSetChanged();
                    //imgFoto1.setImageBitmap(bitmap);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        String titulo=miCliente == null? "Nuevo registro" : "Modificación";
        toolbar.setTitle(titulo);
        toolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        //toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            ((TextView)view.findViewById(R.id.lblTitle)).setText("Cerrar");
            ((TextView)view.findViewById(R.id.lblMessage)).setText("¿Desea salir de la ventana de cliente?");
            ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_check_white);
            ((Button)view.findViewById(R.id.btnCancel)).setText("Cancelar");
            ((Button)view.findViewById(R.id.btnConfirm)).setText("Si");
            final AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { alertDialog.dismiss();}
            });

            if(alertDialog.getWindow()!=null)
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            alertDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void crearBottonSheet() {
        if(btsDialog==null){
            View view = LayoutInflater.from(this).inflate(R.layout.bottonsheet_message,null);
            btnPositive = view.findViewById(R.id.btnPositive);
            btnNegative = view.findViewById(R.id.btnNegative);
            lblMessage = view.findViewById(R.id.lblMessage);
            lblTitle = view.findViewById(R.id.lblTitle);
            viewSeparator = view.findViewById(R.id.vSeparator);
            btnPositive.setOnClickListener(onClick);
            btnNegative.setOnClickListener(onClick);

            btsDialog = new BottomSheetDialog(this, R.style.AlertDialogTheme);
            btsDialog.setContentView(view);
        }
    }

    public void showError(String message){
        crearBottonSheet();
        lblMessage.setText(message);
        btnNegative.setVisibility(View.GONE);
        viewSeparator.setVisibility(View.GONE);
        lblTitle.setVisibility(View.GONE);
        tipoAccion = "MESSAGE";
        if(btsDialog.getWindow()!=null)
            btsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        btsDialog.show();
    }
}
