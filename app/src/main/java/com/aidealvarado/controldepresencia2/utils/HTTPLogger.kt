package com.aidealvarado.controldepresenciaAPFM.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
// Interceptor de https://stackoverflow.com/questions/45646188/how-can-i-debug-my-retrofit-api-call
object HTTPLogger {
    fun getLogger():OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        return  client
    }
}