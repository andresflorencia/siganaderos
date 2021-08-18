package com.florencia.visitaganadero.adapters;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.activities.PropiedadActivity;
import com.florencia.visitaganadero.models.FodaPropiedad;

import java.util.List;

public class FodaAdapter extends RecyclerView.Adapter<FodaAdapter.FodaViewHolder>{

    public List<FodaPropiedad> listFoda;
    private PropiedadActivity activity;
    private Integer tipoFoda;

    public FodaAdapter(PropiedadActivity activity, List<FodaPropiedad> listFoda, Integer tipoFoda){
        this.activity = activity;
        this.listFoda = listFoda;
        this.tipoFoda = tipoFoda;
    }

    @NonNull
    @Override
    public FodaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FodaViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_foda, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FodaViewHolder holder, int position) {
        holder.bindFoda(listFoda.get(position));
    }

    @Override
    public int getItemCount() {
        return listFoda.size();
    }
    class FodaViewHolder extends RecyclerView.ViewHolder {

        TextView tvFoda, tvObservacion;

        FodaViewHolder(@NonNull View itemView){
            super(itemView);
            tvFoda = itemView.findViewById(R.id.tvFoda);
            tvObservacion = itemView.findViewById(R.id.tvObservacion);

        }

        void bindFoda(final FodaPropiedad foda){
            tvFoda.setText(foda.descripcion);
            tvObservacion.setText(foda.observacion);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ModificaFoda(listFoda.get(getAdapterPosition()));
                }
            });

        }

        void ModificaFoda(FodaPropiedad miFoda){
            //0: Limitaciones, 1: Oportunidad, 2: Situación Deseada
            try{
                String title = "";
                if(miFoda.tipo.equals(0))
                    title = "Limitación";
                else if(miFoda.tipo.equals(1))
                    title = "Oportunidad";
                else if(miFoda.tipo.equals(2))
                    title = "Situación deseada";
                EditText txtFoda, txtCausas, txtSolucion1, txtSolucion2, txtObservacion;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(activity).inflate(R.layout.layout_foda,
                        (ConstraintLayout) activity.findViewById(R.id.lyDialogContainer));
                builder.setView(view);
                ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_check_white);
                ((TextView)view.findViewById(R.id.lblTitle)).setText(title);
                txtFoda = view.findViewById(R.id.txtFoda);
                txtFoda.setText(miFoda.descripcion);
                txtCausas = view.findViewById(R.id.txtCausas);
                txtCausas.setText(miFoda.causas);
                txtSolucion1 = view.findViewById(R.id.txtSolucion1);
                txtSolucion1.setText(miFoda.solucion_1);
                txtSolucion2 = view.findViewById(R.id.txtSolucion2);
                txtSolucion2.setText(miFoda.solucion_2);
                txtObservacion = view.findViewById(R.id.txtObservacion);
                txtObservacion.setText(miFoda.observacion);

                if(miFoda.tipo==0){
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
                        miFoda.descripcion = txtFoda.getText().toString().trim();
                        miFoda.causas = txtCausas.getText().toString().trim();
                        miFoda.solucion_1 = txtSolucion1.getText().toString().trim();
                        miFoda.solucion_2 = txtSolucion2.getText().toString().trim();
                        miFoda.observacion = txtObservacion.getText().toString().trim();
                        notifyDataSetChanged();
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
    }
}
