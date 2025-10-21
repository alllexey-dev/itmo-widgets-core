package dev.alllexey.itmowidgets.core

import com.google.gson.Gson
import dev.alllexey.itmowidgets.core.utils.ItmoWidgetsStorage
import dev.alllexey.itmowidgets.core.model.TokenResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface ItmoWidgets {

    fun api(): ItmoWidgetsApi

    fun retrofit(): Retrofit

    fun okHttpClient(): OkHttpClient

    fun gson(): Gson

    fun storage(): ItmoWidgetsStorage
    
    fun loginBlocking(): TokenResponse

    fun refreshTokensBlocking(): TokenResponse

    fun getValidTokens(): TokenResponse

    suspend fun login(): Result<TokenResponse>

    suspend fun refreshTokens(): Result<TokenResponse>

    suspend fun getValidTokensAsync(): Result<TokenResponse>
}
