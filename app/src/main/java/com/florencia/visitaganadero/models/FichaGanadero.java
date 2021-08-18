package com.florencia.visitaganadero.models;

public class FichaGanadero {
    public Integer idfichaganadero, codigosistema, ganaderoid;
    public String fecha_visita, acceso_internet,lugar_compra_insumos, motivo_compra, tipo_alimentos_animales,
            lugar_compra_productos_consumo, lugar_recibe_atención_medica, lugar_compra_medicina;
    public Double monto_atencion_medica;
    public String vision_familia;
    public Integer miembros_familiares;

    public FichaGanadero(){
        this.idfichaganadero = 0;
        this.codigosistema = 0;
        this.ganaderoid = 0;
        this.fecha_visita = "";
        this.acceso_internet = "";
        this.lugar_compra_insumos = "";
        this.motivo_compra = "";
        this.tipo_alimentos_animales = "";
        this.lugar_compra_productos_consumo = "";
        this.lugar_recibe_atención_medica = "";
        this.lugar_compra_medicina = "";
        this.monto_atencion_medica = 0d;
        this.vision_familia = "";
        this.miembros_familiares = 0;
    }
}
