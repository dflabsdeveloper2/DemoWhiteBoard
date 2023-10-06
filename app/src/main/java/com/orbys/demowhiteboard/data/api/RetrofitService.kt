package com.orbys.demowhiteboard.data.api

import com.orbys.demowhiteboard.data.api.model.QrDataJson
import com.orbys.demowhiteboard.data.api.model.ResponseQR
import com.orbys.demowhiteboard.data.api.model.image.ListImageModelApi
import com.orbys.demowhiteboard.data.api.model.youtube.ListVideoModelApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface RetrofitService {

    @GET("v1")
    suspend fun getListImages(
        @Query("key") key: String, @Query("cx") motor: String,
        @Query("q") searchWord: String,
        @Query("rights") licencia: String, @Query("safe") safesearch: String,
        @Query("searchType") type: String, @Query("num") num: String,
    ): ListImageModelApi

    @GET("v1")
    suspend fun getListImagesWithoutLicense(
        @Query("key") key: String, @Query("cx") motor: String,
        @Query("q") searchWord: String,
        @Query("safe") safesearch: String,
        @Query("searchType") type: String, @Query("num") num: String,
    ): ListImageModelApi

    @GET("v1")
    suspend fun getListImagesWithoutSafeSearch(
        @Query("key") key: String, @Query("cx") motor: String,
        @Query("q") searchWord: String,
        @Query("rights") licencia: String,
        @Query("searchType") type: String, @Query("num") num: String,
    ): ListImageModelApi

    @GET("v1")
    suspend fun getListImagesWithoutSafeSearchAndLicense(
        @Query("key") key: String, @Query("cx") motor: String,
        @Query("q") searchWord: String,
        @Query("searchType") type: String, @Query("num") num: String,
    ): ListImageModelApi

    @GET("search")
    suspend fun getListYoutubeVideos(
        @Query("key") key: String, @Query("q") searchWord: String,
        @Query("maxResults") maxResult: String, @Query("type") type: String,
        @Query("part") part: String,
    ): ListVideoModelApi


    @POST("upload")
    suspend fun saveImages(@Body data: QrDataJson): ResponseQR

}