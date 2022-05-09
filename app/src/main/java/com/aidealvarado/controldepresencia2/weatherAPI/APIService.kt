package com.aidealvarado.controldepresenciaAPFM.weatherAPI

import com.aidealvarado.controldepresenciaAPFM.Configuracion
import com.aidealvarado.controldepresenciaAPFM.models.GeoCoder
import com.aidealvarado.controldepresenciaAPFM.models.Welcome
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @GET("data/2.5/weather?&units=metric")
   suspend fun getWeather(@Query("q") city:String,
                   @Query("appid") apiKey :String = Configuracion.apiKey): Response<Welcome>

   @GET("data/2.5/weather?&units=metric")
   suspend fun getCityName(@Query("lat") lat:String,
                           @Query("lon") lon:String,
                           @Query("appid") apiKey:String = Configuracion.apiKey):Response<Welcome>
}