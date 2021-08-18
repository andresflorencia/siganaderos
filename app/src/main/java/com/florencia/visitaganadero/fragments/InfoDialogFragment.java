package com.florencia.visitaganadero.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.models.Persona;

public class InfoDialogFragment extends AppCompatDialogFragment {
    private View view;
    private TextView txtNombre, txtNombreComercial, txtNip, txtContacto, txtDireccion, txtCorreo, txtCategoria;
    private ProgressBar pbCargando;
    private ImageButton btnCerrar;
    Persona cliente = new Persona();
    public InfoDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_info_dialog, container, false);

        txtNombre = (TextView)view.findViewById(R.id.txtNombre);
        txtNombreComercial = (TextView)view.findViewById(R.id.txtNombreComercial);
        txtNip = (TextView)view.findViewById(R.id.txtNIP);
        txtDireccion = (TextView)view.findViewById(R.id.txtDireccion);
        txtContacto = (TextView)view.findViewById(R.id.txtContacto);
        txtCorreo = (TextView) view.findViewById(R.id.txtCorreo);
        txtCategoria = view.findViewById(R.id.txtCategoria);
        pbCargando = view.findViewById(R.id.pbCargando);
        btnCerrar = view.findViewById(R.id.btnCerrar);

        if(!getArguments().isEmpty()) {
            int id = getArguments().getInt("idcliente",0);
            if(id>0)
                BuscarDatos(id);

        }

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return  view;
    }

    private void BuscarDatos(int id) {
        try{
            Thread th = new Thread(){
                @Override
                public void run(){
                    pbCargando.setVisibility(View.VISIBLE);
                    cliente = Persona.get(id,false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(cliente != null){
                                txtNombre.setText(cliente.razonsocial);
                                txtNombreComercial.setText(cliente.nombrecomercial.equals("")?"N/A":cliente.nombrecomercial);
                                txtNip.setText(cliente.nip);
                                txtCorreo.setText(cliente.email.equals("")?"N/A":cliente.email);
                                txtDireccion.setText(cliente.direccion.equals("")?"N/A":cliente.direccion);
                                txtContacto.setText(cliente.fono1.equals("") && cliente.fono2.equals("")?"N/A":cliente.fono1.concat(" - ").concat(cliente.fono2));
                                txtCategoria.setText(cliente.categoria.equals("")?"Sin categoría":"Categoría: ".concat(cliente.categoria));
                            }
                            pbCargando.setVisibility(View.GONE);
                        }
                    });
                }
            };
            th.start();
        }catch (Exception e){
            pbCargando.setVisibility(View.GONE);
            Log.d("TAGCLIENTEFRAGMENT", e.getMessage());
        }
    }

}
