package com.example.art.data

import com.example.art.R
import com.example.art.model.PaintingInfoResponse
import com.example.art.utils.AppContextProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface PaintingApi {

    @GET("/api/paintings/{id}")
    suspend fun getPaintingInfo(@Path("id") id: Int): PaintingInfoResponse
}

object ApiClient {
    private val BASE_URL: String = AppContextProvider.getContext()
        .getString(R.string.server_base_url)
        .removeSuffix("/") + "/api/paintings/"


    val paintingApi: PaintingApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PaintingApi::class.java)
}
