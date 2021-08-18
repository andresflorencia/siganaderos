package com.florencia.visitaganadero.interfaces;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UsuarioInterface {
    //API .NET CORE
    @FormUrlEncoded
    @POST("loginmovilganadero")
    Call<JsonObject> IniciarSesion(@Field("usuario") String user, @Field("clave") String clave,
                                   @Field("phone") String phone);

}

