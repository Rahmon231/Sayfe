package com.lemzeeyyy.sayfe.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val retrofit = Retrofit.Builder()
    .baseUrl("BASE_URL")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface SMSService {
    @GET()
    fun sendSMS(

    )
}

object NetworkObject{
    val retrofitService: SMSService by lazy {
        retrofit.create(SMSService::class.java)
    }
}