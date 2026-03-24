package dev.alllexey.itmowidgets.core

import api.myitmo.MyItmo
import com.google.gson.Gson
import dev.alllexey.itmowidgets.core.utils.InstantTypeAdapter
import dev.alllexey.itmowidgets.core.utils.LocalDateTypeAdapter
import dev.alllexey.itmowidgets.core.utils.LocalTimeTypeAdapter
import dev.alllexey.itmowidgets.core.utils.TokenInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

open class ItmoWidgetsImpl(
    val myItmo: MyItmo,
    private val baseUrl: String = STABLE_BASE_URL
) : ItmoWidgets {

    companion object {
        const val STABLE_BASE_URL = "https://widgets.alllexey.dev"
        const val DEV_BASE_URL = "https://dev.widgets.alllexey.dev"
    }

    override val api: ItmoWidgetsApi by lazy {
        retrofit.create(ItmoWidgetsApi::class.java)
    }

    override val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    override val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(this))
            .build()
    }

    override val gson: Gson by lazy {
        myItmo.gson.newBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter())
            .create()
    }

    override fun getValidToken(): String? {
        if (!hasRefreshToken() || isRefreshTokenExpired()) return null
        return myItmo.validTokens.accessToken
    }

    fun hasAccessToken(): Boolean = myItmo.hasAccessToken()
    fun hasRefreshToken(): Boolean = myItmo.hasRefreshToken()
    fun isAccessTokenExpired(): Boolean = myItmo.isAccessTokenExpired
    fun isRefreshTokenExpired(): Boolean = myItmo.isRefreshTokenExpired
}
