package com.florencia.visitaganadero.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.visitaganadero.R;
import com.florencia.visitaganadero.activities.GanaderoActivity;
import com.florencia.visitaganadero.activities.PropiedadActivity;
import com.florencia.visitaganadero.models.Persona;
import com.florencia.visitaganadero.models.Propiedad;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PropiedadAdapter extends RecyclerView.Adapter<PropiedadAdapter.PropiedadViewHolder>{

    public List<Propiedad> listPropiedad;
    private List<Propiedad> orginalItems = new ArrayList<>();
    Activity context;

    public PropiedadAdapter(Activity context, List<Propiedad> listPropiedad) {
        this.context = context;
        this.listPropiedad = listPropiedad;
        this.orginalItems.addAll(this.listPropiedad);
    }

    @NonNull
    @Override
    public PropiedadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PropiedadViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_propiedad, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PropiedadViewHolder holder, int position) {
        holder.bindPropiedad(listPropiedad.get(position));
    }

    @Override
    public int getItemCount() {
        return listPropiedad.size();
    }

    public void filter(final String busqueda){
        listPropiedad.clear();
        if(busqueda.length()==0){
            listPropiedad.addAll(orginalItems);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<Propiedad> collect = orginalItems.stream()
                        .filter(i -> i.nombrepropiedad.concat(i.area.toString())
                                .concat(i.administrador.razonsocial.toLowerCase())
                                .contains(busqueda.toLowerCase()))
                        .collect(Collectors.<Propiedad>toList());
                listPropiedad.addAll(collect);
            }else{
                for(Propiedad i: orginalItems){
                    if(i.nombrepropiedad.concat(i.area.toString())
                            .concat(i.administrador.razonsocial.toLowerCase()
                            ).contains(busqueda.toLowerCase()))
                        listPropiedad.add(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    class PropiedadViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        TextView tvNombrePropiedad, tvPropietario, tvDireccion, tvArea, tvAdministrador, tvEstado;
        ImageButton btnOptions;
        CardView cvPropiedad;

        PropiedadViewHolder(@NonNull View itemView){
            super(itemView);
            tvNombrePropiedad = itemView.findViewById(R.id.tvNombrePropiedad);
            tvPropietario = itemView.findViewById(R.id.tvPropietario);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvAdministrador = itemView.findViewById(R.id.tvAdministrador);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnOptions = itemView.findViewById(R.id.btnOpciones);
            cvPropiedad = itemView.findViewById(R.id.cvPropiedad);
            btnOptions.setVisibility(View.GONE);
        }

        void bindPropiedad(final Propiedad propiedad){
            Persona propietario = Persona.get(propiedad.propietarioid,false);
            if(propietario == null)
                propietario = Persona.get(propiedad.nip_propietario,false);
            if(propiedad.administrador == null) {
                propiedad.administrador = Persona.get(propiedad.nip_administrador,false);
            }
            tvNombrePropiedad.setText(propiedad.nombrepropiedad);
            tvPropietario.setText(propietario.razonsocial);
            tvAdministrador.setText(propiedad.administrador.razonsocial);
            tvDireccion.setText(propiedad.direccion);
            tvArea.setText("Área: " + propiedad.area + "ha");
            if(propiedad.codigosistema==0 || propiedad.actualizado==1){
                tvEstado.setText("No sincronizado");
                tvEstado.setTextColor(itemView.getContext().getResources().getColor(R.color.colorend_splash));
                tvEstado.setVisibility(View.VISIBLE);
            }else
                tvEstado.setVisibility(View.GONE);

            btnOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(v);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, PropiedadActivity.class);
                    i.putExtra("idpropiedad", listPropiedad.get(getAdapterPosition()).idpropiedad);
                    i.putExtra("idpropietario",listPropiedad.get(getAdapterPosition()).propietarioid);
                    context.startActivityForResult(i, GanaderoActivity.REQUEST_NEW_PROPIEDAD);
                }
            });

        }

        private void showPopupMenu(View v){
            PopupMenu menu = new PopupMenu(v.getContext(),v);
            menu.inflate(R.menu.popup_menu_cliente);
            if((listPropiedad.get(this.getAdapterPosition()).codigosistema.equals(0)
                    || SQLite.usuario.VerificaPermiso(v.getContext(), Constants.VISITA_GANADERO,"lectura")
                    || SQLite.usuario.VerificaPermiso(v.getContext(), Constants.VISITA_GANADERO,"modificacion"))) {
                menu.getMenu().findItem(R.id.action_editar).setVisible(true);
            }else
                menu.getMenu().findItem(R.id.action_editar).setVisible(false);

            menu.getMenu().findItem(R.id.action_comprobante).setVisible(false);
            menu.getMenu().findItem(R.id.action_pedido).setVisible(false);
            menu.setOnMenuItemClickListener(this);
            menu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent i;
            switch (item.getItemId()) {
                case R.id.action_editar: //MODIFICACION DE DATOS
                    i = new Intent(context, PropiedadActivity.class);
                    i.putExtra("idpropiedad", listPropiedad.get(getAdapterPosition()).idpropiedad);
                    i.putExtra("idpropietario",listPropiedad.get(getAdapterPosition()).propietarioid);
                    context.startActivityForResult(i, GanaderoActivity.REQUEST_NEW_PROPIEDAD);
                    return true;
                default:
                    return false;
            }
        }
    }
}
