package com.florencia.visitaganadero.adapters;

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
import com.florencia.visitaganadero.models.Persona;
import com.florencia.visitaganadero.services.SQLite;
import com.florencia.visitaganadero.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GanaderoAdapter extends RecyclerView.Adapter<GanaderoAdapter.GanaderoViewHolder>{

    public List<Persona> listClients;
    private List<Persona> orginalItems = new ArrayList<>();
    Context context;

    public GanaderoAdapter(Context context, List<Persona> listClients) {
        this.context = context;
        this.listClients = listClients;
        this.orginalItems.addAll(this.listClients);
    }

    @NonNull
    @Override
    public GanaderoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GanaderoViewHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_ganadero, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GanaderoViewHolder holder, int position) {
        holder.bindCliente(listClients.get(position));
    }

    @Override
    public int getItemCount() {
        return listClients.size();
    }

    public void filter(final String busqueda){
        listClients.clear();
        if(busqueda.length()==0){
            listClients.addAll(orginalItems);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<Persona> collect = orginalItems.stream()
                        .filter(i -> i.nip.concat(i.razonsocial.toLowerCase())
                                .concat(i.nombrecomercial.toLowerCase())
                                .concat(i.direccion.toLowerCase()
                                ).contains(busqueda.toLowerCase()))
                        .collect(Collectors.<Persona>toList());
                listClients.addAll(collect);
            }else{
                for(Persona i: orginalItems){
                    if(i.nip.concat(i.razonsocial.toLowerCase())
                            .concat(i.nombrecomercial.toLowerCase())
                            .concat(i.direccion.toLowerCase()
                            ).contains(busqueda.toLowerCase()))
                        listClients.add(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    class GanaderoViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        TextView tvNombreComercial, tvRazonSocial, tvDireccion, tvContacto, tvEstado;
        ImageButton btnEdit, btnOptions;
        RoundedImageView imgCliente;
        CardView cvCliente;

        GanaderoViewHolder(@NonNull View itemView){
            super(itemView);
            tvRazonSocial = itemView.findViewById(R.id.tv_RazonSocial);
            tvNombreComercial = itemView.findViewById(R.id.tv_NombreComercial);
            tvDireccion = itemView.findViewById(R.id.tv_Direccion);
            tvContacto = itemView.findViewById(R.id.tv_Contacto);
            imgCliente = itemView.findViewById(R.id.imgCliente);
            //btnEdit = itemView.findViewById(R.id.btnEdit);
            btnOptions = itemView.findViewById(R.id.btnOpciones);
            cvCliente = itemView.findViewById(R.id.cvCliente);
            tvEstado = itemView.findViewById(R.id.tv_Estado);
            //cvCliente.setOnCreateContextMenuListener(this);

        }

        void bindCliente(final Persona cliente){
            tvRazonSocial.setText(cliente.nip + " - "+ cliente.razonsocial);
            tvNombreComercial.setText(cliente.nombrecomercial);
            tvDireccion.setText(cliente.direccion);
            tvContacto.setText(cliente.fono1 + " - " + cliente.fono2 + " - Categoría: " + cliente.categoria);
            btnOptions.setVisibility(View.GONE);
            //btnEdit.setTag(cliente.idcliente);
            //btnNewDocument.setTag(cliente.idcliente);
            if((cliente.codigosistema == 0 || cliente.actualizado == 1) && !cliente.nip.contains("999999999")) {
                tvEstado.setText("No sincronizado");
                tvEstado.setTextColor(itemView.getContext().getResources().getColor(R.color.colorend_splash));
                tvEstado.setVisibility(View.VISIBLE);
            }else{
                tvEstado.setVisibility(View.GONE);
            }

            btnOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(v);
                    /*if(!SQLite.usuario.VerificaPermiso(v.getContext(),Constants.REGISTRO_CLIENTE, "lectura")){
                        Utils.showMessage(v.getContext(),"No tiene permisos para visualizar datos de cliente.");
                        return;
                    }else {
                        Intent i = new Intent(v.getContext(), ClienteActivity.class);
                        i.putExtra("idcliente", listClients.get(getAdapterPosition()).idcliente);
                        v.getContext().startActivity(i);
                    }*/
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, GanaderoActivity.class);
                    i.putExtra("idpersona", listClients.get(getAdapterPosition()).idpersona);
                    context.startActivity(i);
                }
            });

        }

        private void showPopupMenu(View v){
            PopupMenu menu = new PopupMenu(v.getContext(),v);
            menu.inflate(R.menu.popup_menu_cliente);
            if((listClients.get(this.getAdapterPosition()).codigosistema.equals(0)
                    || SQLite.usuario.VerificaPermiso(v.getContext(), Constants.REGISTRO_CLIENTE,"lectura")
                    || SQLite.usuario.VerificaPermiso(v.getContext(), Constants.REGISTRO_CLIENTE,"modificacion"))
            && !listClients.get(this.getAdapterPosition()).nip.contains("999999999")) {
                menu.getMenu().findItem(R.id.action_editar).setVisible(true);
            }else
                menu.getMenu().findItem(R.id.action_editar).setVisible(false);

            menu.getMenu().findItem(R.id.action_comprobante).setVisible(SQLite.usuario.VerificaPermiso(v.getContext(),Constants.PUNTO_VENTA,"escritura"));
            menu.getMenu().findItem(R.id.action_pedido).setVisible(SQLite.usuario.VerificaPermiso(v.getContext(),Constants.PEDIDO,"escritura"));
            menu.setOnMenuItemClickListener(this);
            menu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent i;
            switch (item.getItemId()) {
                case R.id.action_editar: //MODIFICACION DE DATOS
                    i = new Intent(context, GanaderoActivity.class);
                    i.putExtra("idpersona", listClients.get(getAdapterPosition()).idpersona);
                    context.startActivity(i);
                    return true;
                default:
                    return false;
            }
        }
    }
}
