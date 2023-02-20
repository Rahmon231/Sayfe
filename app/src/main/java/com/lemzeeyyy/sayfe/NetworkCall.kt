package com.lemzeeyyy.sayfe

import com.lemzeeyyy.sayfe.utils.Utilities.NOTIFICATION_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST



interface FirebasePushNotifier{
    @POST("send")
    suspend fun sendMsgPush(
        @HeaderMap headers:Map<String,String>,
        @Body data: PushNotifierBody,
    ): Response<Any>
}



private val moshi =  Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

private val retrofit = Retrofit.Builder()
    .client(getOkHttp())
    .addConverterFactory(GsonConverterFactory.create())//MoshiConverterFactory.create(moshi)
    .baseUrl(NOTIFICATION_URL)
    .build()

object ApiService{
    val retrofitApiService:FirebasePushNotifier by lazy {
        retrofit.create(FirebasePushNotifier::class.java)
    }
}

fun getOkHttp(): OkHttpClient {
    val logger = HttpLoggingInterceptor()
    logger.level = HttpLoggingInterceptor.Level.BASIC
    return OkHttpClient.Builder().addInterceptor(logger).build()
}