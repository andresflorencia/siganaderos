package com.florencia.visitaganadero.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.activities.PropiedadActivity;
import com.florencia.visitaganadero.models.Catalogo;
import com.florencia.visitaganadero.models.UsoSuelo;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.florencia.visitaganadero.utils.Utils;

import java.util.List;

public class UsoSueloAdapter extends RecyclerView.Adapter<UsoSueloAdapter.UsoSueloViewHolder>{

    public List<UsoSuelo> listUsoSuelo;
    private PropiedadActivity activity;
    private List<Catalogo> tiposCultivo;

    public UsoSueloAdapter(PropiedadActivity activity, List<UsoSuelo> listUsoSuelo, List<Catalogo> tiposCultivo){
        this.activity = activity;
        this.listUsoSuelo = listUsoSuelo;
        this.tiposCultivo = tiposCultivo;
    }

    @NonNull
    @Override
    public UsoSueloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsoSueloViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_usosuelo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsoSueloViewHolder holder, int position) {
        holder.bindUsoSuelo(listUsoSuelo.get(position));
    }

    @Override
    public int getItemCount() {
        return listUsoSuelo.size();
    }

    class UsoSueloViewHolder extends RecyclerView.ViewHolder {

        TextView tvTipoCultivo, tvVariedad, tvArea, tvObservacion;
        ImageButton btnOptions;
        CardView cvUsoSuelo;

        UsoSueloViewHolder(@NonNull View itemView){
            super(itemView);
            tvTipoCultivo = itemView.findViewById(R.id.tvTipoCultivo);
            tvVariedad = itemView.findViewById(R.id.tvVariedad);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvObservacion = itemView.findViewById(R.id.tvObservacion);

        }

        void bindUsoSuelo(final UsoSuelo usosuelo){
            tvTipoCultivo.setText(usosuelo.tipo_cultivo.nombrecatalogo);
            tvVariedad.setText(usosuelo.variedad_sembrada);
            tvArea.setText("Área (ha): " + usosuelo.area_cultivo);
            tvObservacion.setText(usosuelo.observacion);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ModificaUsoSuelo(listUsoSuelo.get(getAdapterPosition()));
                }
            });

        }

        void ModificaUsoSuelo(UsoSuelo miUsoSuelo){
            try{
                Spinner cbTipoCultivo;
                EditText txtAreaCultivo, txtVariedadSembrada, txtObservacionUS;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(activity).inflate(R.layout.layout_uso_suelo,
                        (ConstraintLayout) activity.findViewById(R.id.lyDialogContainer));
                builder.setView(view);
                ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_check_white);
                txtAreaCultivo = view.findViewById(R.id.txtAreaCultivo);
                txtAreaCultivo.setText(miUsoSuelo.area_cultivo.toString());
                txtVariedadSembrada = view.findViewById(R.id.txtVariedadSembrada);
                txtVariedadSembrada.setText(miUsoSuelo.variedad_sembrada);
                txtObservacionUS = view.findViewById(R.id.txtObservacion);
                txtObservacionUS.setText(miUsoSuelo.observacion);

                cbTipoCultivo = view.findViewById(R.id.cbTipoCultivo);
                ArrayAdapter<Catalogo> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, tiposCultivo);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cbTipoCultivo.setAdapter(adapter);
                int position = 0;
                for (int i=0; i < tiposCultivo.size(); i++){
                    if(tiposCultivo.get(i).codigocatalogo.equals(miUsoSuelo.tipo_cultivo.codigocatalogo)){
                        position = i;
                        break;
                    }
                }
                cbTipoCultivo.setSelection(position,true);

                final AlertDialog alertDialog = builder.create();
                view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        miUsoSuelo.tipo_cultivo = (Catalogo) cbTipoCultivo.getSelectedItem();
                        miUsoSuelo.variedad_sembrada = txtVariedadSembrada.getText().toString().trim();
                        miUsoSuelo.area_cultivo = Double.parseDouble(txtAreaCultivo.getText().toString().trim());
                        miUsoSuelo.observacion = txtObservacionUS.getText().toString().trim();
                        notifyDataSetChanged();
                        alertDialog.dismiss();
                    }
                });

                view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { alertDialog.dismiss();}
                });
                alertDialog.setCancelable(false);
                if(alertDialog.getWindow()!=null)
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
            }catch (Exception e){
                Log.d("TAGUSOSUELO_ADAPTER","ModificaUsoSuelo(): " + e.getMessage());
            }
        }
    }
}
