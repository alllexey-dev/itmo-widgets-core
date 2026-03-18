package dev.alllexey.itmowidgets.core

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface ItmoWidgets {

    val api: ItmoWidgetsApi

    val retrofit: Retrofit

    val okHttpClient: OkHttpClient

    val gson: Gson

    fun getValidToken(): String?
}
