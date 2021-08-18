package com.florencia.visitaganadero.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.florencia.visitaganadero.BuildConfig;
import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.adapters.FodaAdapter;
import com.florencia.visitaganadero.adapters.ImageAdapter;
import com.florencia.visitaganadero.adapters.UsoSueloAdapter;
import com.florencia.visitaganadero.models.Canton;
import com.florencia.visitaganadero.models.Catalogo;
import com.florencia.visitaganadero.models.FodaPropiedad;
import com.florencia.visitaganadero.models.Foto;
import com.florencia.visitaganadero.models.Parroquia;
import com.florencia.visitaganadero.models.Persona;
import com.florencia.visitaganadero.models.Propiedad;
import com.florencia.visitaganadero.models.Provincia;
import com.florencia.visitaganadero.models.UsoSuelo;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.florencia.visitaganadero.utils.Utils;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

public class PropiedadActivity extends AppCompatActivity {

    private static final String TAG = "TAGPROPIEDAD_ACTIVITY";
    private static final int REQUEST_NEW_FOTO = 20;
    private static final int REQUEST_SELECCIONA_FOTO = 30;

    TextView lblInfoPrincipal, lblInfoUbicacion, lblAnimales, lblUsoSuelo, lblLimitaciones, lblOportunidades, lblSituaciones, lblFotos;
    LinearLayout lyInfoPrincipal, lyInfoUbicacion, lyAnimales, lyUsoSuelo, lyLimitaciones, lyOportunidades, lySituaciones, lyFotos;
    EditText txtNombrePropiedad, txtPropietario, txtAdministrador, txtArea,
            txtLatitud, txtLongitud, txtDireccion, txtCaminoPrincipal, txtCaminoSecundario,
            txtNorte, txtSur, txtEste, txtOeste, txtFisiograficas, txtAccesibilidad, txtRazasGanado,
            txtNumVacasParidas, txtNumVacasPreñadas, txtNumVacasSolteras, txtTerneros, txtNumToros,
            txtNumAves, txtNumCerdos, txtNumEquinos, txtNumMascotas, txtOtrosAnimales;
    ImageButton btnObtenerLatLon, btnNewFoto;
    Button btnNewUsoSuelo, btnNewLimitacion, btnNewOportunidad, btnNewSituacion, btnFechaAdquisicion;
    Spinner cbProvincia, cbCanton, cbParroquia;
    RecyclerView rvUsoSuelo;
    UsoSueloAdapter usoSueloAdapter;

    Propiedad miPropiedad;
    Persona propietario, administrador;
    List<Provincia> provincias = new ArrayList<>();
    List<Canton> cantones = new ArrayList<>();
    List<Parroquia> parroquias = new ArrayList<>();
    List<Catalogo> tiposCultivo = new ArrayList<>();
    List<UsoSuelo> listUsoSuelo = new ArrayList<>();
    List<FodaPropiedad> listLimitaciones = new ArrayList<>();
    List<FodaPropiedad> listOportunidades = new ArrayList<>();
    List<FodaPropiedad> listSituaciones = new ArrayList<>();

    FodaAdapter adapterLimitaciones, adapterOportunidades, adapterSituaciones;
    RecyclerView rvLimitaciones, rvOportunidades, rvSituaciones;
    Integer idpropiedad=0;
    ProgressDialog pgCargando;
    Toolbar toolbar;

    DatePickerDialog dtpDialog;
    Calendar calendar;
    Boolean band = false;
    Provincia provActual = new Provincia();
    Canton canActual = new Canton();

    String path, nameFoto;
    File fileImage;
    Bitmap bitmap;

    RecyclerView rvFotos;
    ImageAdapter imageAdapter;

    List<Foto> listFoto = new ArrayList<>();

    String ExternalDirectory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propiedad);

        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        init();

        if(getIntent().getExtras()!=null){
            idpropiedad =  getIntent().getExtras().getInt("idpropiedad",0);
            Integer idpropietario = getIntent().getExtras().getInt("idpropietario",0);
            propietario = Persona.get(idpropietario,false);
            txtPropietario.setText(propietario.razonsocial);
            txtPropietario.setEnabled(false);
            if(idpropiedad>0){
                BuscarDatos(idpropiedad);
            }
            //this.isReturn = getIntent().getExtras().getBoolean("nuevo_cliente",false);
        }
    }

    private void init(){
        lblInfoPrincipal = findViewById(R.id.lblInfoPrincipal);
        lblInfoUbicacion = findViewById(R.id.lblInfoUbicacion);
        lblAnimales = findViewById(R.id.lblInfoAnimales);
        lblUsoSuelo = findViewById(R.id.lblUsoSuelo);
        lblLimitaciones = findViewById(R.id.lblLimitaciones);
        lblOportunidades = findViewById(R.id.lblOportunidades);
        lblSituaciones = findViewById(R.id.lblSituaciones);
        lyInfoPrincipal = findViewById(R.id.lyInfoPrincipal);
        lyInfoUbicacion = findViewById(R.id.lyInfoUbicacion);
        lyAnimales = findViewById(R.id.lyInfoAnimales);
        lyUsoSuelo = findViewById(R.id.lyUsoSuelo);
        lyLimitaciones = findViewById(R.id.lyLimitaciones);
        lyOportunidades = findViewById(R.id.lyOportunidades);
        lySituaciones = findViewById(R.id.lySituaciones);

        txtNombrePropiedad = findViewById(R.id.txtNombrePropiedad);
        txtPropietario = findViewById(R.id.txtPropietario);
        txtAdministrador = findViewById(R.id.txtAdministrador);
        btnFechaAdquisicion = findViewById(R.id.btnFechaAdquisicion);
        txtArea = findViewById(R.id.txtArea);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtCaminoPrincipal = findViewById(R.id.txtCaminoPrincipal);
        txtCaminoSecundario = findViewById(R.id.txtCaminoSecundario);
        txtNorte = findViewById(R.id.txtNorte);
        txtSur = findViewById(R.id.txtSur);
        txtEste = findViewById(R.id.txtEste);
        txtOeste = findViewById(R.id.txtOeste);
        txtFisiograficas = findViewById(R.id.txtFisiograficas);
        txtAccesibilidad = findViewById(R.id.txtAccesibilidad);
        txtRazasGanado = findViewById(R.id.txtRazasGanado);
        txtNumVacasParidas = findViewById(R.id.txtNumVacasParidas);
        txtNumVacasPreñadas = findViewById(R.id.txtNumVacasPreñadas);
        txtNumVacasSolteras = findViewById(R.id.txtNumVacasSolteras);
        txtTerneros = findViewById(R.id.txtNumTerneros);
        txtNumToros = findViewById(R.id.txtNumToros);
        txtNumAves = findViewById(R.id.txtNumAves);
        txtNumCerdos = findViewById(R.id.txtNumCerdos);
        txtNumEquinos = findViewById(R.id.txtNumEquinos);
        txtNumMascotas = findViewById(R.id.txtNumMascotas);
        txtOtrosAnimales = findViewById(R.id.txtOtrosAnimales);

        btnObtenerLatLon = findViewById(R.id.btnObtenerDireccion);
        btnNewUsoSuelo = findViewById(R.id.btnNewUsoSuelo);
        btnNewLimitacion = findViewById(R.id.btnNewLimitacion);
        btnNewOportunidad = findViewById(R.id.btnNewOportunidad);
        btnNewSituacion = findViewById(R.id.btnNewSituacion);
        cbProvincia = findViewById(R.id.cbProvincia);
        cbCanton = findViewById(R.id.cbCanton);
        cbParroquia = findViewById(R.id.cbParroquia);
        rvUsoSuelo = findViewById(R.id.rvUsoSuelo);
        rvLimitaciones = findViewById(R.id.rvLimitacion);
        rvOportunidades = findViewById(R.id.rvOportunidad);
        rvSituaciones = findViewById(R.id.rvSituacion);
        lblFotos = findViewById(R.id.lblFotos);
        lyFotos = findViewById(R.id.lyFotos);
        btnNewFoto = findViewById(R.id.btnNewFoto);
        rvFotos = findViewById(R.id.rvFotos);

        lblInfoPrincipal.setOnClickListener(onClick);
        lblInfoUbicacion.setOnClickListener(onClick);
        lblAnimales.setOnClickListener(onClick);
        lblUsoSuelo.setOnClickListener(onClick);
        btnObtenerLatLon.setOnClickListener(onClick);
        btnNewUsoSuelo.setOnClickListener(onClick);
        lblLimitaciones.setOnClickListener(onClick);
        lblOportunidades.setOnClickListener(onClick);
        lblSituaciones.setOnClickListener(onClick);
        btnNewLimitacion.setOnClickListener(onClick);
        btnNewOportunidad.setOnClickListener(onClick);
        btnNewSituacion.setOnClickListener(onClick);
        btnFechaAdquisicion.setOnClickListener(onClick);
        lblFotos.setOnClickListener(onClick);
        btnNewFoto.setOnClickListener(onClick);

        tiposCultivo = Catalogo.getCatalogo("TIPOCULTIVO");

        usoSueloAdapter = new UsoSueloAdapter(this,listUsoSuelo,tiposCultivo);
        rvUsoSuelo.setAdapter(usoSueloAdapter);

        adapterLimitaciones = new FodaAdapter(this,listLimitaciones,0);
        rvLimitaciones.setAdapter(adapterLimitaciones);
        adapterOportunidades = new FodaAdapter(this, listOportunidades,1);
        rvOportunidades.setAdapter(adapterOportunidades);
        adapterSituaciones = new FodaAdapter(this,listSituaciones,2);
        rvSituaciones.setAdapter(adapterSituaciones);

        btnFechaAdquisicion.setText(Utils.getDateFormat("yyyy-MM-dd"));
        txtAdministrador.setEnabled(false);
        propietario = new Persona();
        administrador = new Persona();

        imageAdapter = new ImageAdapter(this, listFoto);
        rvFotos.setAdapter(imageAdapter);

        ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;

        LlenarComboProvincias(0);
        LlenarComboCantones(0,-1);
        LlenarComboParroquias(0,-1);
        pgCargando = new ProgressDialog(this);

        cbProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(cbProvincia.getAdapter()!=null) {
                    Provincia provincia = ((Provincia)cbProvincia.getItemAtPosition(position));
                    if(provincia.idprovincia!=provActual.idprovincia)
                        band=false;
                    if(!band)
                        LlenarComboCantones(provincia.idprovincia,-1);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cbCanton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(cbCanton.getAdapter()!=null) {
                    Canton canton = ((Canton)cbCanton.getItemAtPosition(position));
                    if(!band)
                        LlenarComboParroquias(canton.idcanton,-1);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void showDatePickerDialog(View v) {
        Locale l = new Locale("ES-es");
        calendar = Calendar.getInstance(l);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int month=calendar.get(Calendar.MONTH);
        int year=calendar.get(Calendar.YEAR);
        String[] fecha= btnFechaAdquisicion.getText().toString().split("-");
        day = Integer.valueOf(fecha[2]);
        month = Integer.valueOf(fecha[1])-1;
        year = Integer.valueOf(fecha[0]);
        dtpDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String dia = (day>=0 && day<10?"0"+(day):String.valueOf(day));
                String mes = (month>=0 && month<10?"0"+(month+1):String.valueOf(month+1));

                String mitextoU = year + "-" + mes +"-" + dia;
                btnFechaAdquisicion.setText(mitextoU);
            }
        },year,month,day);
        dtpDialog.show();
    }

    private void LlenarComboProvincias(Integer idprovincia){
        try{
            provincias = Provincia.getList();
            ArrayAdapter<Provincia> adapterProvincia = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provincias);
            adapterProvincia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbProvincia.setAdapter(adapterProvincia);
            int position = 0;
            for(int i=0; i<provincias.size(); i++){
                if(provincias.get(i).idprovincia == idprovincia){
                    position = i;
                    break;
                }
            }
            if(idprovincia>=0)
                cbProvincia.setSelection(position,true);
        }catch (Exception e){
            Log.d(TAG, "LlenarComboProvincias(): " +e.getMessage());
        }
    }

    private void LlenarComboCantones(Integer idprovincia, Integer idcanton){
        try{
            cantones.clear();
            cantones = Canton.getList(idprovincia);
            ArrayAdapter<Canton> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cantones);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbCanton.setAdapter(adapter);
            int position = 0;
            for(int i=0; i<cantones.size(); i++){
                if(cantones.get(i).idcanton.equals(idcanton)){
                    position = i;
                    break;
                }
            }
            if(idcanton>=0)
                cbCanton.setSelection(position, true);
        }catch (Exception e){
            Log.d(TAG, "LlenarComboCantones(): " +e.getMessage());
        }
    }

    private void LlenarComboParroquias(Integer idcanton, Integer idparroquia){
        try{
            parroquias.clear();
            parroquias = Parroquia.getList(idcanton);
            ArrayAdapter<Parroquia> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parroquias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbParroquia.setAdapter(adapter);
            int position = 0;
            for(int i=0; i<parroquias.size(); i++){
                if(parroquias.get(i).idparroquia.equals(idparroquia)){
                    position = i;
                    break;
                }
            }
            if(idparroquia>=0) {
                cbParroquia.setSelection(position, true);
                //band = false;
            }
        }catch (Exception e){
            Log.d(TAG, "LlenarComboProvincias(): " +e.getMessage());
        }
    }

    private void BuscarDatos(Integer idpropiedad){
        Thread th = new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        miPropiedad = Propiedad.getById(idpropiedad);
                        if(miPropiedad!=null){
                            band = true;
                            Parroquia miparroquia = Parroquia.get(miPropiedad.parroquiaid);
                            canActual = Canton.get(miparroquia.cantonid);
                            provActual = Provincia.get(canActual.provinciaid);

                            for(Provincia miP:provincias){
                                if(provActual.idprovincia.equals(miP.idprovincia)){
                                    cbProvincia.setSelection(provincias.indexOf(miP));
                                    break;
                                }
                            }
                            LlenarComboCantones(provActual.idprovincia, canActual.idcanton);
                            LlenarComboParroquias(canActual.idcanton, miparroquia.idparroquia);

                            propietario = Persona.get(miPropiedad.propietarioid,false);
                            if(propietario==null)
                                propietario = Persona.get(miPropiedad.nip_propietario,false);
                            txtNombrePropiedad.setText(miPropiedad.nombrepropiedad);
                            txtPropietario.setText(propietario.razonsocial);
                            txtPropietario.setEnabled(false);
                            txtAdministrador.setText(miPropiedad.administrador.razonsocial);
                            btnFechaAdquisicion.setText(miPropiedad.fecha_adquisicion);
                            txtArea.setText(Utils.RoundDecimal(miPropiedad.area,2).toString());
                            txtLatitud.setText(miPropiedad.lat.toString());
                            txtLongitud.setText(miPropiedad.lon.toString());
                            txtDireccion.setText(miPropiedad.direccion);
                            txtCaminoPrincipal.setText(miPropiedad.caminos_principales);
                            txtCaminoSecundario.setText(miPropiedad.caminos_secundarios);
                            txtNorte.setText(miPropiedad.norte);
                            txtSur.setText(miPropiedad.sur);
                            txtEste.setText(miPropiedad.este);
                            txtOeste.setText(miPropiedad.oeste);
                            txtFisiograficas.setText(miPropiedad.caracteristicas_fisograficas);
                            txtAccesibilidad.setText(miPropiedad.condiciones_accesibilidad);
                            txtRazasGanado.setText(miPropiedad.razas_ganado);
                            txtNumVacasParidas.setText(miPropiedad.num_vacas_paridas.toString());
                            txtNumVacasPreñadas.setText(miPropiedad.num_vacas_preñadas.toString());
                            txtNumVacasSolteras.setText(miPropiedad.num_vacas_solteras.toString());
                            txtTerneros.setText(miPropiedad.num_terneros.toString());
                            txtNumToros.setText(miPropiedad.num_toros.toString());
                            txtNumAves.setText(miPropiedad.num_aves.toString());
                            txtNumCerdos.setText(miPropiedad.num_cerdos.toString());
                            txtNumEquinos.setText(miPropiedad.num_equinos.toString());
                            txtNumMascotas.setText(miPropiedad.num_mascotas.toString());
                            txtOtrosAnimales.setText(miPropiedad.otros);

                            usoSueloAdapter.listUsoSuelo.addAll(miPropiedad.listaUsoSuelo);
                            usoSueloAdapter.notifyDataSetChanged();

                            adapterLimitaciones.listFoda.addAll(miPropiedad.limitaciones);
                            adapterLimitaciones.notifyDataSetChanged();
                            adapterOportunidades.listFoda.addAll(miPropiedad.oportunidades);
                            adapterOportunidades.notifyDataSetChanged();
                            adapterSituaciones.listFoda.addAll(miPropiedad.situaciones);
                            adapterSituaciones.notifyDataSetChanged();

                            if(miPropiedad.fotos != null){
                                for(int i = 0; i<miPropiedad.fotos.size(); i++){
                                    try {
                                        File miFile = new File(ExternalDirectory, miPropiedad.fotos.get(i).name);
                                        Uri path = Uri.fromFile(miFile);
                                        miPropiedad.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                                PropiedadActivity.this.getContentResolver(),
                                                path);
                                        imageAdapter.listFoto.add(miPropiedad.fotos.get(i));
                                    } catch (IOException e) {
                                        Log.d(TAG, "NotFound(): " + e.getMessage());
                                    }
                                }
                                imageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        };
        th.start();
    }

    public void NuevoUsoSuelo(){
        try{
            final Spinner cbTipoCultivo;
            final EditText txtAreaCultivo, txtVariedadSembrada, txtObservacionUS;
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_uso_suelo,
                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_check_white);
            txtAreaCultivo = view.findViewById(R.id.txtAreaCultivo);
            txtVariedadSembrada = view.findViewById(R.id.txtVariedadSembrada);
            txtObservacionUS = view.findViewById(R.id.txtObservacion);
            cbTipoCultivo = view.findViewById(R.id.cbTipoCultivo);
            ArrayAdapter<Catalogo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposCultivo);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbTipoCultivo.setAdapter(adapter);

            final AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String variedad = txtVariedadSembrada.getText().toString().trim();
                        String area = txtAreaCultivo.getText().toString().trim();
                        Catalogo tipocultivo = (Catalogo) cbTipoCultivo.getSelectedItem();
                        UsoSuelo newUsoSuelo = new UsoSuelo();
                        newUsoSuelo.tipo_cultivo = tipocultivo;
                        newUsoSuelo.area_cultivo = area.equals("")?0d:Double.valueOf(area);
                        newUsoSuelo.variedad_sembrada = variedad;
                        newUsoSuelo.observacion = txtObservacionUS.getText().toString().trim();
                        usoSueloAdapter.listUsoSuelo.add(newUsoSuelo);
                        usoSueloAdapter.notifyDataSetChanged();
                        alertDialog.dismiss();
                    }catch (Exception e){
                        Log.d(TAG, "NuevoUsoSuelo(): " + e.getMessage());
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
            Log.d(TAG,"NuevoUsoSuelo(): " + e.getMessage());
        }
    }

    public void NuevoFoda(Integer tipo){
        //0: Limitaciones, 1: Oportunidad, 2: Situaciones Deseadas
        try{
            String title = "";
            if(tipo.equals(0))
                title = "Limitación";
            else if(tipo.equals(1))
                title = "Oportunidad";
            else if(tipo.equals(2))
                title = "Situación deseada";
            EditText txtFoda, txtCausas, txtSolucion1, txtSolucion2, txtObservacion;
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_foda,
                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_check_white);
            ((TextView)view.findViewById(R.id.lblTitle)).setText(title);
            txtFoda = view.findViewById(R.id.txtFoda);
            txtCausas = view.findViewById(R.id.txtCausas);
            txtSolucion1 = view.findViewById(R.id.txtSolucion1);
            txtSolucion2 = view.findViewById(R.id.txtSolucion2);
            txtObservacion = view.findViewById(R.id.txtObservacion);

            if(tipo==0){
                txtCausas.setVisibility(View.VISIBLE);
                txtSolucion1.setVisibility(View.VISIBLE);
                txtSolucion2.setVisibility(View.VISIBLE);
            }else{
                txtCausas.setVisibility(View.GONE);
                txtSolucion1.setVisibility(View.GONE);
                txtSolucion2.setVisibility(View.GONE);
            }

            final AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FodaPropiedad miFoda = new FodaPropiedad();
                    miFoda.descripcion = txtFoda.getText().toString().trim();
                    miFoda.causas = txtCausas.getText().toString().trim();
                    miFoda.solucion_1 = txtSolucion1.getText().toString().trim();
                    miFoda.solucion_2 = txtSolucion2.getText().toString().trim();
                    miFoda.observacion = txtObservacion.getText().toString().trim();
                    miFoda.tipo = tipo;
                    switch (tipo){
                        case 0:
                            adapterLimitaciones.listFoda.add(miFoda);
                            adapterLimitaciones.notifyDataSetChanged();
                            break;
                        case 1:
                            adapterOportunidades.listFoda.add(miFoda);
                            adapterOportunidades.notifyDataSetChanged();
                            break;
                        case 2:
                            adapterSituaciones.listFoda.add(miFoda);
                            adapterSituaciones.notifyDataSetChanged();
                            break;
                    }
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
        }catch (Exception e){
            Log.d("TAGFODA_ADAPTER","ModificaFoda(): " + e.getMessage());
        }
    }

    private void LimpiarDatos(){
        try{
            toolbar.setTitle("Nueva Propiedad");
            idpropiedad = 0;
            miPropiedad = new Propiedad();
            usoSueloAdapter.listUsoSuelo.clear(); usoSueloAdapter.notifyDataSetChanged();
            adapterLimitaciones.listFoda.clear(); adapterLimitaciones.notifyDataSetChanged();
            adapterOportunidades.listFoda.clear(); adapterOportunidades.notifyDataSetChanged();
            adapterSituaciones.listFoda.clear(); adapterSituaciones.notifyDataSetChanged();
        }catch (Exception e){
            Log.d(TAG, "LimpiarDatos(): " + e.getMessage());
        }
    }

    private void GuardarDatos(){
        try{
            if(!ValidarDatos()) return;

            if(miPropiedad == null)
                miPropiedad = new Propiedad();

            miPropiedad.nombrepropiedad = txtNombrePropiedad.getText().toString().trim();
            miPropiedad.propietarioid = propietario.idpersona;
            miPropiedad.administrador.idpersona = txtAdministrador.getText().toString().trim().equals("")? propietario.idpersona: administrador.idpersona;
            miPropiedad.fecha_adquisicion = btnFechaAdquisicion.getText().toString().trim();
            miPropiedad.area = Double.parseDouble(txtArea.getText().toString().trim());
            SQLite.gpsTracker.getLastKnownLocation();
            miPropiedad.lat =  SQLite.gpsTracker.getLatitude();//txtLatitud.setText(miPropiedad.lat);
            miPropiedad.lon = SQLite.gpsTracker.getLongitude();//txtLongitud.setText(miPropiedad.lon);
            miPropiedad.direccion = txtDireccion.getText().toString().trim();
            miPropiedad.caminos_principales = txtCaminoPrincipal.getText().toString().trim();
            miPropiedad.caminos_secundarios = txtCaminoSecundario.getText().toString().trim();
            miPropiedad.norte = txtNorte.getText().toString().trim();
            miPropiedad.sur = txtSur.getText().toString().trim();
            miPropiedad.este = txtEste.getText().toString().trim();
            miPropiedad.oeste = txtOeste.getText().toString().trim();
            miPropiedad.caracteristicas_fisograficas = txtFisiograficas.getText().toString().trim();
            miPropiedad.condiciones_accesibilidad = txtAccesibilidad.getText().toString().trim();
            miPropiedad.razas_ganado = txtRazasGanado.getText().toString().trim();
            miPropiedad.num_vacas_paridas = txtNumVacasParidas.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumVacasParidas.getText().toString().trim());
            miPropiedad.num_vacas_preñadas = txtNumVacasPreñadas.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumVacasPreñadas.getText().toString().trim());
            miPropiedad.num_vacas_solteras = txtNumVacasSolteras.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumVacasSolteras.getText().toString().trim());
            miPropiedad.num_terneros = txtTerneros.getText().toString().trim().equals("")?0:Integer.parseInt(txtTerneros.getText().toString().trim());
            miPropiedad.num_toros = txtNumToros.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumToros.getText().toString().trim());
            miPropiedad.num_aves = txtNumAves.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumAves.getText().toString().trim());
            miPropiedad.num_cerdos = txtNumCerdos.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumCerdos.getText().toString().trim());
            miPropiedad.num_equinos = txtNumEquinos.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumEquinos.getText().toString().trim());
            miPropiedad.num_mascotas = txtNumMascotas.getText().toString().trim().equals("")?0:Integer.parseInt(txtNumMascotas.getText().toString().trim());
            miPropiedad.otros = txtOtrosAnimales.getText().toString().trim();
            miPropiedad.actualizado = 1;
            miPropiedad.parroquiaid = ((Parroquia) cbParroquia.getSelectedItem()).idparroquia;
            miPropiedad.nip_administrador = propietario.nip;//txtAdministrador.getText().toString().trim().equals("")? propietario.nip: administrador.nip;
            miPropiedad.nip_propietario = propietario.nip;
            miPropiedad.usuarioid = SQLite.usuario.IdUsuario;
            miPropiedad.listaUsoSuelo.clear();
            miPropiedad.listaFoda.clear();
            miPropiedad.listaUsoSuelo.addAll(usoSueloAdapter.listUsoSuelo);
            miPropiedad.listaFoda.addAll(adapterLimitaciones.listFoda);
            miPropiedad.listaFoda.addAll(adapterOportunidades.listFoda);
            miPropiedad.listaFoda.addAll(adapterSituaciones.listFoda);

            miPropiedad.fotos.clear();
            miPropiedad.fotos.addAll(imageAdapter.listFoto);

            if(miPropiedad.Save()) {
                ContentValues val = new ContentValues();
                val.put("actualizado",1);
                Persona.Update(miPropiedad.propietarioid, val);
                Utils.showSuccessDialog(this, "Éxito", Constants.MSG_DATOS_GUARDADOS, true, true);
            }else
                Utils.showErrorDialog(this, "Error", Constants.MSG_DATOS_NO_GUARDADOS);
        }catch (Exception e){
            Log.d(TAG, "GuardarDatos(): " + e.getMessage());
        }
    }

    private boolean ValidarDatos(){
        try{
            if(btnFechaAdquisicion.getText().toString().trim().equals("")){
                Utils.showErrorDialog(this,"Error", "Debe especificar la fecha de adquisición de la propiedad");
                return false;
            }
            if(txtArea.getText().toString().trim().equals("")){
                Utils.showErrorDialog(this, "Error", "Debe especificar el área del terreno");
                return false;
            }
        }catch (Exception e){
            Log.d(TAG, "ValidarDatos(): " + e.getMessage());
            return false;
        }
        return true;
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.lblInfoPrincipal:
                    Utils.EfectoLayout(lyInfoPrincipal,lblInfoPrincipal);
                    break;
                case R.id.lblInfoUbicacion:
                    Utils.EfectoLayout(lyInfoUbicacion,lblInfoUbicacion);
                    break;
                case R.id.lblInfoAnimales:
                    Utils.EfectoLayout(lyAnimales,lblAnimales);
                    break;
                case R.id.lblUsoSuelo:
                    Utils.EfectoLayout(lyUsoSuelo,lblUsoSuelo);
                    break;
                case R.id.btnNewUsoSuelo:
                    NuevoUsoSuelo();
                    break;
                case R.id.lblLimitaciones:
                    Utils.EfectoLayout(lyLimitaciones, lblLimitaciones);
                    break;
                case R.id.lblOportunidades:
                    Utils.EfectoLayout(lyOportunidades, lblOportunidades);
                    break;
                case R.id.lblSituaciones:
                    Utils.EfectoLayout(lySituaciones, lblSituaciones);
                    break;
                case R.id.btnNewLimitacion:
                    NuevoFoda(0);
                    break;
                case R.id.btnNewOportunidad:
                    NuevoFoda(1);
                    break;
                case R.id.btnNewSituacion:
                    NuevoFoda(2);
                    break;
                case R.id.btnObtenerDireccion:
                    if(SQLite.gpsTracker.checkGPSEnabled()){
                        SQLite.gpsTracker.getLastKnownLocation();
                        txtLatitud.setText(String.valueOf(SQLite.gpsTracker.getLatitude()));
                        txtLongitud.setText(String.valueOf(SQLite.gpsTracker.getLongitude()));
                    }else
                        SQLite.gpsTracker.showSettingsAlert(v.getContext());
                    break;
                case R.id.btnFechaAdquisicion:
                    showDatePickerDialog(v);
                    break;
                case R.id.lblFotos:
                    Utils.EfectoLayout(lyFotos, lblFotos);
                    break;
                case R.id.btnNewFoto:
                    if(imageAdapter.listFoto.size() == SQLite.configuracion.maxfotopropiedad)
                        Utils.showMessageShort(PropiedadActivity.this, Constants.MSG_MAX_IMAGES);
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
                //nameFoto = consecutivo.toString() + ".jpg";
                nameFoto = propietario.nip + "_prop_" + Utils.getDateFormat("yyyyMMddHHmmss")+".jpg";
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
                if(idpropiedad == 0 && !SQLite.usuario.VerificaPermiso(this, Constants.VISITA_GANADERO, "escritura")){
                    Utils.showErrorDialog(this,"Error", "No tiene permisos para registrar propiedades.");
                    break;
                }
                if(idpropiedad > 0 && !SQLite.usuario.VerificaPermiso(this, Constants.VISITA_GANADERO, "modificacion")){
                    Utils.showErrorDialog(this,"Error","No tiene permisos para modificar datos de las propiedades.");
                    break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                        (ConstraintLayout) findViewById(R.id.lyDialogContainer));
                builder.setView(view);
                ((TextView)view.findViewById(R.id.lblTitle)).setText("Guardar propiedad");
                ((TextView)view.findViewById(R.id.lblMessage)).setText("¿Está seguro que desea guardar esta propiedad?");
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
                LimpiarDatos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Foto mifoto;
            switch (requestCode){
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
                        mifoto.bitmap = MediaStore.Images.Media.getBitmap(PropiedadActivity.this.getContentResolver(),miPath);
                        //Long consecutivo = System.currentTimeMillis()/1000;
                        //String nombre = consecutivo.toString()+".jpg";
                        String nombre = propietario.nip + "_prop_" + Utils.getDateFormat("yyyyMMddHHmmss")+".jpg";
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
                    MediaScannerConnection.scanFile(PropiedadActivity.this, new String[]{path}, null,
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            ((TextView)view.findViewById(R.id.lblTitle)).setText("Cerrar");
            ((TextView)view.findViewById(R.id.lblMessage)).setText("¿Desea salir de la ventana de propiedad?");
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

    @Override
    protected void onResume() {
        super.onResume();
        String titulo=idpropiedad == 0? "Nueva propiedad" : "Modificación";
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(titulo);
    }
}
