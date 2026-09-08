package dev.alllexey.itmowidgets.core

import api.myitmo.MyItmo
import com.google.gson.Gson
import dev.alllexey.itmowidgets.core.model.AppVersionInfo
import dev.alllexey.itmowidgets.core.model.SportAutoSignEntry
import dev.alllexey.itmowidgets.core.model.SportAutoSignQueue
import dev.alllexey.itmowidgets.core.model.SportFreeSignEntry
import dev.alllexey.itmowidgets.core.model.SportFreeSignQueue
import dev.alllexey.itmowidgets.core.model.SportQueue
import dev.alllexey.itmowidgets.core.model.SportQueueEntry
import dev.alllexey.itmowidgets.core.model.SharingVisibility
import dev.alllexey.itmowidgets.core.model.UserPrivacySettings
import dev.alllexey.itmowidgets.core.utils.InstantTypeAdapter
import dev.alllexey.itmowidgets.core.utils.AppVersionInfoTypeAdapter
import dev.alllexey.itmowidgets.core.utils.LocalDateTypeAdapter
import dev.alllexey.itmowidgets.core.utils.LocalTimeTypeAdapter
import dev.alllexey.itmowidgets.core.utils.RuntimeTypeAdapterFactory
import dev.alllexey.itmowidgets.core.utils.TokenInterceptor
import dev.alllexey.itmowidgets.core.utils.SharingVisibilityTypeAdapter
import dev.alllexey.itmowidgets.core.utils.UserPrivacySettingsTypeAdapter
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
        val sportQueueEntryAdapter = RuntimeTypeAdapterFactory
            .of(SportQueueEntry::class.java, "type", true)
            .registerSubtype(SportFreeSignEntry::class.java, "free")
            .registerSubtype(SportAutoSignEntry::class.java, "auto")

        val sportQueueAdapter = RuntimeTypeAdapterFactory
            .of(SportQueue::class.java, "type", true)
            .registerSubtype(SportFreeSignQueue::class.java, "free")
            .registerSubtype(SportAutoSignQueue::class.java, "auto")

        myItmo.gson.newBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter())
            .registerTypeAdapter(AppVersionInfo::class.java, AppVersionInfoTypeAdapter().nullSafe())
            .registerTypeAdapter(SharingVisibility::class.java, SharingVisibilityTypeAdapter())
            .registerTypeAdapter(UserPrivacySettings::class.java, UserPrivacySettingsTypeAdapter().nullSafe())
            .registerTypeAdapterFactory(sportQueueEntryAdapter)
            .registerTypeAdapterFactory(sportQueueAdapter)
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
