package com.lemzeeyyy.sayfe.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BASE_URL = "https://fir-notification-demo-ccb40.uc.r.appspot.com/"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface SMSService {
    @POST("sendsms")
    suspend fun sendsms(@Query("recipient") recipient: String, @Query("body") body: String)
}

object NetworkObject{
    val retrofitService: SMSService by lazy {
        retrofit.create(SMSService::class.java)
    }
}