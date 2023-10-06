package com.orbys.demowhiteboard.data.api

import com.orbys.demowhiteboard.core.Util
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val retrofitImages = Retrofit.Builder()
        .baseUrl(Util.baseUrlImages)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitYoutube = Retrofit.Builder()
        .baseUrl(Util.baseUrlYoutube)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitQR = Retrofit.Builder()
        .baseUrl(Util.baseUrlQr)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val serviceImage: RetrofitService = retrofitImages.create(RetrofitService::class.java)
    val serviceYoutube: RetrofitService = retrofitYoutube.create(RetrofitService::class.java)
    val serviceQR: RetrofitService = retrofitQR.create(RetrofitService::class.java)
}