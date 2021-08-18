package com.florencia.visitaganadero.interfaces;

import com.florencia.visitaganadero.models.Persona;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface GanaderoInterface {
    @FormUrlEncoded
    @POST("wsganaderos")
    Call<JsonObject> getGanaderos(@Field("usuario") String user, @Field("clave") String clave);

    @POST("loadganaderos")
    Call<JsonObject> loadGanaderos2(@Body Map<String, Object> clientes);
}
